package com.koreanre.ifrs17.businessservice.domain.journal;

import com.koreanre.ifrs17.businessservice.core.context.ServiceContext;
import com.koreanre.ifrs17.businessservice.core.executor.BusinessServiceHandler;
import com.koreanre.ifrs17.businessservice.core.validator.ValidationUtils;
import com.koreanre.ifrs17.businessservice.legacy.adapter.JournalStatusLegacyAdapter;
import com.koreanre.ifrs17.businessservice.legacy.existing.LegacyJournalStatus;
import org.springframework.stereotype.Component;

/** 9.2 IFRS17.JOURNAL.STATUS: 전표 생성·반영 상태 조회. */
@Component("journalStatusBusinessService")
public class JournalStatusBusinessService
        implements BusinessServiceHandler<JournalStatusRequest, JournalStatusResponse> {

    private final JournalStatusLegacyAdapter adapter;

    public JournalStatusBusinessService(JournalStatusLegacyAdapter adapter) {
        this.adapter = adapter;
    }

    @Override
    public String serviceId() {
        return "IFRS17.JOURNAL.STATUS";
    }

    @Override
    public Class<JournalStatusRequest> requestType() {
        return JournalStatusRequest.class;
    }

    @Override
    public void validate(ServiceContext context, JournalStatusRequest request) {
        ValidationUtils.requireYearMonth(request.getClosingYearMonth());
    }

    @Override
    public void authorize(ServiceContext context, JournalStatusRequest request) {
        // 공통 Role 검사는 Executor 단계에서 이미 수행된다.
    }

    @Override
    public JournalStatusResponse process(ServiceContext context, JournalStatusRequest request) {
        LegacyJournalStatus legacy = adapter.findStatus(request.getClosingYearMonth(), request.getJournalType());
        return JournalStatusMapper.toResponse(request.getClosingYearMonth(), request.getJournalType(), legacy);
    }
}
