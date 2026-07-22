package com.koreanre.ifrs17.businessservice.domain.expense;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** 9.3 IFRS17.EXPENSE.STATUS 입력: closingYearMonth, expenseCategory(optional). */
@Getter
@Setter
@NoArgsConstructor
public class ExpenseStatusRequest {
    private String closingYearMonth;
    private String expenseCategory;
}
