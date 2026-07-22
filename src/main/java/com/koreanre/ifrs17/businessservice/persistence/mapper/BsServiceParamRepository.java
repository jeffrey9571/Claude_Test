package com.koreanre.ifrs17.businessservice.persistence.mapper;

import com.koreanre.ifrs17.businessservice.persistence.model.BsServiceParam;
import com.koreanre.ifrs17.businessservice.persistence.model.BsServiceParamId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BsServiceParamRepository extends JpaRepository<BsServiceParam, BsServiceParamId> {

    List<BsServiceParam> findByServiceIdAndVersionOrderByDisplayOrder(String serviceId, String version);
}
