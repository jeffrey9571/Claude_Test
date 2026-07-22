package com.koreanre.ifrs17.businessservice.core.context;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Set;

/**
 * 4.4 표준 인터페이스 예시의 ServiceContext.
 * 표준 처리 순서(4.3) 전 구간에서 전달되는 요청 단위 컨텍스트.
 */
@Getter
@Builder(toBuilder = true)
public class ServiceContext {

    private final String requestId;
    private final String traceId;
    private final String parentRequestId;
    private final String clientId;
    private final String userId;
    private final String departmentCode;
    @Builder.Default
    private final Set<String> roles = Collections.emptySet();
    private final LocalDateTime requestedAt;
    private final String remoteIp;
    private final String serviceId;
    private final String serverInstance;

    public boolean hasAnyRole(Set<String> requiredRoles) {
        if (requiredRoles == null || requiredRoles.isEmpty()) {
            return true;
        }
        if (roles == null || roles.isEmpty()) {
            return false;
        }
        return requiredRoles.stream().anyMatch(roles::contains);
    }
}
