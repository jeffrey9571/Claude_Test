package com.koreanre.ifrs17.businessservice.api.controller;

import com.koreanre.ifrs17.businessservice.api.dto.response.CallLogEntry;
import com.koreanre.ifrs17.businessservice.core.exception.ErrorCode;
import com.koreanre.ifrs17.businessservice.core.exception.BusinessServiceException;
import com.koreanre.ifrs17.businessservice.persistence.mapper.BsCallLogRepository;
import com.koreanre.ifrs17.businessservice.persistence.model.BsCallLog;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 5.1 URI 규칙: GET /calls/{requestId}.
 * 12.3 장애 대응 순서 1~2단계(Request ID로 호출 식별, Call Log 확인)를 지원한다.
 */
@Slf4j
@RestController
@RequestMapping("/api/business-services/v1/calls")
public class CallLogController {

    private final BsCallLogRepository callLogRepository;

    public CallLogController(BsCallLogRepository callLogRepository) {
        this.callLogRepository = callLogRepository;
    }

    @GetMapping("/{requestId}")
    public CallLogEntry get(@PathVariable String requestId) {
        log.info(">>> [진입] CallLogController.get() - 호출이력 추적. requestId={}", requestId);
        BsCallLog callLog = callLogRepository.findById(requestId)
                .orElseThrow(() -> new BusinessServiceException(ErrorCode.BS_SVC_404,
                        "호출 이력을 찾을 수 없습니다: " + requestId));
        return CallLogEntry.builder()
                .requestId(callLog.getRequestId())
                .traceId(callLog.getTraceId())
                .serviceId(callLog.getServiceId())
                .serviceVersion(callLog.getServiceVersion())
                .clientId(callLog.getClientId())
                .userId(callLog.getUserId())
                .requestedAt(callLog.getRequestedAt())
                .completedAt(callLog.getCompletedAt())
                .elapsedMs(callLog.getElapsedMs())
                .statusCode(callLog.getStatusCode())
                .httpStatus(callLog.getHttpStatus())
                .errorCode(callLog.getErrorCode())
                .errorId(callLog.getErrorId())
                .resultCount(callLog.getResultCount())
                .build();
    }
}
