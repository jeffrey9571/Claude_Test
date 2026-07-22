package com.koreanre.ifrs17.businessservice.persistence.mapper;

import com.koreanre.ifrs17.businessservice.persistence.model.BsServiceRole;
import com.koreanre.ifrs17.businessservice.persistence.model.BsServiceRoleId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BsServiceRoleRepository extends JpaRepository<BsServiceRole, BsServiceRoleId> {

    List<BsServiceRole> findByServiceId(String serviceId);
}
