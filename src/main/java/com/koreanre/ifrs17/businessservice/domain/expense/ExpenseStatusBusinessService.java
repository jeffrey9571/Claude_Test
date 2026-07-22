package com.koreanre.ifrs17.businessservice.domain.expense;

import com.koreanre.ifrs17.businessservice.core.context.ServiceContext;
import com.koreanre.ifrs17.businessservice.core.executor.BusinessServiceHandler;
import com.koreanre.ifrs17.businessservice.core.validator.ValidationUtils;
import com.koreanre.ifrs17.businessservice.legacy.adapter.ExpenseStatusLegacyAdapter;
import com.koreanre.ifrs17.businessservice.legacy.existing.LegacyExpenseStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/** 9.3 IFRS17.EXPENSE.STATUS: 사업비 처리 상태 조회. */
@Slf4j
@Component("expenseStatusBusinessService")
public class ExpenseStatusBusinessService
        implements BusinessServiceHandler<ExpenseStatusRequest, ExpenseStatusResponse> {

    private final ExpenseStatusLegacyAdapter adapter;

    public ExpenseStatusBusinessService(ExpenseStatusLegacyAdapter adapter) {
        this.adapter = adapter;
    }

    @Override
    public String serviceId() {
        return "IFRS17.EXPENSE.STATUS";
    }

    @Override
    public Class<ExpenseStatusRequest> requestType() {
        return ExpenseStatusRequest.class;
    }

    @Override
    public void validate(ServiceContext context, ExpenseStatusRequest request) {
        ValidationUtils.requireYearMonth(request.getClosingYearMonth());
    }

    @Override
    public void authorize(ServiceContext context, ExpenseStatusRequest request) {
        // 공통 Role 검사는 Executor 단계에서 이미 수행된다.
    }

    @Override
    public ExpenseStatusResponse process(ServiceContext context, ExpenseStatusRequest request) {
        log.info(">>> [진입] ExpenseStatusBusinessService.process() - 사업비 처리상태 조회. closingYearMonth={}",
                request.getClosingYearMonth());
        LegacyExpenseStatus legacy = adapter.findStatus(request.getClosingYearMonth(), request.getExpenseCategory());
        return ExpenseStatusMapper.toResponse(request.getClosingYearMonth(), request.getExpenseCategory(), legacy);
    }
}
