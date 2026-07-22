package com.koreanre.ifrs17.businessservice.legacy.existing;

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
@Component
public class ExistingStatementServiceStub implements ExistingStatementService {

    private static final List<String> STATEMENT_TYPES = Arrays.asList("재무상태표", "포괄손익계산서", "보험계약부채명세서");

    @Override
    public LegacyStatementStatus findStatementStatus(String closingYearMonth, String statementType) {
        YearMonth requested;
        try {
            requested = YearMonth.parse(closingYearMonth);
        } catch (DateTimeParseException e) {
            return null;
        }
        if (requested.isAfter(YearMonth.now())) {
            return null;
        }

        LocalDateTime generatedAt = requested.atEndOfMonth().atTime(23, 30);
        List<String> types = resolveTypes(statementType);

        List<LegacyStatementStatus.LegacyStatementItem> results = types.stream()
                .map(t -> LegacyStatementStatus.LegacyStatementItem.builder()
                        .statementType(t)
                        .statusCode("COMPLETED")
                        .version("1.0")
                        .lastGeneratedAt(generatedAt)
                        .build())
                .collect(Collectors.toList());

        return LegacyStatementStatus.builder()
                .closingYearMonth(closingYearMonth)
                .statementType(statementType)
                .statements(results)
                .build();
    }

    private List<String> resolveTypes(String statementType) {
        if (!StringUtils.hasText(statementType)) {
            return STATEMENT_TYPES;
        }
        return STATEMENT_TYPES.contains(statementType) ? Collections.singletonList(statementType) : Collections.emptyList();
    }
}
