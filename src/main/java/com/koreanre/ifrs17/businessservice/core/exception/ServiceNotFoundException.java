package com.koreanre.ifrs17.businessservice.core.exception;

/** 서비스/버전 없음 또는 비활성 (HTTP 404 / BS-SVC-404). */
public class ServiceNotFoundException extends BusinessServiceException {

    public ServiceNotFoundException(String serviceId, String version) {
        super(ErrorCode.BS_SVC_404, "서비스를 찾을 수 없거나 비활성 상태입니다: " + serviceId + " v" + version);
    }
}
