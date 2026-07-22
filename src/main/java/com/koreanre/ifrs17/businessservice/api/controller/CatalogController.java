package com.koreanre.ifrs17.businessservice.api.controller;

import com.koreanre.ifrs17.businessservice.api.dto.response.CatalogEntry;
import com.koreanre.ifrs17.businessservice.core.exception.ServiceNotFoundException;
import com.koreanre.ifrs17.businessservice.core.metadata.CatalogQueryService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** 5.1 URI 규칙: GET /catalog, GET /catalog/{serviceId}. */
@RestController
@RequestMapping("/api/business-services/v1/catalog")
public class CatalogController {

    private final CatalogQueryService catalogQueryService;

    public CatalogController(CatalogQueryService catalogQueryService) {
        this.catalogQueryService = catalogQueryService;
    }

    @GetMapping
    public List<CatalogEntry> list() {
        return catalogQueryService.listActive();
    }

    @GetMapping("/{serviceId}")
    public CatalogEntry get(@PathVariable String serviceId) {
        return catalogQueryService.findActive(serviceId)
                .orElseThrow(() -> new ServiceNotFoundException(serviceId, "-"));
    }
}
