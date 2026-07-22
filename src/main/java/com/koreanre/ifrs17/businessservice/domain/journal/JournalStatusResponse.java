package com.koreanre.ifrs17.businessservice.domain.journal;

import com.koreanre.ifrs17.businessservice.domain.common.StandardStatusCode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

/** 9.2 IFRS17.JOURNAL.STATUS 출력: 전표 유형별 생성건수, 반영건수, 오류건수, 최종처리시간. */
@Getter
@Builder
@AllArgsConstructor
public class JournalStatusResponse {

    private final String closingYearMonth;
    private final String journalType;
    private final StandardStatusCode overallStatus;
    @Builder.Default
    private final List<JournalTypeStatus> journalTypes = Collections.emptyList();
    private final int totalErrorCount;

    @Getter
    @Builder
    @AllArgsConstructor
    public static class JournalTypeStatus {
        private final String journalType;
        private final StandardStatusCode status;
        private final int createdCount;
        private final int postedCount;
        private final int errorCount;
        private final LocalDateTime lastProcessedAt;
    }
}
