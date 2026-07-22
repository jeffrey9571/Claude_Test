package com.koreanre.ifrs17.businessservice.api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.Set;

/** GET /catalog, /catalog/{serviceId} 응답 항목. */
@Getter
@Builder
@AllArgsConstructor
public class CatalogEntry {
    private final String serviceId;
    private final String serviceName;
    private final String domainCode;
    private final String serviceType;
    private final String sourceSystem;
    private final String activeVersion;
    private final String statusCode;
    private final Set<String> requiredRoles;
}
