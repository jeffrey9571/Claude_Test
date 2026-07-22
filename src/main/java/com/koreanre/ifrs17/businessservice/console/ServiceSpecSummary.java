package com.koreanre.ifrs17.businessservice.console;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/** CON-01 목록 영역에 표시되는 요약 행. */
@Getter
@Builder
@AllArgsConstructor
public class ServiceSpecSummary {
    private final String serviceId;
    private final String serviceName;
    private final String domainCode;
    private final String serviceType;
    private final String activeVersion;
    private final boolean active;
    private final String versionStatus;
}
