package com.koreanre.ifrs17.businessservice.domain.csm;

import com.koreanre.ifrs17.businessservice.core.context.ServiceContext;
import com.koreanre.ifrs17.businessservice.core.executor.BusinessServiceHandler;
import com.koreanre.ifrs17.businessservice.core.validator.ValidationUtils;
import com.koreanre.ifrs17.businessservice.legacy.adapter.CsmStatusLegacyAdapter;
import com.koreanre.ifrs17.businessservice.legacy.existing.LegacyCsmStatus;
import org.springframework.stereotype.Component;

/** 9.5 IFRS17.CSM.STATUS: CSM 산출 상태 조회. */
@Component("csmStatusBusinessService")
public class CsmStatusBusinessService
        implements BusinessServiceHandler<CsmStatusRequest, CsmStatusResponse> {

    private final CsmStatusLegacyAdapter adapter;

    public CsmStatusBusinessService(CsmStatusLegacyAdapter adapter) {
        this.adapter = adapter;
    }

    @Override
    public String serviceId() {
        return "IFRS17.CSM.STATUS";
    }

    @Override
    public Class<CsmStatusRequest> requestType() {
        return CsmStatusRequest.class;
    }

    @Override
    public void validate(ServiceContext context, CsmStatusRequest request) {
        ValidationUtils.requireYearMonth(request.getClosingYearMonth());
    }

    @Override
    public void authorize(ServiceContext context, CsmStatusRequest request) {
        // 공통 Role 검사는 Executor 단계에서 이미 수행된다.
    }

    @Override
    public CsmStatusResponse process(ServiceContext context, CsmStatusRequest request) {
        LegacyCsmStatus legacy = adapter.findStatus(request.getClosingYearMonth(), request.getPortfolioCode());
        return CsmStatusMapper.toResponse(request.getClosingYearMonth(), request.getPortfolioCode(), legacy);
    }
}
