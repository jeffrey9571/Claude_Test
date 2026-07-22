package com.koreanre.ifrs17.businessservice.legacy.existing;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

/** 기존 "재무제표/리포트 Service"가 반환하는 원시 모델. */
@Getter
@Builder
@AllArgsConstructor
public class LegacyStatementStatus {

    private final String closingYearMonth;
    private final String statementType;
    @Builder.Default
    private final List<LegacyStatementItem> statements = Collections.emptyList();

    @Getter
    @Builder
    @AllArgsConstructor
    public static class LegacyStatementItem {
        private final String statementType;
        private final String statusCode;
        private final String version;
        private final LocalDateTime lastGeneratedAt;
    }
}
