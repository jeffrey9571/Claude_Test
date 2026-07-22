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
public class ExistingJournalServiceStub implements ExistingJournalService {

    private static final List<String> JOURNAL_TYPES = Arrays.asList("보험료수익", "보험금비용", "CSM상각", "재보험");

    @Override
    public LegacyJournalStatus findJournalStatus(String closingYearMonth, String journalType) {
        log.info(">>> [진입] ExistingJournalServiceStub.findJournalStatus() - (Stub) 현행 전표 데이터 생성. yearMonth={}",
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

        LocalDateTime base = requested.atDay(28).atTime(18, 0);
        List<String> types = resolveTypes(journalType);

        List<LegacyJournalStatus.LegacyJournalType> results = types.stream()
                .map(t -> {
                    int idx = JOURNAL_TYPES.indexOf(t);
                    int created = 1000 + idx * 137;
                    boolean lastType = idx == JOURNAL_TYPES.size() - 1;
                    int posted = lastType ? created - 5 : created;
                    int error = lastType ? 5 : 0;
                    return LegacyJournalStatus.LegacyJournalType.builder()
                            .journalType(t)
                            .statusCode(error > 0 ? "PARTIAL" : "COMPLETED")
                            .createdCount(created)
                            .postedCount(posted)
                            .errorCount(error)
                            .lastProcessedAt(base.plusHours(idx))
                            .build();
                })
                .collect(Collectors.toList());

        return LegacyJournalStatus.builder()
                .closingYearMonth(closingYearMonth)
                .journalType(journalType)
                .types(results)
                .build();
    }

    /** journalType 필터가 존재 목록에 없으면 빈 결과(데이터 없음)를 반환한다. */
    private List<String> resolveTypes(String journalType) {
        if (!StringUtils.hasText(journalType)) {
            return JOURNAL_TYPES;
        }
        return JOURNAL_TYPES.contains(journalType) ? Collections.singletonList(journalType) : Collections.emptyList();
    }
}
