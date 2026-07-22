package com.koreanre.ifrs17.businessservice.console;

import com.koreanre.ifrs17.businessservice.core.exception.ValidationException;
import com.koreanre.ifrs17.businessservice.core.executor.BusinessServiceHandler;
import com.koreanre.ifrs17.businessservice.persistence.mapper.BsChangeHistoryRepository;
import com.koreanre.ifrs17.businessservice.persistence.mapper.BsServiceParamRepository;
import com.koreanre.ifrs17.businessservice.persistence.mapper.BsServiceRepository;
import com.koreanre.ifrs17.businessservice.persistence.mapper.BsServiceRoleRepository;
import com.koreanre.ifrs17.businessservice.persistence.mapper.BsServiceVersionRepository;
import com.koreanre.ifrs17.businessservice.persistence.model.BsChangeHistory;
import com.koreanre.ifrs17.businessservice.persistence.model.BsService;
import com.koreanre.ifrs17.businessservice.persistence.model.BsServiceParam;
import com.koreanre.ifrs17.businessservice.persistence.model.BsServiceRole;
import com.koreanre.ifrs17.businessservice.persistence.model.BsServiceVersion;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 8.3 서비스 명세 관리 / 8.4 서비스 등록 및 외부 공개 흐름의 백엔드 로직.
 * 저장/사용전환 시 Spring Bean 존재 여부, BusinessServiceHandler 구현 여부,
 * Service ID 일치 여부를 검증한다(8.1, CON-ACC-01).
 */
@Service
public class ServiceSpecService {

    private static final Pattern SERVICE_ID_PATTERN = Pattern.compile("^[A-Z0-9]+(\\.[A-Z0-9]+)+$");

    private final BsServiceRepository serviceRepository;
    private final BsServiceVersionRepository versionRepository;
    private final BsServiceParamRepository paramRepository;
    private final BsServiceRoleRepository roleRepository;
    private final BsChangeHistoryRepository changeHistoryRepository;
    private final ApplicationContext applicationContext;

    public ServiceSpecService(BsServiceRepository serviceRepository, BsServiceVersionRepository versionRepository,
            BsServiceParamRepository paramRepository, BsServiceRoleRepository roleRepository,
            BsChangeHistoryRepository changeHistoryRepository, ApplicationContext applicationContext) {
        this.serviceRepository = serviceRepository;
        this.versionRepository = versionRepository;
        this.paramRepository = paramRepository;
        this.roleRepository = roleRepository;
        this.changeHistoryRepository = changeHistoryRepository;
        this.applicationContext = applicationContext;
    }

    public List<ServiceSpecSummary> list() {
        return serviceRepository.findAll().stream()
                .map(this::toSummary)
                .collect(Collectors.toList());
    }

    public ServiceSpecDetail get(String serviceId) {
        BsService service = requireService(serviceId);
        BsServiceVersion version = requireLatestVersion(serviceId);
        return toDetail(service, version);
    }

    @Transactional
    public ServiceSpecDetail create(ServiceSpecRequest request, String operatorId) {
        validateServiceIdFormat(request.getServiceId());
        if (serviceRepository.existsById(request.getServiceId())) {
            throw new ValidationException("이미 등록된 Service ID입니다: " + request.getServiceId());
        }
        validateCommon(request);
        validateBean(request.getServiceId(), request.getImplementationBean());

        LocalDateTime now = LocalDateTime.now();
        BsService service = BsService.builder()
                .serviceId(request.getServiceId())
                .serviceName(request.getServiceName())
                .domainCode(request.getDomainCode())
                .serviceType(request.getServiceType())
                .sourceSystem("IFRS17")
                .serviceDescription(request.getServiceDescription())
                .ownerDepartment(request.getOwnerDepartment())
                .activeYn(request.isActive() ? "Y" : "N")
                .createdAt(now)
                .createdBy(operatorId)
                .updatedAt(now)
                .updatedBy(operatorId)
                .build();
        serviceRepository.save(service);

        saveVersion(request, now);
        savePararms(request);
        saveRoles(request);
        recordChange(request.getServiceId(), "CREATE", operatorId, "서비스 신규 등록");

        return get(request.getServiceId());
    }

