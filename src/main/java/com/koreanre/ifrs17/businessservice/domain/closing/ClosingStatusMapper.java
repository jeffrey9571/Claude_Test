package com.koreanre.ifrs17.businessservice.domain.closing;

import com.koreanre.ifrs17.businessservice.domain.common.StandardStatusCode;
import com.koreanre.ifrs17.businessservice.legacy.existing.LegacyClosingStatus;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/** 결과 DTO를 표준 JSON으로 변환(4.3 11단계)하는 Mapper. */
public final class ClosingStatusMapper {

    private ClosingStatusMapper() {
    }

    public static ClosingStatusResponse toResponse(String closingYearMonth, String closingType, LegacyClosingStatus legacy) {
        if (legacy == null || legacy.getStages().isEmpty()) {
            return ClosingStatusResponse.builder()
                    .closingYearMonth(closingYearMonth)
                    .closingType(closingType)
                    .overallStatus(StandardStatusCode.NOT_STARTED)
                    .stages(Collections.emptyList())
                    .totalErrorCount(0)
                    .build();
        }

        List<ClosingStatusResponse.StageStatus> stages = legacy.getStages().stream()
                .map(s -> ClosingStatusResponse.StageStatus.builder()
                        .stageName(s.getStageName())
                        .status(toStatus(s.getStatusCode()))
                        .startTime(s.getStartTime())
                        .endTime(s.getEndTime())
                        .progressRate(s.getProgressRate())
                        .errorCount(s.getErrorCount())
                        .build())
                .collect(Collectors.toList());

        int totalErrorCount = stages.stream().mapToInt(ClosingStatusResponse.StageStatus::getErrorCount).sum();

        return ClosingStatusResponse.builder()
                .closingYearMonth(closingYearMonth)
                .closingType(closingType)
                .overallStatus(resolveOverallStatus(stages))
                .stages(stages)
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

    private static StandardStatusCode resolveOverallStatus(List<ClosingStatusResponse.StageStatus> stages) {
        boolean anyFailed = stages.stream().anyMatch(s -> s.getStatus() == StandardStatusCode.FAILED);
        boolean allCompleted = stages.stream().allMatch(s -> s.getStatus() == StandardStatusCode.COMPLETED);
        boolean noneStarted = stages.stream().allMatch(s -> s.getStatus() == StandardStatusCode.NOT_STARTED);
        boolean anyRunning = stages.stream().anyMatch(s -> s.getStatus() == StandardStatusCode.RUNNING);

        if (anyFailed) {
            return allCompleted ? StandardStatusCode.PARTIAL : StandardStatusCode.FAILED;
        }
        if (allCompleted) {
            return StandardStatusCode.COMPLETED;
        }
        if (noneStarted) {
            return StandardStatusCode.NOT_STARTED;
        }
        if (anyRunning) {
            return StandardStatusCode.RUNNING;
        }
        return StandardStatusCode.PARTIAL;
    }
}
