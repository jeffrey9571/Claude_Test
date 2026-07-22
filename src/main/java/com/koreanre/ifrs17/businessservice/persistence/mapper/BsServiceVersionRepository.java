package com.koreanre.ifrs17.businessservice.persistence.mapper;

import com.koreanre.ifrs17.businessservice.persistence.model.BsServiceVersion;
import com.koreanre.ifrs17.businessservice.persistence.model.BsServiceVersionId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BsServiceVersionRepository extends JpaRepository<BsServiceVersion, BsServiceVersionId> {

    List<BsServiceVersion> findByServiceId(String serviceId);

    Optional<BsServiceVersion> findByServiceIdAndVersion(String serviceId, String version);

    /** version 미지정 시 최신 유효(effective_from 최댓값) ACTIVE 버전을 사용한다. */
    Optional<BsServiceVersion> findFirstByServiceIdAndStatusCodeOrderByEffectiveFromDesc(String serviceId, String statusCode);
}
