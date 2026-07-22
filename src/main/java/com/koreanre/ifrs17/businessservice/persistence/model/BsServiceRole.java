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

/** BS_SERVICE_ROLE : 서비스 역할 권한. */
@Entity
@Table(name = "bs_service_role", schema = "business_service")
@IdClass(BsServiceRoleId.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BsServiceRole {

    @Id
    @Column(name = "service_id", length = 100)
    private String serviceId;

    @Id
    @Column(name = "role_code", length = 100)
    private String roleCode;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}
