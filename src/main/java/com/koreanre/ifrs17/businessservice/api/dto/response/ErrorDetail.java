package com.koreanre.ifrs17.businessservice.api.dto.response;

import java.util.Collections;
import java.util.List;

/** 5.5 표준 Error Response의 error 객체. */
public class ErrorDetail {

    private String code;
    private String message;
    private String errorId;
    private List<String> details = Collections.emptyList();

    public static ErrorDetail of(String code, String message, String errorId, List<String> details) {
        ErrorDetail e = new ErrorDetail();
        e.code = code;
        e.message = message;
        e.errorId = errorId;
        e.details = details == null ? Collections.emptyList() : details;
        return e;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getErrorId() {
        return errorId;
    }

    public void setErrorId(String errorId) {
        this.errorId = errorId;
    }

    public List<String> getDetails() {
        return details;
    }

    public void setDetails(List<String> details) {
        this.details = details;
    }
}