    @Transactional
    public ServiceSpecDetail update(String serviceId, ServiceSpecRequest request, String operatorId) {
        BsService service = requireService(serviceId);
        validateCommon(request);
        validateBean(serviceId, request.getImplementationBean());

        service.setServiceName(request.getServiceName());
        service.setDomainCode(request.getDomainCode());
        service.setServiceType(request.getServiceType());
        service.setServiceDescription(request.getServiceDescription());
        service.setOwnerDepartment(request.getOwnerDepartment());
        service.setUpdatedAt(LocalDateTime.now());
        service.setUpdatedBy(operatorId);
        serviceRepository.save(service);

        paramRepository.deleteAll(paramRepository.findByServiceIdAndVersionOrderByDisplayOrder(serviceId, request.getVersion()));
        roleRepository.deleteAll(roleRepository.findByServiceId(serviceId));

        Optional<BsServiceVersion> existingVersion = versionRepository.findByServiceIdAndVersion(serviceId, request.getVersion());
        if (existingVersion.isPresent()) {
            BsServiceVersion version = existingVersion.get();
            version.setImplementationBean(request.getImplementationBean());
            version.setTimeoutMs(request.getTimeoutMs() == null ? version.getTimeoutMs() : request.getTimeoutMs());
            version.setStatusCode(request.isActive() ? "ACTIVE" : "INACTIVE");
            versionRepository.save(version);
        } else {
            saveVersion(request, LocalDateTime.now());
        }

        savePararms(request);
        saveRoles(request);
        recordChange(serviceId, "UPDATE", operatorId, "서비스 명세 수정");

        return get(serviceId);
    }

    @Transactional
    public ServiceSpecDetail setActive(String serviceId, boolean active, String operatorId) {
        BsService service = requireService(serviceId);
        BsServiceVersion version = requireLatestVersion(serviceId);

        if (active) {
            validateBean(serviceId, version.getImplementationBean());
        }

        service.setActiveYn(active ? "Y" : "N");
        service.setUpdatedAt(LocalDateTime.now());
        service.setUpdatedBy(operatorId);
        serviceRepository.save(service);

        version.setStatusCode(active ? "ACTIVE" : "INACTIVE");
        versionRepository.save(version);

        recordChange(serviceId, active ? "ENABLE" : "DISABLE", operatorId,
                active ? "서비스 사용 전환" : "서비스 미사용 전환");

        return get(serviceId);
    }

    /** CON-ACC-01: Bean 미등록/Interface 미구현/ServiceId 불일치 시 저장·사용전환을 차단한다. */
    private void validateBean(String serviceId, String implementationBean) {
        Object bean;
        try {
            bean = applicationContext.getBean(implementationBean);
        } catch (BeansException e) {
            throw new ValidationException("등록된 Bean이 없습니다: " + implementationBean);
        }
        if (!(bean instanceof BusinessServiceHandler)) {
            throw new ValidationException("Bean이 BusinessServiceHandler를 구현하지 않습니다: " + implementationBean);
        }
        String handlerServiceId = ((BusinessServiceHandler<?, ?>) bean).serviceId();
        if (!serviceId.equals(handlerServiceId)) {
            throw new ValidationException(
                    "Handler ID가 Service ID와 일치하지 않습니다: " + handlerServiceId + " != " + serviceId);
        }
    }

    private void validateServiceIdFormat(String serviceId) {
        if (!StringUtils.hasText(serviceId) || !SERVICE_ID_PATTERN.matcher(serviceId).matches()) {
            throw new ValidationException("Service ID는 대문자와 점(.)으로 구성되어야 합니다. 예: IFRS17.CLOSING.STATUS");
        }
    }

    private void validateCommon(ServiceSpecRequest request) {
        if (!StringUtils.hasText(request.getServiceName()) || request.getServiceName().length() > 100) {
            throw new ValidationException("서비스명은 필수이며 100자 이내여야 합니다.");
        }
        if (!StringUtils.hasText(request.getVersion())) {
            throw new ValidationException("버전은 필수입니다.");
        }
        if (!StringUtils.hasText(request.getImplementationBean())) {
            throw new ValidationException("Spring Bean명은 필수입니다.");
        }
        if (request.getAllowedRoles() == null || request.getAllowedRoles().isEmpty()) {
            throw new ValidationException("허용 역할은 최소 1개 이상 지정해야 합니다.");
        }
        if (!"READ".equals(request.getServiceType()) && !"ACTION".equals(request.getServiceType())) {
            throw new ValidationException("서비스 유형은 READ 또는 ACTION이어야 합니다.");
        }
    }

    private void saveVersion(ServiceSpecRequest request, LocalDateTime now) {
        BsServiceVersion version = BsServiceVersion.builder()
                .serviceId(request.getServiceId())
                .version(request.getVersion())
                .implementationBean(request.getImplementationBean())
                .timeoutMs(request.getTimeoutMs() == null ? 30000 : request.getTimeoutMs())
                .statusCode(request.isActive() ? "ACTIVE" : "INACTIVE")
                .effectiveFrom(now)
                .build();
        versionRepository.save(version);
    }

