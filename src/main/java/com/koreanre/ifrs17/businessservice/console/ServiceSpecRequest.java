package com.koreanre.ifrs17.businessservice.console;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Collections;
import java.util.List;

/** 8.3 서비스 명세 관리 화면의 등록/수정 입력(그림 8-1 필드 구성). */
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
    /** 요청 JSON 명세(자유 텍스트). 설계서 8.3 "요청 JSON 명세" 필드. */
    private String requestSpec;
    /** 응답 JSON 명세(자유 텍스트). 설계서 8.3 "응답 JSON 명세" 필드. */
    private String responseSpec;
    private List<String> allowedRoles = Collections.emptyList();
    private boolean active;
}
