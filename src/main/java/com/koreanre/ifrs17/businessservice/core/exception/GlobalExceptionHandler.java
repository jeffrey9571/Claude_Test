package com.koreanre.ifrs17.businessservice.core.exception;

import com.koreanre.ifrs17.businessservice.api.dto.response.ErrorDetail;
import com.koreanre.ifrs17.businessservice.api.dto.response.StandardResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Collections;
import java.util.UUID;

/**
 * BusinessServiceController(/execute)는 BusinessServiceExecutor 내부에서 오류를
 * 표준 Response로 변환하므로 이 Handler를 거치지 않는다. Catalog/CallLog/Console 등
 * 그 외 API의 예외를 5.5 표준 Error Response 형식으로 통일한다.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(BusinessServiceException.class)
    public ResponseEntity<StandardResponse<?>> handleBusinessServiceException(BusinessServiceException ex) {
        String errorId = "ERR-" + UUID.randomUUID().toString().replace("-", "").substring(0, 14).toUpperCase();
        ErrorDetail detail = ErrorDetail.of(ex.errorCode().code(), ex.getMessage(), errorId, ex.details());
        return ResponseEntity.status(ex.errorCode().httpStatus())
                .body(StandardResponse.error(null, null, detail));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<StandardResponse<?>> handleUnexpected(Exception ex) {
        log.error("처리되지 않은 오류가 발생했습니다.", ex);
        String errorId = "ERR-" + UUID.randomUUID().toString().replace("-", "").substring(0, 14).toUpperCase();
        ErrorDetail detail = ErrorDetail.of(ErrorCode.BS_SYS_500.code(), ErrorCode.BS_SYS_500.defaultMessage(),
                errorId, Collections.emptyList());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(StandardResponse.error(null, null, detail));
    }
}
