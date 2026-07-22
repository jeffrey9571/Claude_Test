package com.koreanre.ifrs17.businessservice.persistence.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;
import java.time.LocalDateTime;

/** BS_SERVICE_VERSION : 버전·상태·Schema·Timeout. */
@Entity
@Table(name = "bs_service_version", schema = "business_service")
@IdClass(BsServiceVersionId.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BsServiceVersion {

    @Id
    @Column(name = "service_id", length = 100)
    private String serviceId;

    @Id
    @Column(name = "version", length = 20)
    private String version;

    @Column(name = "implementation_bean", length = 200, nullable = false)
    private String implementationBean;

    @Column(name = "timeout_ms", nullable = false)
    private Integer timeoutMs;

    @Column(name = "request_schema")
    private String requestSchema;

    @Column(name = "response_schema")
    private String responseSchema;

    @Column(name = "status_code", length = 20, nullable = false)
    private String statusCode;

    @Column(name = "effective_from", nullable = false)
    private LocalDateTime effectiveFrom;

    @Column(name = "effective_to")
    private LocalDateTime effectiveTo;

    public boolean isActive() {
        return "ACTIVE".equals(statusCode);
    }
}
