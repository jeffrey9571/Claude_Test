package com.koreanre.ifrs17.businessservice.legacy.existing;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/** 개발/테스트용 Stub. 운영 반영 전 DB-INC의 현행 Service 구현으로 교체(Bean 대체)한다. */
@Component
public class ExistingExpenseServiceStub implements ExistingExpenseService {

    private static final List<String> CATEGORIES = Arrays.asList("신계약비", "유지비", "수금비", "기타사업비");

    @Override
    public LegacyExpenseStatus findExpenseStatus(String closingYearMonth, String expenseCategory) {
        YearMonth requested;
        try {
            requested = YearMonth.parse(closingYearMonth);
        } catch (DateTimeParseException e) {
            return null;
        }
        if (requested.isAfter(YearMonth.now())) {
            return null;
        }

        List<String> categories = resolveCategories(expenseCategory);
        List<LegacyExpenseStatus.LegacyExpenseCategory> results = categories.stream()
                .map(c -> {
                    int idx = CATEGORIES.indexOf(c);
                    boolean lastCategory = idx == CATEGORIES.size() - 1;
                    return LegacyExpenseStatus.LegacyExpenseCategory.builder()
                            .expenseCategory(c)
                            .loadStatusCode("COMPLETED")
                            .validationStatusCode(lastCategory ? "PARTIAL" : "COMPLETED")
                            .allocationStatusCode(lastCategory ? "NOT_STARTED" : "COMPLETED")
                            .errorCount(lastCategory ? 3 : 0)
                            .build();
                })
                .collect(Collectors.toList());

        return LegacyExpenseStatus.builder()
                .closingYearMonth(closingYearMonth)
                .expenseCategory(expenseCategory)
                .categories(results)
                .build();
    }

    private List<String> resolveCategories(String expenseCategory) {
        if (!StringUtils.hasText(expenseCategory)) {
            return CATEGORIES;
        }
        return CATEGORIES.contains(expenseCategory) ? Collections.singletonList(expenseCategory) : Collections.emptyList();
    }
}
