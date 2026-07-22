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

/** BS_CHANGE_HISTORY : 메타데이터 변경이력. */
@Entity
@Table(name = "bs_change_history", schema = "business_service")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BsChangeHistory {

    @Id
    @Column(name = "change_id", length = 80)
    private String changeId;

    @Column(name = "entity_type", length = 50, nullable = false)
    private String entityType;

    @Column(name = "entity_id", length = 200, nullable = false)
    private String entityId;

    @Column(name = "change_type", length = 20, nullable = false)
    private String changeType;

    @Column(name = "changed_by", length = 30, nullable = false)
    private String changedBy;

    @Column(name = "changed_at", nullable = false)
    private LocalDateTime changedAt;

    @Column(name = "change_summary", length = 500)
    private String changeSummary;
}
