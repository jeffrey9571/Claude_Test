package com.koreanre.ifrs17.businessservice.domain.closing;

import com.koreanre.ifrs17.businessservice.core.context.ServiceContext;
import com.koreanre.ifrs17.businessservice.core.executor.BusinessServiceHandler;
import com.koreanre.ifrs17.businessservice.core.validator.ValidationUtils;
import com.koreanre.ifrs17.businessservice.legacy.adapter.ClosingStatusLegacyAdapter;
import com.koreanre.ifrs17.businessservice.legacy.existing.LegacyClosingStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 10.2 Reference Implementation. 9.1 IFRS17.CLOSING.STATUS: 결산 진행상태 조회.
 */
@Slf4j
@Component("closingStatusBusinessService")
public class ClosingStatusBusinessService
        implements BusinessServiceHandler<ClosingStatusRequest, ClosingStatusResponse> {

    private final ClosingStatusLegacyAdapter adapter;

    public ClosingStatusBusinessService(ClosingStatusLegacyAdapter adapter) {
        this.adapter = adapter;
    }

    @Override
    public String serviceId() {
        return "IFRS17.CLOSING.STATUS";
    }

    @Override
    public Class<ClosingStatusRequest> requestType() {
        return ClosingStatusRequest.class;
    }

    @Override
    public void validate(ServiceContext context, ClosingStatusRequest request) {
        ValidationUtils.requireYearMonth(request.getClosingYearMonth());
    }

    @Override
    public void authorize(ServiceContext context, ClosingStatusRequest request) {
        // 공통 Role 검사(AuthorizationService)는 Executor 단계에서 이미 수행된다.
        // 업무 파라미터에 따른 추가 권한 확인이 필요하면 여기에서 기존 IFRS17 권한 Service를 호출한다.
    }

    @Override
    public ClosingStatusResponse process(ServiceContext context, ClosingStatusRequest request) {
        log.info(">>> [진입] ClosingStatusBusinessService.process() - 결산 진행상태 조회. closingYearMonth={}",
                request.getClosingYearMonth());
        LegacyClosingStatus legacy = adapter.findStatus(request.getClosingYearMonth(), request.getClosingType());
        return ClosingStatusMapper.toResponse(request.getClosingYearMonth(), request.getClosingType(), legacy);
    }
}
