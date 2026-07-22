package com.koreanre.ifrs17.businessservice.legacy.existing;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 착수 전 개발/테스트용 Stub. 미래 월(아직 결산이 시작되지 않은 월)은 데이터 없음을 반환하여
 * 9.1 "데이터 없음은 오류가 아니라 SUCCESS + 빈 결과" 시나리오를 재현한다.
 * 실제 배포 시 DB-INC의 현행 Service 구현으로 교체(Bean 대체)한다.
 */
@Slf4j
@Component
public class ExistingClosingServiceStub implements ExistingClosingService {

    private static final List<String> STAGE_NAMES = Arrays.asList(
            "데이터적재", "보험부채평가", "손익인식", "재무제표생성", "결산확정");

    @Override
    public LegacyClosingStatus findClosingStatus(String closingYearMonth, String closingType) {
        log.info(">>> [진입] ExistingClosingServiceStub.findClosingStatus() - (Stub) 현행 결산 데이터 생성. yearMonth={}",
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

        boolean fullyClosed = requested.isBefore(YearMonth.now());
        List<LegacyClosingStatus.LegacyStage> stages = buildStages(requested, fullyClosed);

        return LegacyClosingStatus.builder()
                .closingYearMonth(closingYearMonth)
                .closingType(closingType)
                .stages(stages)
                .build();
    }

    private List<LegacyClosingStatus.LegacyStage> buildStages(YearMonth requested, boolean fullyClosed) {
        LocalDateTime base = requested.atDay(28).atTime(9, 0);
        int total = STAGE_NAMES.size();
        int completedCount = fullyClosed ? total : Math.max(1, total - 2);

        return java.util.stream.IntStream.range(0, total).mapToObj(i -> {
            String stageName = STAGE_NAMES.get(i);
            LocalDateTime start = base.plusHours(i * 2L);
            if (i < completedCount) {
                return LegacyClosingStatus.LegacyStage.builder()
                        .stageName(stageName)
                        .statusCode("COMPLETED")
                        .startTime(start)
                        .endTime(start.plusHours(2))
                        .progressRate(100)
                        .errorCount(0)
                        .build();
            } else if (i == completedCount) {
                return LegacyClosingStatus.LegacyStage.builder()
                        .stageName(stageName)
                        .statusCode("RUNNING")
                        .startTime(start)
                        .endTime(null)
                        .progressRate(45)
                        .errorCount(0)
                        .build();
            }
            return LegacyClosingStatus.LegacyStage.builder()
                    .stageName(stageName)
                    .statusCode("NOT_STARTED")
                    .startTime(null)
                    .endTime(null)
                    .progressRate(0)
                    .errorCount(0)
                    .build();
        }).collect(Collectors.toList());
    }
}
