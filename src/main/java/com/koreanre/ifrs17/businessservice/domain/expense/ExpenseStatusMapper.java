package com.koreanre.ifrs17.businessservice.domain.expense;

import com.koreanre.ifrs17.businessservice.domain.common.StandardStatusCode;
import com.koreanre.ifrs17.businessservice.legacy.existing.LegacyExpenseStatus;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public final class ExpenseStatusMapper {

    private ExpenseStatusMapper() {
    }

    public static ExpenseStatusResponse toResponse(String closingYearMonth, String expenseCategory, LegacyExpenseStatus legacy) {
        if (legacy == null || legacy.getCategories().isEmpty()) {
            return ExpenseStatusResponse.builder()
                    .closingYearMonth(closingYearMonth)
                    .expenseCategory(expenseCategory)
                    .overallStatus(StandardStatusCode.NOT_STARTED)
                    .categories(Collections.emptyList())
                    .totalErrorCount(0)
                    .build();
        }

        List<ExpenseStatusResponse.ExpenseCategoryStatus> categories = legacy.getCategories().stream()
                .map(c -> ExpenseStatusResponse.ExpenseCategoryStatus.builder()
                        .expenseCategory(c.getExpenseCategory())
                        .loadStatus(toStatus(c.getLoadStatusCode()))
                        .validationStatus(toStatus(c.getValidationStatusCode()))
                        .allocationStatus(toStatus(c.getAllocationStatusCode()))
                        .errorCount(c.getErrorCount())
                        .build())
                .collect(Collectors.toList());

        int totalErrorCount = categories.stream().mapToInt(ExpenseStatusResponse.ExpenseCategoryStatus::getErrorCount).sum();

        return ExpenseStatusResponse.builder()
                .closingYearMonth(closingYearMonth)
                .expenseCategory(expenseCategory)
                .overallStatus(totalErrorCount > 0 ? StandardStatusCode.PARTIAL : StandardStatusCode.COMPLETED)
                .categories(categories)
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
}
