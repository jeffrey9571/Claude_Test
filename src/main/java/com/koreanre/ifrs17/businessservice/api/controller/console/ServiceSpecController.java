package com.koreanre.ifrs17.businessservice.api.controller.console;

import com.koreanre.ifrs17.businessservice.console.ServiceSpecDetail;
import com.koreanre.ifrs17.businessservice.console.ServiceSpecRequest;
import com.koreanre.ifrs17.businessservice.console.ServiceSpecService;
import com.koreanre.ifrs17.businessservice.console.ServiceSpecSummary;
import com.koreanre.ifrs17.businessservice.core.context.RequestContextResolver;
import com.koreanre.ifrs17.businessservice.core.exception.AuthenticationException;
import com.koreanre.ifrs17.businessservice.core.exception.AuthorizationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * CON-01 서비스 명세 관리 화면의 API.
 * 8.2/8.3/8.4: 목록 조회, 신규 등록, 상세 조회, 수정, 사용/미사용 설정.
 * 6.2: 관리 Console의 등록·변경·권한부여는 운영자 역할(BS_CONSOLE_ADMIN)로 제한한다.
 */
@RestController
@Slf4j
@RequestMapping("/api/console/services")
public class ServiceSpecController {

    private static final String CONSOLE_ADMIN_ROLE = "BS_CONSOLE_ADMIN";

    private final ServiceSpecService serviceSpecService;

    public ServiceSpecController(ServiceSpecService serviceSpecService) {
        this.serviceSpecService = serviceSpecService;
    }

    @GetMapping
    public List<ServiceSpecSummary> list() {
        log.info(">>> [진입] ServiceSpecController.list() - [CON-01] 서비스 명세 목록 조회");
        return serviceSpecService.list();
    }

    @GetMapping("/{serviceId}")
    public ServiceSpecDetail get(@PathVariable String serviceId) {
        log.info(">>> [진입] ServiceSpecController.get() - [CON-01] 서비스 상세 조회. serviceId={}", serviceId);
        return serviceSpecService.get(serviceId);
    }

    @PostMapping
    public ServiceSpecDetail create(@RequestBody ServiceSpecRequest request, HttpServletRequest httpRequest) {
        log.info(">>> [진입] ServiceSpecController.create() - [CON-01] 서비스 신규 등록. serviceId={}",
                request == null ? null : request.getServiceId());
        String operatorId = requireOperator(httpRequest);
        return serviceSpecService.create(request, operatorId);
    }

    @PutMapping("/{serviceId}")
    public ServiceSpecDetail update(@PathVariable String serviceId, @RequestBody ServiceSpecRequest request,
            HttpServletRequest httpRequest) {
        log.info(">>> [진입] ServiceSpecController.update() - [CON-01] 서비스 명세 수정. serviceId={}", serviceId);
        String operatorId = requireOperator(httpRequest);
        return serviceSpecService.update(serviceId, request, operatorId);
    }

    @PatchMapping("/{serviceId}/active")
    public ServiceSpecDetail setActive(@PathVariable String serviceId, @RequestBody Map<String, Boolean> body,
            HttpServletRequest httpRequest) {
        boolean active = Boolean.TRUE.equals(body.get("active"));
        log.info(">>> [진입] ServiceSpecController.setActive() - [CON-01] 사용여부 전환. serviceId={}, active={}",
                serviceId, active);
        String operatorId = requireOperator(httpRequest);
        return serviceSpecService.setActive(serviceId, active, operatorId);
    }

    private String requireOperator(HttpServletRequest httpRequest) {
        String userId = httpRequest.getHeader(RequestContextResolver.HEADER_USER_ID);
        if (!StringUtils.hasText(userId)) {
            throw new AuthenticationException("운영자 사용자 정보를 확인할 수 없습니다.");
        }
        String roles = httpRequest.getHeader(RequestContextResolver.HEADER_USER_ROLES);
        if (roles == null || !roles.contains(CONSOLE_ADMIN_ROLE)) {
            throw new AuthorizationException("관리 Console 운영자 권한이 없습니다.");
        }
        return userId;
    }
}