    private void savePararms(ServiceSpecRequest request) {
        if (request.getRequestParams() == null) {
            return;
        }
        List<BsServiceParam> params = request.getRequestParams().stream()
                .map(p -> BsServiceParam.builder()
                        .serviceId(request.getServiceId())
                        .version(request.getVersion())
                        .paramName(p.getParamName())
                        .paramType(StringUtils.hasText(p.getParamType()) ? p.getParamType() : "STRING")
                        .requiredYn(p.isRequired() ? "Y" : "N")
                        .paramDescription(p.getParamDescription())
                        .displayOrder(p.getDisplayOrder())
                        .build())
                .collect(Collectors.toList());
        paramRepository.saveAll(params);
    }

    private void saveRoles(ServiceSpecRequest request) {
        LocalDateTime now = LocalDateTime.now();
        List<BsServiceRole> roles = request.getAllowedRoles().stream()
                .map(r -> BsServiceRole.builder()
                        .serviceId(request.getServiceId())
                        .roleCode(r)
                        .createdAt(now)
                        .build())
                .collect(Collectors.toList());
        roleRepository.saveAll(roles);
    }

    private void recordChange(String serviceId, String changeType, String operatorId, String summary) {
        changeHistoryRepository.save(BsChangeHistory.builder()
                .changeId("CHG-" + UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase())
                .entityType("BS_SERVICE")
                .entityId(serviceId)
                .changeType(changeType)
                .changedBy(operatorId)
                .changedAt(LocalDateTime.now())
                .changeSummary(summary)
                .build());
    }

    private BsService requireService(String serviceId) {
        return serviceRepository.findById(serviceId)
                .orElseThrow(() -> new ValidationException("등록되지 않은 서비스입니다: " + serviceId));
    }

    private BsServiceVersion requireLatestVersion(String serviceId) {
        return versionRepository.findByServiceId(serviceId).stream()
                .max(Comparator.comparing(BsServiceVersion::getEffectiveFrom))
                .orElseThrow(() -> new ValidationException("등록된 버전이 없습니다: " + serviceId));
    }

    private ServiceSpecSummary toSummary(BsService service) {
        Optional<BsServiceVersion> version = versionRepository.findByServiceId(service.getServiceId()).stream()
                .max(Comparator.comparing(BsServiceVersion::getEffectiveFrom));
        return ServiceSpecSummary.builder()
                .serviceId(service.getServiceId())
                .serviceName(service.getServiceName())
                .domainCode(service.getDomainCode())
                .serviceType(service.getServiceType())
                .activeVersion(version.map(BsServiceVersion::getVersion).orElse(null))
                .active(service.isActive())
                .versionStatus(version.map(BsServiceVersion::getStatusCode).orElse("NONE"))
                .build();
    }

    private ServiceSpecDetail toDetail(BsService service, BsServiceVersion version) {
        boolean beanRegistered;
        try {
            Object bean = applicationContext.getBean(version.getImplementationBean());
            beanRegistered = bean instanceof BusinessServiceHandler
                    && service.getServiceId().equals(((BusinessServiceHandler<?, ?>) bean).serviceId());
        } catch (BeansException e) {
            beanRegistered = false;
        }

        List<ServiceSpecRequest.ParamSpec> params = paramRepository
                .findByServiceIdAndVersionOrderByDisplayOrder(service.getServiceId(), version.getVersion()).stream()
                .map(p -> {
                    ServiceSpecRequest.ParamSpec spec = new ServiceSpecRequest.ParamSpec();
                    spec.setParamName(p.getParamName());
                    spec.setParamType(p.getParamType());
                    spec.setRequired(p.isRequired());
                    spec.setParamDescription(p.getParamDescription());
                    spec.setDisplayOrder(p.getDisplayOrder());
                    return spec;
                })
                .collect(Collectors.toList());

        List<String> roles = roleRepository.findByServiceId(service.getServiceId()).stream()
                .map(BsServiceRole::getRoleCode)
                .collect(Collectors.toList());

        return ServiceSpecDetail.builder()
                .serviceId(service.getServiceId())
                .serviceName(service.getServiceName())
                .domainCode(service.getDomainCode())
                .serviceType(service.getServiceType())
                .serviceDescription(service.getServiceDescription())
                .ownerDepartment(service.getOwnerDepartment())
                .version(version.getVersion())
                .implementationBean(version.getImplementationBean())
                .timeoutMs(version.getTimeoutMs())
                .versionStatus(version.getStatusCode())
                .requestParams(params)
                .allowedRoles(roles)
                .active(service.isActive())
                .beanRegistered(beanRegistered)
                .build();
    }
}
