package com.koreanre.ifrs17.businessservice.core.exception;

import java.util.Collections;
import java.util.List;

/**
 * 표준 처리 순서에서 발생하는 모든 업무/시스템 예외의 기준 타입.
 * StandardErrorResponse로 변환되는 최소 정보(code, message, details)를 가진다.
 */
public class BusinessServiceException extends RuntimeException {

    private final ErrorCode errorCode;
    private final List<String> details;

    public BusinessServiceException(ErrorCode errorCode) {
        this(errorCode, errorCode.defaultMessage(), Collections.emptyList());
    }

    public BusinessServiceException(ErrorCode errorCode, String message) {
        this(errorCode, message, Collections.emptyList());
    }

    public BusinessServiceException(ErrorCode errorCode, String message, List<String> details) {
        super(message);
        this.errorCode = errorCode;
        this.details = details == null ? Collections.emptyList() : details;
    }

    public BusinessServiceException(ErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.details = Collections.emptyList();
    }

    public ErrorCode errorCode() {
        return errorCode;
    }

    public List<String> details() {
        return details;
    }
}
