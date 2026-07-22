package com.koreanre.ifrs17.businessservice.core.exception;

import java.util.Collections;

/**
 * CON-ACC-03: 사용 여부가 "미사용"인 서비스에 대한 호출 차단.
 * 표준 오류코드는 BS-SVC-404를 재사용하되 detail에 SERVICE_DISABLED를 명시한다.
 */
public class ServiceDisabledException extends BusinessServiceException {

    public ServiceDisabledException(String serviceId) {
        super(ErrorCode.BS_SVC_404, "서비스가 비활성화(미사용) 상태입니다: " + serviceId,
                Collections.singletonList("SERVICE_DISABLED"));
    }
}
