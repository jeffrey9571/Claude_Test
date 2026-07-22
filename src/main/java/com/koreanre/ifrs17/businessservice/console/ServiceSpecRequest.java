package com.koreanre.ifrs17.businessservice.console;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Collections;
import java.util.List;

/** 8.3 서비스 명세 관리 화면의 등록/수정 입력. */
@Getter
@Setter
@NoArgsConstructor
public class ServiceSpecRequest {

    private String serviceId;
    private String serviceName;
    private String domainCode;
    private String serviceType;
    private String serviceDescription;
    private String ownerDepartment;
    private String version;
    private String implementationBean;
    private Integer timeoutMs;
    private List<ParamSpec> requestParams = Collections.emptyList();
    private List<String> allowedRoles = Collections.emptyList();
    private boolean active;

    @Getter
    @Setter
    @NoArgsConstructor
    public static class ParamSpec {
        private String paramName;
        private String paramType;
        private boolean required;
        private String paramDescription;
        private int displayOrder;
    }
}
