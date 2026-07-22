package com.koreanre.ifrs17.businessservice.api.controller;

import com.koreanre.ifrs17.businessservice.api.dto.response.CatalogEntry;
import com.koreanre.ifrs17.businessservice.core.exception.ServiceNotFoundException;
import com.koreanre.ifrs17.businessservice.core.metadata.CatalogQueryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** 5.1 URI 규칙: GET /catalog, GET /catalog/{serviceId}. */
@Slf4j
@RestController
@RequestMapping("/api/business-services/v1/catalog")
public class CatalogController {

    private final CatalogQueryService catalogQueryService;

    public CatalogController(CatalogQueryService catalogQueryService) {
        this.catalogQueryService = catalogQueryService;
    }

    @GetMapping
    public List<CatalogEntry> list() {
        log.info(">>> [진입] CatalogController.list() - 서비스 Catalog 목록 조회");
        return catalogQueryService.listActive();
    }

    @GetMapping("/{serviceId}")
    public CatalogEntry get(@PathVariable String serviceId) {
        log.info(">>> [진입] CatalogController.get() - serviceId={}", serviceId);
        return catalogQueryService.findActive(serviceId)
                .orElseThrow(() -> new ServiceNotFoundException(serviceId, "-"));
    }
}
