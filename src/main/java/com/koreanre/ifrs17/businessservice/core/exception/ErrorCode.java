package com.koreanre.ifrs17.businessservice.core.exception;

import org.springframework.http.HttpStatus;

/**
 * 부록 A. 표준 오류코드.
 */
public enum ErrorCode {

    BS_VAL_001(HttpStatus.BAD_REQUEST, "필수값 누락 또는 형식 오류"),
    BS_VAL_002(HttpStatus.BAD_REQUEST, "허용범위 초과"),
    BS_AUTH_001(HttpStatus.UNAUTHORIZED, "인증 실패"),
    BS_AUTH_002(HttpStatus.UNAUTHORIZED, "Token 만료"),
    BS_AUTH_003(HttpStatus.FORBIDDEN, "서비스 권한 없음"),
    BS_SVC_404(HttpStatus.NOT_FOUND, "서비스/버전 없음 또는 비활성"),
    BS_DATA_000(HttpStatus.OK, "정상이나 결과 없음"),
    BS_LEG_500(HttpStatus.INTERNAL_SERVER_ERROR, "Legacy Service 처리 오류"),
    BS_SYS_500(HttpStatus.INTERNAL_SERVER_ERROR, "내부 시스템 오류"),
    BS_SYS_503(HttpStatus.SERVICE_UNAVAILABLE, "일시적 사용불가"),
    BS_SYS_504(HttpStatus.GATEWAY_TIMEOUT, "Timeout");

    private final HttpStatus httpStatus;
    private final String defaultMessage;

    ErrorCode(HttpStatus httpStatus, String defaultMessage) {
        this.httpStatus = httpStatus;
        this.defaultMessage = defaultMessage;
    }

    public String code() {
        return name().replace('_', '-');
    }

    public HttpStatus httpStatus() {
        return httpStatus;
    }

    public String defaultMessage() {
        return defaultMessage;
    }
}
