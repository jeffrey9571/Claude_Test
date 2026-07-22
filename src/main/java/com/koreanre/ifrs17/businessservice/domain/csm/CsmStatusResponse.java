package com.koreanre.ifrs17.businessservice.domain.csm;

import com.koreanre.ifrs17.businessservice.domain.common.StandardStatusCode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

/** 9.5 IFRS17.CSM.STATUS 출력: 포트폴리오별 CSM 산출 상태, 건수, 오류, 기준시각. */
@Getter
@Builder
@AllArgsConstructor
public class CsmStatusResponse {

    private final String closingYearMonth;
    private final String portfolioCode;
    private final StandardStatusCode overallStatus;
    @Builder.Default
    private final List<PortfolioCsmStatus> portfolios = Collections.emptyList();
    private final int totalErrorCount;

    @Getter
    @Builder
    @AllArgsConstructor
    public static class PortfolioCsmStatus {
        private final String portfolioCode;
        private final StandardStatusCode status;
        private final int calculatedCount;
        private final int errorCount;
        private final LocalDateTime asOfTime;
    }
}
