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

/** BS_CALL_LOG : 호출 감사로그 (6.3 필수항목). */
@Entity
@Table(name = "bs_call_log", schema = "business_service")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BsCallLog {

    @Id
    @Column(name = "request_id", length = 80)
    private String requestId;

    @Column(name = "trace_id", length = 80)
    private String traceId;

    @Column(name = "parent_request_id", length = 80)
    private String parentRequestId;

    @Column(name = "service_id", length = 100, nullable = false)
    private String serviceId;

    @Column(name = "service_version", length = 20, nullable = false)
    private String serviceVersion;

    @Column(name = "client_id", length = 80, nullable = false)
    private String clientId;

    @Column(name = "user_id", length = 50)
    private String userId;

    @Column(name = "department_code", length = 50)
    private String departmentCode;

    @Column(name = "roles", length = 300)
    private String roles;

    @Column(name = "requested_at", nullable = false)
    private LocalDateTime requestedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "elapsed_ms")
    private Integer elapsedMs;

    @Column(name = "status_code", length = 20, nullable = false)
    private String statusCode;

    @Column(name = "http_status")
    private Integer httpStatus;

    @Column(name = "error_code", length = 50)
    private String errorCode;

    @Column(name = "error_id", length = 80)
    private String errorId;

    @Column(name = "parameter_hash", length = 128)
    private String parameterHash;

    @Column(name = "result_count")
    private Integer resultCount;

    @Column(name = "sensitive_access_flag", length = 1, nullable = false)
    private String sensitiveAccessFlag;

    @Column(name = "remote_ip", length = 64)
    private String remoteIp;

    @Column(name = "auth_type", length = 30)
    private String authType;

    @Column(name = "authorization_result", length = 20)
    private String authorizationResult;

    @Column(name = "server_instance", length = 100)
    private String serverInstance;

    @Column(name = "application_version", length = 30)
    private String applicationVersion;
}
