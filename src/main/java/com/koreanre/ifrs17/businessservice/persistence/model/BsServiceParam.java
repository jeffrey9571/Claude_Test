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

/** BS_SERVICE_PARAM : 입력 파라미터 정의. */
@Entity
@Table(name = "bs_service_param", schema = "business_service")
@IdClass(BsServiceParamId.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BsServiceParam {

    @Id
    @Column(name = "service_id", length = 100)
    private String serviceId;

    @Id
    @Column(name = "version", length = 20)
    private String version;

    @Id
    @Column(name = "param_name", length = 100)
    private String paramName;

    @Column(name = "param_type", length = 30, nullable = false)
    private String paramType;

    @Column(name = "required_yn", length = 1, nullable = false)
    private String requiredYn;

    @Column(name = "param_description", length = 300)
    private String paramDescription;

    @Column(name = "display_order", nullable = false)
    private Integer displayOrder;

    public boolean isRequired() {
        return "Y".equals(requiredYn);
    }
}
