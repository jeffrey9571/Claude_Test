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

/** BS_ACCESS_ENTITY : 접근 업무객체 이력. */
@Entity
@Table(name = "bs_access_entity", schema = "business_service")
@IdClass(BsAccessEntityId.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BsAccessEntity {

    @Id
    @Column(name = "request_id", length = 80)
    private String requestId;

    @Id
    @Column(name = "entity_type", length = 50)
    private String entityType;

    @Id
    @Column(name = "entity_id", length = 200)
    private String entityId;
}
