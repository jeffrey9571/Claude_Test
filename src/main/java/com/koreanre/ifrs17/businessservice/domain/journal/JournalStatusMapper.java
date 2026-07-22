package com.koreanre.ifrs17.businessservice.domain.journal;

import com.koreanre.ifrs17.businessservice.domain.common.StandardStatusCode;
import com.koreanre.ifrs17.businessservice.legacy.existing.LegacyJournalStatus;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public final class JournalStatusMapper {

    private JournalStatusMapper() {
    }

    public static JournalStatusResponse toResponse(String closingYearMonth, String journalType, LegacyJournalStatus legacy) {
        if (legacy == null || legacy.getTypes().isEmpty()) {
            return JournalStatusResponse.builder()
                    .closingYearMonth(closingYearMonth)
                    .journalType(journalType)
                    .overallStatus(StandardStatusCode.NOT_STARTED)
                    .journalTypes(Collections.emptyList())
                    .totalErrorCount(0)
                    .build();
        }

        List<JournalStatusResponse.JournalTypeStatus> types = legacy.getTypes().stream()
                .map(t -> JournalStatusResponse.JournalTypeStatus.builder()
                        .journalType(t.getJournalType())
                        .status(toStatus(t.getStatusCode()))
                        .createdCount(t.getCreatedCount())
                        .postedCount(t.getPostedCount())
                        .errorCount(t.getErrorCount())
                        .lastProcessedAt(t.getLastProcessedAt())
                        .build())
                .collect(Collectors.toList());

        int totalErrorCount = types.stream().mapToInt(JournalStatusResponse.JournalTypeStatus::getErrorCount).sum();
        boolean anyError = totalErrorCount > 0;

        return JournalStatusResponse.builder()
                .closingYearMonth(closingYearMonth)
                .journalType(journalType)
                .overallStatus(anyError ? StandardStatusCode.PARTIAL : StandardStatusCode.COMPLETED)
                .journalTypes(types)
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
