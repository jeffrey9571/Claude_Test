package com.koreanre.ifrs17.businessservice.persistence.mapper;

import com.koreanre.ifrs17.businessservice.persistence.model.BsService;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BsServiceRepository extends JpaRepository<BsService, String> {
}
