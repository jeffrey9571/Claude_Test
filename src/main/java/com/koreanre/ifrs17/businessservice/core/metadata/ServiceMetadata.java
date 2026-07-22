package com.koreanre.ifrs17.businessservice.core.metadata;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.Collections;
import java.util.Set;

/**
 * ServiceMetadataRepository가 Catalog(BS_SERVICE/BS_SERVICE_VERSION/BS_SERVICE_ROLE)를
 * 조회하여 조합한 실행용 메타데이터.
 */
@Getter
@Builder
@AllArgsConstructor
public class ServiceMetadata {

    private final String serviceId;
    private final String serviceName;
    private final String serviceType;
    private final String sourceSystem;
    private final String version;
    private final String implementationBean;
    private final int timeoutMs;
    @Builder.Default
    private final Set<String> requiredRoles = Collections.emptySet();
}
