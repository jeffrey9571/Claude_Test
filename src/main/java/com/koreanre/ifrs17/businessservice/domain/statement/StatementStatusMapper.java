package com.koreanre.ifrs17.businessservice.domain.statement;

import com.koreanre.ifrs17.businessservice.domain.common.StandardStatusCode;
import com.koreanre.ifrs17.businessservice.legacy.existing.LegacyStatementStatus;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public final class StatementStatusMapper {

    private StatementStatusMapper() {
    }

    public static StatementStatusResponse toResponse(String closingYearMonth, String statementType, LegacyStatementStatus legacy) {
        if (legacy == null || legacy.getStatements().isEmpty()) {
            return StatementStatusResponse.builder()
                    .closingYearMonth(closingYearMonth)
                    .statementType(statementType)
                    .overallStatus(StandardStatusCode.NOT_STARTED)
                    .statements(Collections.emptyList())
                    .build();
        }

        List<StatementStatusResponse.StatementItemStatus> statements = legacy.getStatements().stream()
                .map(s -> StatementStatusResponse.StatementItemStatus.builder()
                        .statementType(s.getStatementType())
                        .status(toStatus(s.getStatusCode()))
                        .version(s.getVersion())
                        .lastGeneratedAt(s.getLastGeneratedAt())
                        .build())
                .collect(Collectors.toList());

        boolean allCompleted = statements.stream().allMatch(s -> s.getStatus() == StandardStatusCode.COMPLETED);

        return StatementStatusResponse.builder()
                .closingYearMonth(closingYearMonth)
                .statementType(statementType)
                .overallStatus(allCompleted ? StandardStatusCode.COMPLETED : StandardStatusCode.PARTIAL)
                .statements(statements)
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
