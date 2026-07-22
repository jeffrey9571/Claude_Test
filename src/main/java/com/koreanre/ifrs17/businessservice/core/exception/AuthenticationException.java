package com.koreanre.ifrs17.businessservice.core.exception;

/** 인증 실패 (HTTP 401 / BS-AUTH-001, BS-AUTH-002). */
public class AuthenticationException extends BusinessServiceException {

    public AuthenticationException(String message) {
        super(ErrorCode.BS_AUTH_001, message);
    }

    public static AuthenticationException tokenExpired(String message) {
        return new AuthenticationException(ErrorCode.BS_AUTH_002, message);
    }

    private AuthenticationException(ErrorCode code, String message) {
        super(code, message);
    }
}
