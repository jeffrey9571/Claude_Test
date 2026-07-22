package com.koreanre.ifrs17.businessservice.domain.expense;

import com.koreanre.ifrs17.businessservice.domain.common.StandardStatusCode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.Collections;
import java.util.List;

/** 9.3 IFRS17.EXPENSE.STATUS 출력: 사업비 구분별 적재/검증/배부 상태와 오류. */
@Getter
@Builder
@AllArgsConstructor
public class ExpenseStatusResponse {

    private final String closingYearMonth;
    private final String expenseCategory;
    private final StandardStatusCode overallStatus;
    @Builder.Default
    private final List<ExpenseCategoryStatus> categories = Collections.emptyList();
    private final int totalErrorCount;

    @Getter
    @Builder
    @AllArgsConstructor
    public static class ExpenseCategoryStatus {
        private final String expenseCategory;
        private final StandardStatusCode loadStatus;
        private final StandardStatusCode validationStatus;
        private final StandardStatusCode allocationStatus;
        private final int errorCount;
    }
}
