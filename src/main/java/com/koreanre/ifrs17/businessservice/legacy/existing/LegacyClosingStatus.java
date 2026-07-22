package com.koreanre.ifrs17.businessservice.legacy.existing;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

/**
 * 기존(현행) 결산 상태 화면/배치 관리 Service가 반환하는 원시 모델.
 * DB-INC 착수 분석 후 "현행 매핑서"에 따라 실제 기존 Service DTO로 교체한다.
 */
@Getter
@Builder
@AllArgsConstructor
public class LegacyClosingStatus {

    private final String closingYearMonth;
    private final String closingType;
    @Builder.Default
    private final List<LegacyStage> stages = Collections.emptyList();

    @Getter
    @Builder
    @AllArgsConstructor
    public static class LegacyStage {
        private final String stageName;
        private final String statusCode;
        private final LocalDateTime startTime;
        private final LocalDateTime endTime;
        private final int progressRate;
        private final int errorCount;
    }
}
