package com.koreanre.ifrs17.businessservice.legacy.adapter;

import com.koreanre.ifrs17.businessservice.core.exception.LegacyServiceException;
import com.koreanre.ifrs17.businessservice.legacy.existing.ExistingClosingService;
import com.koreanre.ifrs17.businessservice.legacy.existing.LegacyClosingStatus;
import org.springframework.stereotype.Component;

/** 10.3 Legacy Adapter: 기존 Service 호출과 DTO 변환. */
@Component
public class ClosingStatusLegacyAdapter {

    private final ExistingClosingService existingClosingService;

    public ClosingStatusLegacyAdapter(ExistingClosingService existingClosingService) {
        this.existingClosingService = existingClosingService;
    }

    public LegacyClosingStatus findStatus(String yearMonth, String closingType) {
        try {
            return existingClosingService.findClosingStatus(yearMonth, closingType);
        } catch (Exception e) {
            throw new LegacyServiceException("결산 상태 조회 중 기존 Service 오류가 발생했습니다.", e);
        }
    }
}
