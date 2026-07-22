package com.koreanre.ifrs17.businessservice.core.exception;

/** 서비스 권한 없음 (HTTP 403 / BS-AUTH-003). */
public class AuthorizationException extends BusinessServiceException {

    public AuthorizationException(String message) {
        super(ErrorCode.BS_AUTH_003, message);
    }
}
