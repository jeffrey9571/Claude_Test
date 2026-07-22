package com.koreanre.ifrs17.businessservice.core.executor;

import com.koreanre.ifrs17.businessservice.api.dto.request.StandardRequest;
import com.koreanre.ifrs17.businessservice.api.dto.response.StandardResponse;
import com.koreanre.ifrs17.businessservice.core.audit.AuditLogger;
import com.koreanre.ifrs17.businessservice.core.context.RequestContextResolver;
import com.koreanre.ifrs17.businessservice.core.context.ServiceContext;
import com.koreanre.ifrs17.businessservice.core.dispatcher.BusinessServiceDispatcher;
import com.koreanre.ifrs17.businessservice.core.exception.BusinessServiceException;
import com.koreanre.ifrs17.businessservice.core.exception.ServiceDisabledException;
import com.koreanre.ifrs17.businessservice.core.exception.ServiceNotFoundException;
import com.koreanre.ifrs17.businessservice.core.exception.ServiceTimeoutException;
import com.koreanre.ifrs17.businessservice.core.exception.SystemException;
import com.koreanre.ifrs17.businessservice.core.masking.MaskingPolicy;
import com.koreanre.ifrs17.businessservice.core.metadata.ServiceMetadata;
import com.koreanre.ifrs17.businessservice.core.metadata.ServiceMetadataRepository;
import com.koreanre.ifrs17.businessservice.core.response.StandardResponseBuilder;
import com.koreanre.ifrs17.businessservice.core.security.AuthorizationService;
import com.koreanre.ifrs17.businessservice.core.security.ClientValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * 4.2 BusinessServiceExecutor: 공통 처리 템플릿, 시간측정, 예외변환.
 * 10.4 공통 Executor 의사코드를 그대로 구현한다.
 */
@Component
public class BusinessServiceExecutor {

    private static final Logger log = LoggerFactory.getLogger(BusinessServiceExecutor.class);

    private final RequestContextResolver contextResolver;
    private final ClientValidator clientValidator;
    private final ServiceMetadataRepository metadataRepository;
    private final AuthorizationService authorizationService;
    private final BusinessServiceDispatcher dispatcher;
    private final MaskingPolicy maskingPolicy;
    private final AuditLogger auditLogger;
    private final StandardResponseBuilder responseBuilder;
    private final ExecutorService timeoutExecutor = Executors.newCachedThreadPool();

    public BusinessServiceExecutor(RequestContextResolver contextResolver, ClientValidator clientValidator,
            ServiceMetadataRepository metadataRepository, AuthorizationService authorizationService,
            BusinessServiceDispatcher dispatcher, MaskingPolicy maskingPolicy, AuditLogger auditLogger,
            StandardResponseBuilder responseBuilder) {
        this.contextResolver = contextResolver;
        this.clientValidator = clientValidator;
        this.metadataRepository = metadataRepository;
        this.authorizationService = authorizationService;
        this.dispatcher = dispatcher;
        this.maskingPolicy = maskingPolicy;
        this.auditLogger = auditLogger;
        this.responseBuilder = responseBuilder;
    }

    public ResponseEntity<StandardResponse<?>> execute(String serviceId, StandardRequest request,
            HttpServletRequest httpRequest) {
        long start = System.currentTimeMillis();
        ServiceContext context = null;
        String serviceVersion = request == null ? null : request.getServiceVersion();
        try {
            // 1~2. HTTP Header/Body 수신, Request ID 생성/검증, SSO Context 추출
            context = contextResolver.resolve(httpRequest, serviceId);

            // 3. 호출 Client 검증
            clientValidator.validate(context.getClientId(), serviceId);

            // 5. Service Catalog에서 활성 버전 조회
            ServiceMetadata metadata = metadataRepository.findActive(serviceId, serviceVersion)
                    .orElseThrow(() -> new ServiceNotFoundException(serviceId, String.valueOf(serviceVersion)));
            serviceVersion = metadata.getVersion();

            // 6. 서비스/역할 권한 확인
            authorizationService.authorize(context, metadata);

            // 8. 감사 시작 로그 기록
            Map<String, Object> parameters = request.getParameters() == null
                    ? Collections.emptyMap() : request.getParameters();
            auditLogger.start(context, serviceVersion, parameters);

            // 7,9,10. 입력검증 + Handler 실행(Legacy Adapter 호출 포함), Timeout 보호
            Object result = invokeWithTimeout(context, metadata, parameters);

            // 11. 결과 마스킹
            Object maskedResult = maskingPolicy.mask(result, MaskingPolicy.DEFAULT_MASKING);

            long elapsedMs = System.currentTimeMillis() - start;
            int resultCount = maskedResult == null ? 0 : 1;

            // 12. 감사 성공 로그
            auditLogger.success(context, serviceVersion, HttpStatus.OK.value(), resultCount, elapsedMs);

            // 13. 표준 Response 반환
            StandardResponse<Object> response = responseBuilder.success(context, serviceVersion, maskedResult, elapsedMs);
            return ResponseEntity.ok(response);

        } catch (BusinessServiceException e) {
            return handleFailure(context, serviceId, serviceVersion, start, e);
        } catch (Exception e) {
            log.error("예상치 못한 오류가 발생했습니다. serviceId={}", serviceId, e);
            return handleFailure(context, serviceId, serviceVersion, start, new SystemException(e));
        }
    }

    private Object invokeWithTimeout(ServiceContext context, ServiceMetadata metadata, Map<String, Object> parameters) {
        Callable<Object> task = () -> dispatcher.dispatch(context, metadata, parameters);
        Future<Object> future = timeoutExecutor.submit(task);
        try {
            return future.get(metadata.getTimeoutMs(), TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            future.cancel(true);
            throw new ServiceTimeoutException(metadata.getServiceId(), metadata.getTimeoutMs());
        } catch (java.util.concurrent.ExecutionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof BusinessServiceException) {
                throw (BusinessServiceException) cause;
            }
            throw new SystemException(cause == null ? e : cause);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new SystemException(e);
        }
    }

    private ResponseEntity<StandardResponse<?>> handleFailure(ServiceContext context, String serviceId,
            String serviceVersion, long start, BusinessServiceException ex) {
        long elapsedMs = System.currentTimeMillis() - start;
        StandardResponse<?> errorResponse = responseBuilder.error(
                context != null ? context : fallbackContext(serviceId), ex);
        if (context != null) {
            auditLogger.fail(context, serviceVersion == null ? "" : serviceVersion,
                    ex.errorCode().httpStatus().value(), ex.errorCode().code(), errorResponse.getError().getErrorId(),
                    elapsedMs);
        }
        return ResponseEntity.status(ex.errorCode().httpStatus()).body(errorResponse);
    }

    private ServiceContext fallbackContext(String serviceId) {
        return ServiceContext.builder().serviceId(serviceId).build();
    }

    @PreDestroy
    public void shutdown() {
        timeoutExecutor.shutdown();
    }
}
