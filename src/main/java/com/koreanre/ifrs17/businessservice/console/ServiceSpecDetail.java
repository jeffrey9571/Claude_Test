package com.koreanre.ifrs17.businessservice.console;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.Collections;
import java.util.List;

/** CON-01 상세영역 응답(8.3 필드 표 전체). */
@Getter
@Builder
@AllArgsConstructor
public class ServiceSpecDetail {
    private final String serviceId;
    private final String serviceName;
    private final String domainCode;
    private final String serviceType;
    private final String serviceDescription;
    private final String ownerDepartment;
    private final String version;
    private final String implementationBean;
    private final Integer timeoutMs;
    private final String versionStatus;
    @Builder.Default
    private final List<ServiceSpecRequest.ParamSpec> requestParams = Collections.emptyList();
    @Builder.Default
    private final List<String> allowedRoles = Collections.emptyList();
    private final boolean active;
    private final boolean beanRegistered;
}
