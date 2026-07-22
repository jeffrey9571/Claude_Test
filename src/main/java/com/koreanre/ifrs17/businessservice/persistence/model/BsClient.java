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

/** BS_CLIENT : 호출 Client 등록. */
@Entity
@Table(name = "bs_client", schema = "business_service")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BsClient {

    @Id
    @Column(name = "client_id", length = 80)
    private String clientId;

    @Column(name = "client_name", length = 200, nullable = false)
    private String clientName;

    @Column(name = "channel_type", length = 30, nullable = false)
    private String channelType;

    @Column(name = "allowed_ip_cidr", length = 50)
    private String allowedIpCidr;

    @Column(name = "active_yn", length = 1, nullable = false)
    private String activeYn;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "created_by", length = 30, nullable = false)
    private String createdBy;

    public boolean isActive() {
        return "Y".equals(activeYn);
    }
}
