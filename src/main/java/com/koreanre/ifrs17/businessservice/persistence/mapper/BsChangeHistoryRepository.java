package com.koreanre.ifrs17.businessservice.persistence.mapper;

import com.koreanre.ifrs17.businessservice.persistence.model.BsChangeHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BsChangeHistoryRepository extends JpaRepository<BsChangeHistory, String> {
}
