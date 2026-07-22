package com.koreanre.ifrs17.businessservice.core.audit;

import com.koreanre.ifrs17.businessservice.core.context.ServiceContext;
import com.koreanre.ifrs17.businessservice.persistence.mapper.BsCallLogRepository;
import com.koreanre.ifrs17.businessservice.persistence.model.BsCallLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * 4.2 AuditLogger: 호출 전후 및 실패 로그 저장.
 * 6.3 감사로그 필수항목을 BS_CALL_LOG에 기록한다.
 * 7.4: 감사로그 저장 실패가 업무 응답을 무조건 실패시키지 않도록 기본은 Fail Open으로 동작하되,
 * business-service.audit.fail-closed=true 설정 시 저장 실패를 상위로 전파한다(보안상 필수 로그).
 */
@Component
public class AuditLogger {

    private static final Logger log = LoggerFactory.getLogger(AuditLogger.class);
    private static final String STATUS_IN_PROGRESS = "IN_PROGRESS";
    private static final String STATUS_SUCCESS = "SUCCESS";
    private static final String STATUS_ERROR = "ERROR";

    private final BsCallLogRepository repository;
    private final boolean failClosed;

    public AuditLogger(BsCallLogRepository repository,
            @Value("${business-service.audit.fail-closed:false}") boolean failClosed) {
        this.repository = repository;
        this.failClosed = failClosed;
    }

    public void start(ServiceContext context, String serviceVersion, Map<String, Object> parameters) {
        log.info(">>> [진입] AuditLogger.start() - 감사 시작 로그 저장. requestId={}", context.getRequestId());
        BsCallLog entry = BsCallLog.builder()
                .requestId(context.getRequestId())
                .traceId(context.getTraceId())
                .parentRequestId(context.getParentRequestId())
                .serviceId(context.getServiceId())
                .serviceVersion(serviceVersion)
                .clientId(context.getClientId())
                .userId(context.getUserId())
                .departmentCode(context.getDepartmentCode())
                .roles(String.join(",", context.getRoles()))
                .requestedAt(context.getRequestedAt() != null ? context.getRequestedAt() : LocalDateTime.now())
                .statusCode(STATUS_IN_PROGRESS)
                .parameterHash(hashParameters(parameters))
                .sensitiveAccessFlag("N")
                .remoteIp(context.getRemoteIp())
                .authType("HEADER_SSO")
                .serverInstance(context.getServerInstance())
                .build();
        persist(entry);
    }

    public void success(ServiceContext context, String serviceVersion, int httpStatus, Integer resultCount, long elapsedMs) {
        log.info(">>> [진입] AuditLogger.success() - 감사 성공 로그 갱신. requestId={}, resultCount={}",
                context.getRequestId(), resultCount);
        Optional<BsCallLog> existing = repository.findById(context.getRequestId());
        BsCallLog entry = existing.orElseGet(() -> newFallbackEntry(context, serviceVersion));
        entry.setCompletedAt(LocalDateTime.now());
        entry.setElapsedMs((int) elapsedMs);
        entry.setStatusCode(STATUS_SUCCESS);
        entry.setHttpStatus(httpStatus);
        entry.setResultCount(resultCount);
        entry.setAuthorizationResult("ALLOWED");
        persist(entry);
    }

    public void fail(ServiceContext context, String serviceVersion, int httpStatus, String errorCode, String errorId,
            long elapsedMs) {
        log.info(">>> [진입] AuditLogger.fail() - 감사 실패 로그 갱신. requestId={}, errorCode={}",
                context.getRequestId(), errorCode);
        Optional<BsCallLog> existing = repository.findById(context.getRequestId());
        BsCallLog entry = existing.orElseGet(() -> newFallbackEntry(context, serviceVersion));
        entry.setCompletedAt(LocalDateTime.now());
        entry.setElapsedMs((int) elapsedMs);
        entry.setStatusCode(STATUS_ERROR);
        entry.setHttpStatus(httpStatus);
        entry.setErrorCode(errorCode);
        entry.setErrorId(errorId);
        entry.setAuthorizationResult(httpStatus == 401 || httpStatus == 403 ? "DENIED" : entry.getAuthorizationResult());
        persist(entry);
    }

    private BsCallLog newFallbackEntry(ServiceContext context, String serviceVersion) {
        return BsCallLog.builder()
                .requestId(context.getRequestId())
                .traceId(context.getTraceId())
                .parentRequestId(context.getParentRequestId())
                .serviceId(context.getServiceId())
                .serviceVersion(serviceVersion)
                .clientId(context.getClientId())
                .userId(context.getUserId())
                .departmentCode(context.getDepartmentCode())
                .roles(String.join(",", context.getRoles()))
                .requestedAt(context.getRequestedAt() != null ? context.getRequestedAt() : LocalDateTime.now())
                .sensitiveAccessFlag("N")
                .remoteIp(context.getRemoteIp())
                .serverInstance(context.getServerInstance())
                .build();
    }

    private void persist(BsCallLog entry) {
        try {
            repository.save(entry);
        } catch (Exception e) {
            log.error("감사로그 저장 실패: requestId={}", entry.getRequestId(), e);
            if (failClosed) {
                throw e;
            }
        }
    }

    /** Request 원문 전체 저장을 금지하므로(6.4) 파라미터는 정렬 후 해시만 저장한다. */
    private String hashParameters(Map<String, Object> parameters) {
        if (parameters == null || parameters.isEmpty()) {
            return null;
        }
        String canonical = new TreeMap<>(parameters).entrySet().stream()
                .map(e -> e.getKey() + "=" + e.getValue())
                .collect(Collectors.joining("&"));
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(canonical.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 알고리즘을 사용할 수 없습니다.", e);
        }
    }
}
