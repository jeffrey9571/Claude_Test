package com.koreanre.ifrs17.businessservice.core.metadata;

import com.koreanre.ifrs17.businessservice.persistence.mapper.BsServiceRepository;
import com.koreanre.ifrs17.businessservice.persistence.mapper.BsServiceRoleRepository;
import com.koreanre.ifrs17.businessservice.persistence.mapper.BsServiceVersionRepository;
import com.koreanre.ifrs17.businessservice.persistence.model.BsService;
import com.koreanre.ifrs17.businessservice.persistence.model.BsServiceVersion;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 4.2 ServiceMetadataRepository: 서비스 버전·상태·권한·Timeout 조회.
 * 활성(active_yn=Y, status_code=ACTIVE) Catalog 항목만 실행 대상으로 반환한다.
 */
@Component
public class ServiceMetadataRepository {

    private final BsServiceRepository serviceRepository;
    private final BsServiceVersionRepository versionRepository;
    private final BsServiceRoleRepository roleRepository;

    public ServiceMetadataRepository(BsServiceRepository serviceRepository,
            BsServiceVersionRepository versionRepository,
            BsServiceRoleRepository roleRepository) {
        this.serviceRepository = serviceRepository;
        this.versionRepository = versionRepository;
        this.roleRepository = roleRepository;
    }

    /** version이 없으면 최신 ACTIVE 버전을 사용한다. */
    public Optional<ServiceMetadata> findActive(String serviceId, String version) {
        Optional<BsService> serviceOpt = serviceRepository.findById(serviceId)
                .filter(BsService::isActive);
        if (!serviceOpt.isPresent()) {
            return Optional.empty();
        }

        Optional<BsServiceVersion> versionOpt = StringUtils.hasText(version)
                ? versionRepository.findByServiceIdAndVersion(serviceId, version)
                : versionRepository.findFirstByServiceIdAndStatusCodeOrderByEffectiveFromDesc(serviceId, "ACTIVE");

        Optional<BsServiceVersion> activeVersion = versionOpt.filter(BsServiceVersion::isActive);
        if (!activeVersion.isPresent()) {
            return Optional.empty();
        }

        BsService service = serviceOpt.get();
        BsServiceVersion serviceVersion = activeVersion.get();
        Set<String> roles = roleRepository.findByServiceId(serviceId).stream()
                .map(r -> r.getRoleCode())
                .collect(Collectors.toSet());

        return Optional.of(ServiceMetadata.builder()
                .serviceId(service.getServiceId())
                .serviceName(service.getServiceName())
                .serviceType(service.getServiceType())
                .sourceSystem(service.getSourceSystem())
                .version(serviceVersion.getVersion())
                .implementationBean(serviceVersion.getImplementationBean())
                .timeoutMs(serviceVersion.getTimeoutMs())
                .requiredRoles(roles)
                .build());
    }
}
