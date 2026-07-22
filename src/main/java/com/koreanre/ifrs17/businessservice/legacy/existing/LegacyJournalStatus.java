package com.koreanre.ifrs17.businessservice.legacy.existing;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

/** 기존 "전표 현황 화면/전표 Service"가 반환하는 원시 모델. */
@Getter
@Builder
@AllArgsConstructor
public class LegacyJournalStatus {

    private final String closingYearMonth;
    private final String journalType;
    @Builder.Default
    private final List<LegacyJournalType> types = Collections.emptyList();

    @Getter
    @Builder
    @AllArgsConstructor
    public static class LegacyJournalType {
        private final String journalType;
        private final String statusCode;
        private final int createdCount;
        private final int postedCount;
        private final int errorCount;
        private final LocalDateTime lastProcessedAt;
    }
}
