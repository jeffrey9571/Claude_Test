package com.koreanre.ifrs17.businessservice.legacy.adapter;

import com.koreanre.ifrs17.businessservice.core.exception.LegacyServiceException;
import com.koreanre.ifrs17.businessservice.legacy.existing.ExistingJournalService;
import com.koreanre.ifrs17.businessservice.legacy.existing.LegacyJournalStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/** Legacy Adapter: 기존 전표 Service 호출과 DTO 변환. */
@Slf4j
@Component
public class JournalStatusLegacyAdapter {

    private final ExistingJournalService existingJournalService;

    public JournalStatusLegacyAdapter(ExistingJournalService existingJournalService) {
        this.existingJournalService = existingJournalService;
    }

    public LegacyJournalStatus findStatus(String yearMonth, String journalType) {
        log.info(">>> [진입] JournalStatusLegacyAdapter.findStatus() - 기존 전표 Service 호출. yearMonth={}", yearMonth);
        try {
            return existingJournalService.findJournalStatus(yearMonth, journalType);
        } catch (Exception e) {
            throw new LegacyServiceException("전표 상태 조회 중 기존 Service 오류가 발생했습니다.", e);
        }
    }
}
