package com.koreanre.ifrs17.businessservice.core.executor;

import com.koreanre.ifrs17.businessservice.core.context.ServiceContext;

/**
 * 4.4 표준 인터페이스 예시: 도메인 서비스 표준 인터페이스.
 * 모든 파일럿 Business Service는 이 인터페이스를 구현한다.
 */
public interface BusinessServiceHandler<REQ, RES> {

    String serviceId();

    Class<REQ> requestType();

    void validate(ServiceContext context, REQ request);

    void authorize(ServiceContext context, REQ request);

    RES process(ServiceContext context, REQ request);
}
