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

/** BS_CLIENT_SERVICE : Client별 호출 허용. */
@Entity
@Table(name = "bs_client_service", schema = "business_service")
@IdClass(BsClientServiceId.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BsClientService {

    @Id
    @Column(name = "client_id", length = 80)
    private String clientId;

    @Id
    @Column(name = "service_id", length = 100)
    private String serviceId;

    @Column(name = "active_yn", length = 1, nullable = false)
    private String activeYn;

    public boolean isActive() {
        return "Y".equals(activeYn);
    }
}
