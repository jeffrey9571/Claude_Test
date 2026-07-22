package com.koreanre.ifrs17.businessservice.persistence.mapper;

import com.koreanre.ifrs17.businessservice.persistence.model.BsClientService;
import com.koreanre.ifrs17.businessservice.persistence.model.BsClientServiceId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BsClientServiceRepository extends JpaRepository<BsClientService, BsClientServiceId> {

    Optional<BsClientService> findByClientIdAndServiceId(String clientId, String serviceId);
}
