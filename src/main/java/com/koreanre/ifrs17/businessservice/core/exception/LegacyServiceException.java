package com.koreanre.ifrs17.businessservice.core.exception;

/** 기존 Service 예외 (HTTP 500 / BS-LEG-500). Legacy Adapter가 감싸서 던진다. */
public class LegacyServiceException extends BusinessServiceException {

    public LegacyServiceException(String message, Throwable cause) {
        super(ErrorCode.BS_LEG_500, message, cause);
    }
}
