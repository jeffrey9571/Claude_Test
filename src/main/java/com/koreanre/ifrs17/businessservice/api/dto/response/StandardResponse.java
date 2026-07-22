package com.koreanre.ifrs17.businessservice.api.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;

/**
 * 5.4/5.5 표준 Success/Error Response Envelope.
 * 성공 시 result/warnings, 실패 시 error가 채워진다.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class StandardResponse<T> {

    public enum Status { SUCCESS, ERROR }

    private String requestId;
    private String traceId;
    private String serviceId;
    private String serviceVersion;
    private String sourceSystem;
    private Status status;
    private OffsetDateTime processedAt;
    private long elapsedMs;
    private T result;
    private List<String> warnings = Collections.emptyList();
    private ErrorDetail error;

    public static <T> StandardResponse<T> success(String requestId, String traceId, String serviceId,
            String serviceVersion, String sourceSystem, OffsetDateTime processedAt, long elapsedMs,
            T result, List<String> warnings) {
        StandardResponse<T> r = new StandardResponse<>();
        r.requestId = requestId;
        r.traceId = traceId;
        r.serviceId = serviceId;
        r.serviceVersion = serviceVersion;
        r.sourceSystem = sourceSystem;
        r.status = Status.SUCCESS;
        r.processedAt = processedAt;
        r.elapsedMs = elapsedMs;
        r.result = result;
        r.warnings = warnings == null ? Collections.emptyList() : warnings;
        return r;
    }

    public static <T> StandardResponse<T> error(String requestId, String serviceId, ErrorDetail error) {
        StandardResponse<T> r = new StandardResponse<>();
        r.requestId = requestId;
        r.serviceId = serviceId;
        r.status = Status.ERROR;
        r.error = error;
        return r;
    }

    public String getRequestId() {
        return requestId;
    }

    public String getTraceId() {
        return traceId;
    }

    public String getServiceId() {
        return serviceId;
    }

    public String getServiceVersion() {
        return serviceVersion;
    }

    public String getSourceSystem() {
        return sourceSystem;
    }

    public Status getStatus() {
        return status;
    }

    public OffsetDateTime getProcessedAt() {
        return processedAt;
    }

    public long getElapsedMs() {
        return elapsedMs;
    }

    public T getResult() {
        return result;
    }

    public List<String> getWarnings() {
        return warnings;
    }

    public ErrorDetail getError() {
        return error;
    }
}
