package com.koreanre.ifrs17.businessservice.core.security;

import com.koreanre.ifrs17.businessservice.core.context.ServiceContext;
import com.koreanre.ifrs17.businessservice.core.exception.AuthorizationException;
import com.koreanre.ifrs17.businessservice.core.metadata.ServiceMetadata;
import org.springframework.stereotype.Component;

/**
 * 6.2 권한 원칙: 서비스 권한은 BS_SERVICE_ROLE과 기존 IFRS17 권한정보를 함께 사용하며,
 * MCP/AI Agent가 권한을 최종 결정하지 않는다. 본 컴포넌트가 최종 허용/거부를 판단한다.
 */
@Component
public class AuthorizationService {

    public void authorize(ServiceContext context, ServiceMetadata metadata) {
        if (!context.hasAnyRole(metadata.getRequiredRoles())) {
            throw new AuthorizationException(
                    "해당 서비스에 대한 권한이 없습니다: " + metadata.getServiceId());
        }
    }
}
