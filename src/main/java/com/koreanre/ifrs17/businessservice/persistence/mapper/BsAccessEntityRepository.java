package com.koreanre.ifrs17.businessservice.persistence.mapper;

import com.koreanre.ifrs17.businessservice.persistence.model.BsAccessEntity;
import com.koreanre.ifrs17.businessservice.persistence.model.BsAccessEntityId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BsAccessEntityRepository extends JpaRepository<BsAccessEntity, BsAccessEntityId> {
}
