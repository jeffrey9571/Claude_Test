package com.koreanre.ifrs17.businessservice.persistence.model;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class BsAccessEntityId implements Serializable {
    private String requestId;
    private String entityType;
    private String entityId;
}
