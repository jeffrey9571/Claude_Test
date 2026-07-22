package com.koreanre.ifrs17.businessservice.core.exception;

/** 처리시간 초과 (HTTP 504 / BS-SYS-504). */
public class ServiceTimeoutException extends BusinessServiceException {

    public ServiceTimeoutException(String serviceId, long timeoutMs) {
        super(ErrorCode.BS_SYS_504, "서비스 처리시간이 초과되었습니다: " + serviceId + " (" + timeoutMs + "ms)");
    }
}
