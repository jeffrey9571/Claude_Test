package com.koreanre.ifrs17.businessservice.core.security;

import com.koreanre.ifrs17.businessservice.core.exception.AuthenticationException;
import com.koreanre.ifrs17.businessservice.core.exception.AuthorizationException;
import com.koreanre.ifrs17.businessservice.persistence.mapper.BsClientRepository;
import com.koreanre.ifrs17.businessservice.persistence.mapper.BsClientServiceRepository;
import com.koreanre.ifrs17.businessservice.persistence.model.BsClient;
import com.koreanre.ifrs17.businessservice.persistence.model.BsClientService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 4.3 표준 처리 순서 3단계 "호출 Client 검증".
 * 3.3 네트워크 및 호출 원칙: MCP/Portal 호출 Client ID는 허용목록으로 관리한다.
 */
@Slf4j
@Component
public class ClientValidator {

    private final BsClientRepository clientRepository;
    private final BsClientServiceRepository clientServiceRepository;

    public ClientValidator(BsClientRepository clientRepository, BsClientServiceRepository clientServiceRepository) {
        this.clientRepository = clientRepository;
        this.clientServiceRepository = clientServiceRepository;
    }

    public void validate(String clientId, String serviceId) {
        log.info(">>> [진입] ClientValidator.validate() - Client 허용목록 검증. clientId={}, serviceId={}", clientId, serviceId);
        BsClient client = clientRepository.findById(clientId)
                .orElseThrow(() -> new AuthenticationException("등록되지 않은 호출 Client입니다: " + clientId));
        if (!client.isActive()) {
            throw new AuthenticationException("비활성화된 호출 Client입니다: " + clientId);
        }

        BsClientService allowance = clientServiceRepository.findByClientIdAndServiceId(clientId, serviceId)
                .orElseThrow(() -> new AuthorizationException("Client에 허용되지 않은 서비스입니다: " + clientId + " -> " + serviceId));
        if (!allowance.isActive()) {
            throw new AuthorizationException("Client의 서비스 호출 허용이 비활성화되었습니다: " + clientId + " -> " + serviceId);
        }
    }
}
