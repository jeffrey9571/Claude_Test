package com.koreanre.ifrs17.businessservice.legacy.adapter;

import com.koreanre.ifrs17.businessservice.core.exception.LegacyServiceException;
import com.koreanre.ifrs17.businessservice.legacy.existing.ExistingCsmService;
import com.koreanre.ifrs17.businessservice.legacy.existing.LegacyCsmStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/** Legacy Adapter: 기존 CSM 산출 Service 호출과 DTO 변환. */
@Slf4j
@Component
public class CsmStatusLegacyAdapter {

    private final ExistingCsmService existingCsmService;

    public CsmStatusLegacyAdapter(ExistingCsmService existingCsmService) {
        this.existingCsmService = existingCsmService;
    }

    public LegacyCsmStatus findStatus(String yearMonth, String portfolioCode) {
        log.info(">>> [진입] CsmStatusLegacyAdapter.findStatus() - 기존 CSM Service 호출. yearMonth={}", yearMonth);
        try {
            return existingCsmService.findCsmStatus(yearMonth, portfolioCode);
        } catch (Exception e) {
            throw new LegacyServiceException("CSM 산출 상태 조회 중 기존 Service 오류가 발생했습니다.", e);
        }
    }
}
