package com.koreanre.ifrs17.businessservice.legacy.adapter;

import com.koreanre.ifrs17.businessservice.core.exception.LegacyServiceException;
import com.koreanre.ifrs17.businessservice.legacy.existing.ExistingExpenseService;
import com.koreanre.ifrs17.businessservice.legacy.existing.LegacyExpenseStatus;
import org.springframework.stereotype.Component;

/** Legacy Adapter: 기존 사업비 처리 Service 호출과 DTO 변환. */
@Component
public class ExpenseStatusLegacyAdapter {

    private final ExistingExpenseService existingExpenseService;

    public ExpenseStatusLegacyAdapter(ExistingExpenseService existingExpenseService) {
        this.existingExpenseService = existingExpenseService;
    }

    public LegacyExpenseStatus findStatus(String yearMonth, String expenseCategory) {
        try {
            return existingExpenseService.findExpenseStatus(yearMonth, expenseCategory);
        } catch (Exception e) {
            throw new LegacyServiceException("사업비 처리 상태 조회 중 기존 Service 오류가 발생했습니다.", e);
        }
    }
}
