package com.koreanre.ifrs17.businessservice.legacy.existing;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/** 개발/테스트용 Stub. 운영 반영 전 DB-INC의 현행 Service 구현으로 교체(Bean 대체)한다. */
@Slf4j
@Component
public class ExistingCsmServiceStub implements ExistingCsmService {

    private static final List<String> PORTFOLIOS = Arrays.asList("PF-LIFE-01", "PF-LIFE-02", "PF-HEALTH-01");

    @Override
    public LegacyCsmStatus findCsmStatus(String closingYearMonth, String portfolioCode) {
        log.info(">>> [진입] ExistingCsmServiceStub.findCsmStatus() - (Stub) 현행 CSM 데이터 생성. yearMonth={}",
                closingYearMonth);
        YearMonth requested;
        try {
            requested = YearMonth.parse(closingYearMonth);
        } catch (DateTimeParseException e) {
            return null;
        }
        if (requested.isAfter(YearMonth.now())) {
            return null;
        }

        LocalDateTime asOf = requested.atEndOfMonth().atTime(20, 0);
        List<String> portfolios = resolvePortfolios(portfolioCode);

        List<LegacyCsmStatus.LegacyPortfolioCsm> results = portfolios.stream()
                .map(p -> {
                    int idx = PORTFOLIOS.indexOf(p);
                    boolean lastPortfolio = idx == PORTFOLIOS.size() - 1;
                    return LegacyCsmStatus.LegacyPortfolioCsm.builder()
                            .portfolioCode(p)
                            .statusCode(lastPortfolio ? "RUNNING" : "COMPLETED")
                            .calculatedCount(lastPortfolio ? 0 : 500 + idx * 42)
                            .errorCount(0)
                            .asOfTime(asOf)
                            .build();
                })
                .collect(Collectors.toList());

        return LegacyCsmStatus.builder()
                .closingYearMonth(closingYearMonth)
                .portfolioCode(portfolioCode)
                .portfolios(results)
                .build();
    }

    private List<String> resolvePortfolios(String portfolioCode) {
        if (!StringUtils.hasText(portfolioCode)) {
            return PORTFOLIOS;
        }
        return PORTFOLIOS.contains(portfolioCode) ? Collections.singletonList(portfolioCode) : Collections.emptyList();
    }
}
