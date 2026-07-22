package com.koreanre.ifrs17.businessservice.persistence.mapper;

import com.koreanre.ifrs17.businessservice.persistence.model.BsClient;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BsClientRepository extends JpaRepository<BsClient, String> {
}
