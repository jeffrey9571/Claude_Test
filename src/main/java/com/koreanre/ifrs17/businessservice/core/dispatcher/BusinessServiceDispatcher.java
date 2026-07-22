package com.koreanre.ifrs17.businessservice.core.dispatcher;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.koreanre.ifrs17.businessservice.core.context.ServiceContext;
import com.koreanre.ifrs17.businessservice.core.exception.SystemException;
import com.koreanre.ifrs17.businessservice.core.exception.ValidationException;
import com.koreanre.ifrs17.businessservice.core.executor.BusinessServiceHandler;
import com.koreanre.ifrs17.businessservice.core.metadata.ServiceMetadata;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 4.2 BusinessServiceDispatcher: serviceId와 구현 Bean 매핑.
 * Console에서 등록한 implementation_bean 이름으로 Spring Bean을 조회하고,
 * BusinessServiceHandler 계약(validate/authorize/process)에 따라 실행한다.
 */
@Slf4j
@Component
public class BusinessServiceDispatcher {

    private final ApplicationContext applicationContext;
    private final ObjectMapper objectMapper;

    public BusinessServiceDispatcher(ApplicationContext applicationContext, ObjectMapper objectMapper) {
        this.applicationContext = applicationContext;
        this.objectMapper = objectMapper;
    }

    @SuppressWarnings("unchecked")
    public Object dispatch(ServiceContext context, ServiceMetadata metadata, Map<String, Object> parameters) {
        log.info(">>> [진입] BusinessServiceDispatcher.dispatch() - serviceId={}, 구현 Bean={}",
                metadata.getServiceId(), metadata.getImplementationBean());
        BusinessServiceHandler<Object, Object> handler = resolveHandler(metadata);

        if (!metadata.getServiceId().equals(handler.serviceId())) {
            throw new SystemException(new IllegalStateException(
                    "등록된 Service ID와 Handler의 serviceId()가 일치하지 않습니다: "
                            + metadata.getServiceId() + " != " + handler.serviceId()));
        }

        Object request = convertRequest(parameters, handler.requestType());
        log.info("    [Handler] validate() 호출");
        handler.validate(context, request);
        log.info("    [Handler] authorize() 호출");
        handler.authorize(context, request);
        log.info("    [Handler] process() 호출");
        return handler.process(context, request);
    }

    @SuppressWarnings("unchecked")
    private BusinessServiceHandler<Object, Object> resolveHandler(ServiceMetadata metadata) {
        Object bean;
        try {
            bean = applicationContext.getBean(metadata.getImplementationBean());
        } catch (BeansException e) {
            throw new SystemException(new IllegalStateException(
                    "구현 Bean을 찾을 수 없습니다: " + metadata.getImplementationBean(), e));
        }
        if (!(bean instanceof BusinessServiceHandler)) {
            throw new SystemException(new IllegalStateException(
                    "Bean이 BusinessServiceHandler를 구현하지 않습니다: " + metadata.getImplementationBean()));
        }
        return (BusinessServiceHandler<Object, Object>) bean;
    }

    private Object convertRequest(Map<String, Object> parameters, Class<?> requestType) {
        try {
            return objectMapper.convertValue(parameters, requestType);
        } catch (IllegalArgumentException e) {
            throw new ValidationException("요청 파라미터 형식이 올바르지 않습니다: " + e.getMessage());
        }
    }
}
