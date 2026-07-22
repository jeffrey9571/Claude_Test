package com.koreanre.ifrs17.businessservice.api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/** GET /calls/{requestId} 응답: 12.3 장애 대응 순서에서 사용하는 호출 추적 정보. */
@Getter
@Builder
@AllArgsConstructor
public class CallLogEntry {
    private final String requestId;
    private final String traceId;
    private final String serviceId;
    private final String serviceVersion;
    private final String clientId;
    private final String userId;
    private final LocalDateTime requestedAt;
    private final LocalDateTime completedAt;
    private final Integer elapsedMs;
    private final String statusCode;
    private final Integer httpStatus;
    private final String errorCode;
    private final String errorId;
    private final Integer resultCount;
}
