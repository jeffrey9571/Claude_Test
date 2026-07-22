package com.koreanre.ifrs17.businessservice.core.exception;

/** 내부 시스템 오류 (HTTP 500 / BS-SYS-500). 미처리 예외를 감싸는 최종 fallback. */
public class SystemException extends BusinessServiceException {

    public SystemException(Throwable cause) {
        super(ErrorCode.BS_SYS_500, "내부 시스템 오류가 발생했습니다.", cause);
    }
}
