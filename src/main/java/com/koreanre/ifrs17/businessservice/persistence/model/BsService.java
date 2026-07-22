package com.koreanre.ifrs17.businessservice.persistence.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.LocalDateTime;

/** BS_SERVICE : 서비스 Catalog. */
@Entity
@Table(name = "bs_service", schema = "business_service")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BsService {

    @Id
    @Column(name = "service_id", length = 100)
    private String serviceId;

    @Column(name = "service_name", length = 200, nullable = false)
    private String serviceName;

    @Column(name = "domain_code", length = 30, nullable = false)
    private String domainCode;

    @Column(name = "service_type", length = 10, nullable = false)
    private String serviceType;

    @Column(name = "source_system", length = 30, nullable = false)
    private String sourceSystem;

    @Column(name = "service_description")
    private String serviceDescription;

    @Column(name = "owner_department", length = 50, nullable = false)
    private String ownerDepartment;

    @Column(name = "active_yn", length = 1, nullable = false)
    private String activeYn;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "created_by", length = 30, nullable = false)
    private String createdBy;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "updated_by", length = 30, nullable = false)
    private String updatedBy;

    public boolean isActive() {
        return "Y".equals(activeYn);
    }
}
