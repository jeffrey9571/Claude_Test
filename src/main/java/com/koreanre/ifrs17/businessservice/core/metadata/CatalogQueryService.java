package com.koreanre.ifrs17.businessservice.core.metadata;

import com.koreanre.ifrs17.businessservice.api.dto.response.CatalogEntry;
import com.koreanre.ifrs17.businessservice.persistence.mapper.BsServiceRepository;
import com.koreanre.ifrs17.businessservice.persistence.mapper.BsServiceRoleRepository;
import com.koreanre.ifrs17.businessservice.persistence.mapper.BsServiceVersionRepository;
import com.koreanre.ifrs17.businessservice.persistence.model.BsService;
import com.koreanre.ifrs17.businessservice.persistence.model.BsServiceVersion;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/** 5.1 GET /catalog, /catalog/{serviceId}: 외부 채널에 공개하는 서비스 목록. */
@Component
public class CatalogQueryService {

    private final BsServiceRepository serviceRepository;
    private final BsServiceVersionRepository versionRepository;
    private final BsServiceRoleRepository roleRepository;

    public CatalogQueryService(BsServiceRepository serviceRepository, BsServiceVersionRepository versionRepository,
            BsServiceRoleRepository roleRepository) {
        this.serviceRepository = serviceRepository;
        this.versionRepository = versionRepository;
        this.roleRepository = roleRepository;
    }

    public List<CatalogEntry> listActive() {
        return serviceRepository.findAll().stream()
                .filter(BsService::isActive)
                .map(this::toEntry)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    public Optional<CatalogEntry> findActive(String serviceId) {
        return serviceRepository.findById(serviceId)
                .filter(BsService::isActive)
                .flatMap(this::toEntry);
    }

    private Optional<CatalogEntry> toEntry(BsService service) {
        Optional<BsServiceVersion> activeVersion =
                versionRepository.findFirstByServiceIdAndStatusCodeOrderByEffectiveFromDesc(service.getServiceId(), "ACTIVE");
        if (!activeVersion.isPresent()) {
            return Optional.empty();
        }
        Set<String> roles = roleRepository.findByServiceId(service.getServiceId()).stream()
                .map(r -> r.getRoleCode())
                .collect(Collectors.toSet());
        return Optional.of(CatalogEntry.builder()
                .serviceId(service.getServiceId())
                .serviceName(service.getServiceName())
                .domainCode(service.getDomainCode())
                .serviceType(service.getServiceType())
                .sourceSystem(service.getSourceSystem())
                .activeVersion(activeVersion.get().getVersion())
                .statusCode(activeVersion.get().getStatusCode())
                .requiredRoles(roles)
                .build());
    }
}
