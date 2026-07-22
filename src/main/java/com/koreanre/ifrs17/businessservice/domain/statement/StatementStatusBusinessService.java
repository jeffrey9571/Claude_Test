package com.koreanre.ifrs17.businessservice.domain.statement;

import com.koreanre.ifrs17.businessservice.core.context.ServiceContext;
import com.koreanre.ifrs17.businessservice.core.executor.BusinessServiceHandler;
import com.koreanre.ifrs17.businessservice.core.validator.ValidationUtils;
import com.koreanre.ifrs17.businessservice.legacy.adapter.StatementStatusLegacyAdapter;
import com.koreanre.ifrs17.businessservice.legacy.existing.LegacyStatementStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/** 9.4 IFRS17.STATEMENT.STATUS: 재무제표 산출 상태 조회. */
@Slf4j
@Component("statementStatusBusinessService")
public class StatementStatusBusinessService
        implements BusinessServiceHandler<StatementStatusRequest, StatementStatusResponse> {

    private final StatementStatusLegacyAdapter adapter;

    public StatementStatusBusinessService(StatementStatusLegacyAdapter adapter) {
        this.adapter = adapter;
    }

    @Override
    public String serviceId() {
        return "IFRS17.STATEMENT.STATUS";
    }

    @Override
    public Class<StatementStatusRequest> requestType() {
        return StatementStatusRequest.class;
    }

    @Override
    public void validate(ServiceContext context, StatementStatusRequest request) {
        ValidationUtils.requireYearMonth(request.getClosingYearMonth());
    }

    @Override
    public void authorize(ServiceContext context, StatementStatusRequest request) {
        // 공통 Role 검사는 Executor 단계에서 이미 수행된다.
    }

    @Override
    public StatementStatusResponse process(ServiceContext context, StatementStatusRequest request) {
        log.info(">>> [진입] StatementStatusBusinessService.process() - 재무제표 산출상태 조회. closingYearMonth={}",
                request.getClosingYearMonth());
        LegacyStatementStatus legacy = adapter.findStatus(request.getClosingYearMonth(), request.getStatementType());
        return StatementStatusMapper.toResponse(request.getClosingYearMonth(), request.getStatementType(), legacy);
    }
}
