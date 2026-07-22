package com.koreanre.ifrs17.businessservice.legacy.existing;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.Collections;
import java.util.List;

/** 기존 "사업비 처리 Service"가 반환하는 원시 모델. */
@Getter
@Builder
@AllArgsConstructor
public class LegacyExpenseStatus {

    private final String closingYearMonth;
    private final String expenseCategory;
    @Builder.Default
    private final List<LegacyExpenseCategory> categories = Collections.emptyList();

    @Getter
    @Builder
    @AllArgsConstructor
    public static class LegacyExpenseCategory {
        private final String expenseCategory;
        private final String loadStatusCode;
        private final String validationStatusCode;
        private final String allocationStatusCode;
        private final int errorCount;
    }
}
