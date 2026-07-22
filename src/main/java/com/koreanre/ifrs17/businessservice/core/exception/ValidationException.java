package com.koreanre.ifrs17.businessservice.core.exception;

import java.util.List;

/** 입력 오류 (HTTP 400 / BS-VAL-001, BS-VAL-002). */
public class ValidationException extends BusinessServiceException {

    public ValidationException(String message) {
        super(ErrorCode.BS_VAL_001, message);
    }

    public ValidationException(String message, List<String> fieldErrors) {
        super(ErrorCode.BS_VAL_001, message, fieldErrors);
    }

    public static ValidationException outOfRange(String message) {
        return new ValidationException(ErrorCode.BS_VAL_002, message);
    }

    private ValidationException(ErrorCode code, String message) {
        super(code, message);
    }
}
