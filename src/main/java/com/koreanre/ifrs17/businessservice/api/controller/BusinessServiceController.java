package com.koreanre.ifrs17.businessservice.api.controller;

import com.koreanre.ifrs17.businessservice.api.dto.request.StandardRequest;
import com.koreanre.ifrs17.businessservice.api.dto.response.StandardResponse;
import com.koreanre.ifrs17.businessservice.core.executor.BusinessServiceExecutor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

/**
 * 5.1 URI 규칙: POST /api/business-services/v1/{serviceId}:execute
 * 10.1 Reference Implementation Controller.
 * Console의 서비스 Test 화면(8.6)도 동일한 Endpoint/Controller/Dispatcher 경로를 사용한다.
 */
@RestController
@RequestMapping("/api/business-services/v1")
public class BusinessServiceController {

    private final BusinessServiceExecutor executor;

    public BusinessServiceController(BusinessServiceExecutor executor) {
        this.executor = executor;
    }

    @PostMapping("/{serviceId}:execute")
    public ResponseEntity<StandardResponse<?>> execute(
            @PathVariable String serviceId,
            @RequestBody StandardRequest request,
            HttpServletRequest httpRequest) {
        return executor.execute(serviceId, request, httpRequest);
    }
}
