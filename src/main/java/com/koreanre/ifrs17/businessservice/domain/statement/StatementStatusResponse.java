package com.koreanre.ifrs17.businessservice.domain.statement;

import com.koreanre.ifrs17.businessservice.domain.common.StandardStatusCode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

/** 9.4 IFRS17.STATEMENT.STATUS 출력: 보고서별 산출 상태, 버전, 최종생성시각. */
@Getter
@Builder
@AllArgsConstructor
public class StatementStatusResponse {

    private final String closingYearMonth;
    private final String statementType;
    private final StandardStatusCode overallStatus;
    @Builder.Default
    private final List<StatementItemStatus> statements = Collections.emptyList();

    @Getter
    @Builder
    @AllArgsConstructor
    public static class StatementItemStatus {
        private final String statementType;
        private final StandardStatusCode status;
        private final String version;
        private final LocalDateTime lastGeneratedAt;
    }
}
