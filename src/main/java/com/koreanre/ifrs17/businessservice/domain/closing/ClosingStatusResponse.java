package com.koreanre.ifrs17.businessservice.domain.closing;

import com.koreanre.ifrs17.businessservice.domain.common.StandardStatusCode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

/** 9.1 IFRS17.CLOSING.STATUS 출력: 단계별 상태, 시작/종료시간, 진행률, 오류건수. */
@Getter
@Builder
@AllArgsConstructor
public class ClosingStatusResponse {

    private final String closingYearMonth;
    private final String closingType;
    private final StandardStatusCode overallStatus;
    @Builder.Default
    private final List<StageStatus> stages = Collections.emptyList();
    private final int totalErrorCount;

    @Getter
    @Builder
    @AllArgsConstructor
    public static class StageStatus {
        private final String stageName;
        private final StandardStatusCode status;
        private final LocalDateTime startTime;
        private final LocalDateTime endTime;
        private final int progressRate;
        private final int errorCount;
    }
}
