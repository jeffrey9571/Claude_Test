package com.koreanre.ifrs17.businessservice.persistence.mapper;

import com.koreanre.ifrs17.businessservice.persistence.model.BsCallLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BsCallLogRepository extends JpaRepository<BsCallLog, String> {

    List<BsCallLog> findTop50ByServiceIdOrderByRequestedAtDesc(String serviceId);
}
