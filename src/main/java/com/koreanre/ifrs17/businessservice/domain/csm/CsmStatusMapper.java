package com.koreanre.ifrs17.businessservice.domain.csm;

import com.koreanre.ifrs17.businessservice.domain.common.StandardStatusCode;
import com.koreanre.ifrs17.businessservice.legacy.existing.LegacyCsmStatus;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public final class CsmStatusMapper {

    private CsmStatusMapper() {
    }

    public static CsmStatusResponse toResponse(String closingYearMonth, String portfolioCode, LegacyCsmStatus legacy) {
        if (legacy == null || legacy.getPortfolios().isEmpty()) {
            return CsmStatusResponse.builder()
                    .closingYearMonth(closingYearMonth)
                    .portfolioCode(portfolioCode)
                    .overallStatus(StandardStatusCode.NOT_STARTED)
                    .portfolios(Collections.emptyList())
                    .totalErrorCount(0)
                    .build();
        }

        List<CsmStatusResponse.PortfolioCsmStatus> portfolios = legacy.getPortfolios().stream()
                .map(p -> CsmStatusResponse.PortfolioCsmStatus.builder()
                        .portfolioCode(p.getPortfolioCode())
                        .status(toStatus(p.getStatusCode()))
                        .calculatedCount(p.getCalculatedCount())
                        .errorCount(p.getErrorCount())
                        .asOfTime(p.getAsOfTime())
                        .build())
                .collect(Collectors.toList());

        int totalErrorCount = portfolios.stream().mapToInt(CsmStatusResponse.PortfolioCsmStatus::getErrorCount).sum();
        boolean anyRunning = portfolios.stream().anyMatch(p -> p.getStatus() == StandardStatusCode.RUNNING);
        boolean allCompleted = portfolios.stream().allMatch(p -> p.getStatus() == StandardStatusCode.COMPLETED);

        StandardStatusCode overall;
        if (totalErrorCount > 0) {
            overall = StandardStatusCode.PARTIAL;
        } else if (allCompleted) {
            overall = StandardStatusCode.COMPLETED;
        } else if (anyRunning) {
            overall = StandardStatusCode.RUNNING;
        } else {
            overall = StandardStatusCode.PARTIAL;
        }

        return CsmStatusResponse.builder()
                .closingYearMonth(closingYearMonth)
                .portfolioCode(portfolioCode)
                .overallStatus(overall)
                .portfolios(portfolios)
                .totalErrorCount(totalErrorCount)
                .build();
    }

    private static StandardStatusCode toStatus(String statusCode) {
        try {
            return StandardStatusCode.valueOf(statusCode);
        } catch (IllegalArgumentException | NullPointerException e) {
            return StandardStatusCode.UNKNOWN;
        }
    }
}
