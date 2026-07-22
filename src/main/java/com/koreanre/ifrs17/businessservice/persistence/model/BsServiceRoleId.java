package com.koreanre.ifrs17.businessservice.persistence.model;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class BsServiceRoleId implements Serializable {
    private String serviceId;
    private String roleCode;
}
