package com.koreanre.ifrs17.businessservice.core.response;

import com.koreanre.ifrs17.businessservice.api.dto.response.ErrorDetail;
import com.koreanre.ifrs17.businessservice.api.dto.response.StandardResponse;
import com.koreanre.ifrs17.businessservice.core.context.ServiceContext;
import com.koreanre.ifrs17.businessservice.core.exception.BusinessServiceException;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/** 4.2 StandardResponseBuilder: 공통 Envelope 생성. */
@Component
public class StandardResponseBuilder {

    private static final String SOURCE_SYSTEM = "IFRS17";

    public <T> StandardResponse<T> success(ServiceContext context, String serviceVersion, T result, long elapsedMs) {
        return success(context, serviceVersion, result, elapsedMs, Collections.emptyList());
    }

    public <T> StandardResponse<T> success(ServiceContext context, String serviceVersion, T result, long elapsedMs,
            List<String> warnings) {
        return StandardResponse.success(
                context.getRequestId(),
                context.getTraceId(),
                context.getServiceId(),
                serviceVersion,
                SOURCE_SYSTEM,
                OffsetDateTime.now(),
                elapsedMs,
                result,
                warnings);
    }

    public <T> StandardResponse<T> error(ServiceContext context, BusinessServiceException ex) {
        String errorId = "ERR-" + UUID.randomUUID().toString().replace("-", "").substring(0, 14).toUpperCase();
        ErrorDetail detail = ErrorDetail.of(ex.errorCode().code(), ex.getMessage(), errorId, ex.details());
        return StandardResponse.error(context == null ? null : context.getRequestId(),
                context == null ? null : context.getServiceId(), detail);
    }
}
