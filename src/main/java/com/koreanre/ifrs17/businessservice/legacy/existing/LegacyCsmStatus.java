package com.koreanre.ifrs17.businessservice.legacy.existing;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

/** 기존 "CSM 산출 Service"가 반환하는 원시 모델. */
@Getter
@Builder
@AllArgsConstructor
public class LegacyCsmStatus {

    private final String closingYearMonth;
    private final String portfolioCode;
    @Builder.Default
    private final List<LegacyPortfolioCsm> portfolios = Collections.emptyList();

    @Getter
    @Builder
    @AllArgsConstructor
    public static class LegacyPortfolioCsm {
        private final String portfolioCode;
        private final String statusCode;
        private final int calculatedCount;
        private final int errorCount;
        private final LocalDateTime asOfTime;
    }
}
