package com.koreanre.ifrs17.businessservice.api.controller;

import com.koreanre.ifrs17.businessservice.api.dto.response.CallLogEntry;
import com.koreanre.ifrs17.businessservice.core.exception.ErrorCode;
import com.koreanre.ifrs17.businessservice.core.exception.BusinessServiceException;
import com.koreanre.ifrs17.businessservice.persistence.mapper.BsCallLogRepository;
import com.koreanre.ifrs17.businessservice.persistence.model.BsCallLog;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 5.1 URI 규칙: GET /calls/{requestId}.
 * 12.3 장애 대응 순서 1~2단계(Request ID로 호출 식별, Call Log 확인)를 지원한다.
 */
@RestController
@RequestMapping("/api/business-services/v1/calls")
public class CallLogController {

    private final BsCallLogRepository callLogRepository;

    public CallLogController(BsCallLogRepository callLogRepository) {
        this.callLogRepository = callLogRepository;
    }

    @GetMapping("/{requestId}")
    public CallLogEntry get(@PathVariable String requestId) {
        BsCallLog log = callLogRepository.findById(requestId)
                .orElseThrow(() -> new BusinessServiceException(ErrorCode.BS_SVC_404,
                        "호출 이력을 찾을 수 없습니다: " + requestId));
        return CallLogEntry.builder()
                .requestId(log.getRequestId())
                .traceId(log.getTraceId())
                .serviceId(log.getServiceId())
                .serviceVersion(log.getServiceVersion())
                .clientId(log.getClientId())
                .userId(log.getUserId())
                .requestedAt(log.getRequestedAt())
                .completedAt(log.getCompletedAt())
                .elapsedMs(log.getElapsedMs())
                .statusCode(log.getStatusCode())
                .httpStatus(log.getHttpStatus())
                .errorCode(log.getErrorCode())
                .errorId(log.getErrorId())
                .resultCount(log.getResultCount())
                .build();
    }
}
