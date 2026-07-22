package com.koreanre.ifrs17.businessservice.legacy.adapter;

import com.koreanre.ifrs17.businessservice.core.exception.LegacyServiceException;
import com.koreanre.ifrs17.businessservice.legacy.existing.ExistingStatementService;
import com.koreanre.ifrs17.businessservice.legacy.existing.LegacyStatementStatus;
import org.springframework.stereotype.Component;

/** Legacy Adapter: 기존 재무제표/리포트 Service 호출과 DTO 변환. */
@Component
public class StatementStatusLegacyAdapter {

    private final ExistingStatementService existingStatementService;

    public StatementStatusLegacyAdapter(ExistingStatementService existingStatementService) {
        this.existingStatementService = existingStatementService;
    }

    public LegacyStatementStatus findStatus(String yearMonth, String statementType) {
        try {
            return existingStatementService.findStatementStatus(yearMonth, statementType);
        } catch (Exception e) {
            throw new LegacyServiceException("재무제표 산출 상태 조회 중 기존 Service 오류가 발생했습니다.", e);
        }
    }
}
