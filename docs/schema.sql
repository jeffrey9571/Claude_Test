-- =============================================================================
-- IFRS17 Business Service Layer - Database Schema (DDL)
-- 근거: 개발착수용 상세설계서 v1.3 / 7장 「메타데이터 및 Database 설계」
--   - 7.2 주요 테이블 (14개 목록)
--   - 7.3 핵심 DDL (bs_service / bs_service_version / bs_call_log 원본)
-- 작성 기준: 설계서 문서 내용만 근거로 정의 (코드 구현과 무관)
--   [원본]  = 7.3에 명시된 DDL 그대로
--   [신규]  = 7.2 목적·주요Key + 관련 절(5.2, 6.3, 8.3, 8.4, 부록 B)을 근거로 정의
-- DBMS: PostgreSQL
-- =============================================================================

CREATE SCHEMA IF NOT EXISTS business_service;

-- =============================================================================
-- 1) BS_SERVICE : 서비스 Catalog                                     [원본 7.3]
-- =============================================================================
CREATE TABLE business_service.bs_service (
    service_id        varchar(100) PRIMARY KEY,
    service_name      varchar(200) NOT NULL,
    domain_code       varchar(30)  NOT NULL,
    service_type      varchar(10)  NOT NULL CHECK (service_type IN ('READ','ACTION')),
    source_system     varchar(30)  NOT NULL DEFAULT 'IFRS17',
    owner_department  varchar(50)  NOT NULL,
    active_yn         char(1)      NOT NULL DEFAULT 'Y',
    created_at        timestamp    NOT NULL DEFAULT current_timestamp,
    created_by        varchar(30)  NOT NULL,
    updated_at        timestamp    NOT NULL DEFAULT current_timestamp,
    updated_by        varchar(30)  NOT NULL
);

-- =============================================================================
-- 2) BS_SERVICE_VERSION : 버전·상태·Schema·Timeout                   [원본 7.3]
-- =============================================================================
CREATE TABLE business_service.bs_service_version (
    service_id          varchar(100) NOT NULL REFERENCES business_service.bs_service(service_id),
    version             varchar(20)  NOT NULL,
    implementation_bean varchar(200) NOT NULL,
    timeout_ms          integer NOT NULL DEFAULT 30000,
    request_schema      text,
    response_schema     text,
    status_code         varchar(20) NOT NULL DEFAULT 'ACTIVE',
    effective_from      timestamp NOT NULL DEFAULT current_timestamp,
    effective_to        timestamp,
    PRIMARY KEY (service_id, version)
);

-- =============================================================================
-- 3) BS_SERVICE_PARAM : 입력 파라미터 정의                            [신규]
--   근거: 7.2 (Key: service_id, version, param_name),
--         8.3 요청 JSON 명세(필드/필수값), 5.3 표준 Request parameters
-- =============================================================================
CREATE TABLE business_service.bs_service_param (
    service_id        varchar(100) NOT NULL,
    version           varchar(20)  NOT NULL,
    param_name        varchar(100) NOT NULL,
    param_type        varchar(30)  NOT NULL,                    -- STRING/NUMBER/BOOLEAN/DATE 등
    required_yn       char(1)      NOT NULL DEFAULT 'Y' CHECK (required_yn IN ('Y','N')),
    param_description varchar(300),
    display_order     integer      NOT NULL DEFAULT 1,
    PRIMARY KEY (service_id, version, param_name),
    FOREIGN KEY (service_id, version)
        REFERENCES business_service.bs_service_version(service_id, version)
);

-- =============================================================================
-- 4) BS_SERVICE_ROLE : 서비스 역할 권한                               [신규]
--   근거: 7.2 (Key: service_id, role_code),
--         6.2 권한 원칙(BS_SERVICE_ROLE 사용), 8.3 허용 역할
-- =============================================================================
CREATE TABLE business_service.bs_service_role (
    service_id  varchar(100) NOT NULL REFERENCES business_service.bs_service(service_id),
    role_code   varchar(100) NOT NULL,
    created_at  timestamp    NOT NULL DEFAULT current_timestamp,
    created_by  varchar(30)  NOT NULL,
    PRIMARY KEY (service_id, role_code)
);

-- =============================================================================
-- 5) BS_CLIENT : 호출 Client 등록                                     [신규]
--   근거: 7.2 (Key: client_id),
--         5.2 X-Client-ID(호출 채널 식별), 6.x remote_ip 통제
-- =============================================================================
CREATE TABLE business_service.bs_client (
    client_id       varchar(80)  PRIMARY KEY,
    client_name     varchar(200) NOT NULL,
    channel_type    varchar(30)  NOT NULL,                     -- MCP / AI-AGENT / BATCH 등
    allowed_ip_cidr varchar(50),
    active_yn       char(1)      NOT NULL DEFAULT 'Y' CHECK (active_yn IN ('Y','N')),
    created_at      timestamp    NOT NULL DEFAULT current_timestamp,
    created_by      varchar(30)  NOT NULL
);

-- =============================================================================
-- 6) BS_CLIENT_SERVICE : Client별 호출 허용                           [신규]
--   근거: 7.2 (Key: client_id, service_id)
-- =============================================================================
CREATE TABLE business_service.bs_client_service (
    client_id  varchar(80)  NOT NULL REFERENCES business_service.bs_client(client_id),
    service_id varchar(100) NOT NULL REFERENCES business_service.bs_service(service_id),
    active_yn  char(1)      NOT NULL DEFAULT 'Y' CHECK (active_yn IN ('Y','N')),
    created_at timestamp    NOT NULL DEFAULT current_timestamp,
    created_by varchar(30)  NOT NULL,
    PRIMARY KEY (client_id, service_id)
);

-- =============================================================================
-- 7) BS_CALL_LOG : 호출 감사로그                                      [원본 7.3]
-- =============================================================================
CREATE TABLE business_service.bs_call_log (
    request_id        varchar(80) PRIMARY KEY,
    trace_id          varchar(80),
    service_id        varchar(100) NOT NULL,
    service_version   varchar(20) NOT NULL,
    client_id         varchar(80) NOT NULL,
    user_id           varchar(50),
    department_code   varchar(50),
    requested_at      timestamp NOT NULL,
    completed_at      timestamp,
    elapsed_ms        integer,
    status_code       varchar(20) NOT NULL,
    http_status       integer,
    error_code        varchar(50),
    error_id          varchar(80),
    parameter_hash    varchar(128),
    result_count      integer,
    remote_ip         varchar(64),
    server_instance   varchar(100)
);
CREATE INDEX ix_bs_call_log_01 ON business_service.bs_call_log(service_id, requested_at DESC);
CREATE INDEX ix_bs_call_log_02 ON business_service.bs_call_log(user_id, requested_at DESC);
CREATE INDEX ix_bs_call_log_03 ON business_service.bs_call_log(status_code, requested_at DESC);

-- =============================================================================
-- 8) BS_ACCESS_ENTITY : 접근 업무객체 이력                            [신규]
--   근거: 7.2 (Key: request_id, entity_type, entity_id),
--         6.3 감사 데이터 필드(accessed_entity_ids)
-- =============================================================================
CREATE TABLE business_service.bs_access_entity (
    request_id  varchar(80)  NOT NULL REFERENCES business_service.bs_call_log(request_id),
    entity_type varchar(50)  NOT NULL,
    entity_id   varchar(200) NOT NULL,
    accessed_at timestamp    NOT NULL DEFAULT current_timestamp,
    PRIMARY KEY (request_id, entity_type, entity_id)
);

-- =============================================================================
-- 9) BS_CHANGE_HISTORY : 메타데이터 변경이력                          [신규]
--   근거: 7.2 (Key: change_id), 7.4 「Catalog 변경은 변경이력을 남긴다」
-- =============================================================================
CREATE TABLE business_service.bs_change_history (
    change_id      varchar(80)  PRIMARY KEY,
    entity_type    varchar(50)  NOT NULL,                       -- SERVICE / VERSION / PARAM / ROLE / CLIENT 등
    entity_id      varchar(200) NOT NULL,
    change_type    varchar(20)  NOT NULL
        CHECK (change_type IN ('CREATE','UPDATE','DELETE','ACTIVATE','DEACTIVATE')),
    changed_by     varchar(30)  NOT NULL,
    changed_at     timestamp    NOT NULL DEFAULT current_timestamp,
    change_summary varchar(500)
);

-- =============================================================================
-- 10) BS_SERVICE_CHANNEL : 서비스별 허용 채널과 Client 정책           [신규]
--   근거: 7.2 (Key: service_id, version, channel_code),
--         5.2 채널 식별, 6.2 권한 원칙
-- =============================================================================
CREATE TABLE business_service.bs_service_channel (
    service_id    varchar(100) NOT NULL,
    version       varchar(20)  NOT NULL,
    channel_code  varchar(30)  NOT NULL,                        -- MCP / AI-AGENT / INTERNAL 등
    allow_yn      char(1)      NOT NULL DEFAULT 'Y' CHECK (allow_yn IN ('Y','N')),
    client_policy varchar(20)  NOT NULL DEFAULT 'ALLOWLIST'
        CHECK (client_policy IN ('ALLOWLIST','DENYLIST','OPEN')),
    created_at    timestamp    NOT NULL DEFAULT current_timestamp,
    created_by    varchar(30)  NOT NULL,
    PRIMARY KEY (service_id, version, channel_code),
    FOREIGN KEY (service_id, version)
        REFERENCES business_service.bs_service_version(service_id, version)
);

-- =============================================================================
-- 11) BS_SERVICE_DEPLOYMENT : 배포된 구현체·JAR/WAR·Bean·Checksum     [신규]
--   근거: 7.2 (Key: deployment_id),
--         8.4 「Java 클래스를 WAS에 배포」 / 「Bean 존재·Interface 구현·ID 일치 검증」
-- =============================================================================
CREATE TABLE business_service.bs_service_deployment (
    deployment_id       varchar(80) PRIMARY KEY,
    service_id          varchar(100) NOT NULL,
    version             varchar(20)  NOT NULL,
    implementation_bean varchar(200) NOT NULL,
    artifact_type       varchar(10)  NOT NULL CHECK (artifact_type IN ('JAR','WAR')),
    artifact_name       varchar(200) NOT NULL,
    checksum            varchar(128) NOT NULL,                  -- 배포 산출물 무결성(SHA-256 등)
    deploy_status       varchar(20)  NOT NULL DEFAULT 'DEPLOYED'
        CHECK (deploy_status IN ('DEPLOYED','VERIFIED','FAILED','RETIRED')),
    bean_verified_yn    char(1)      NOT NULL DEFAULT 'N' CHECK (bean_verified_yn IN ('Y','N')),
    deployed_by         varchar(30)  NOT NULL,
    deployed_at         timestamp    NOT NULL DEFAULT current_timestamp,
    server_instance     varchar(100),
    FOREIGN KEY (service_id, version)
        REFERENCES business_service.bs_service_version(service_id, version)
);

-- =============================================================================
-- 12) BS_SERVICE_APPROVAL : 등록·변경·활성화 승인 이력                [신규]
--   근거: 7.2 (Key: approval_id),
--         8.4 등록·공개 흐름, 6.2 「등록·변경·권한부여는 운영자 역할로 제한」
-- =============================================================================
CREATE TABLE business_service.bs_service_approval (
    approval_id      varchar(80) PRIMARY KEY,
    service_id       varchar(100) NOT NULL REFERENCES business_service.bs_service(service_id),
    version          varchar(20),
    request_type     varchar(20) NOT NULL
        CHECK (request_type IN ('REGISTER','MODIFY','ACTIVATE','DEACTIVATE')),
    approval_status  varchar(20) NOT NULL DEFAULT 'REQUESTED'
        CHECK (approval_status IN ('REQUESTED','APPROVED','REJECTED')),
    requested_by     varchar(30) NOT NULL,
    requested_at     timestamp   NOT NULL DEFAULT current_timestamp,
    approved_by      varchar(30),
    approved_at      timestamp,
    approval_comment varchar(500)
);

-- =============================================================================
-- 13) BS_SERVICE_CHANGE_LOG : 메타데이터 변경 전/후 이력              [신규]
--   근거: 7.2 (Key: change_id, 목적 「변경 전/후 이력」)
--         → BS_CHANGE_HISTORY(요약)와 달리 필드 단위 before/after 값 보관
-- =============================================================================
CREATE TABLE business_service.bs_service_change_log (
    change_id    varchar(80) PRIMARY KEY,
    service_id   varchar(100) NOT NULL REFERENCES business_service.bs_service(service_id),
    version      varchar(20),
    entity_type  varchar(50)  NOT NULL,
    field_name   varchar(100) NOT NULL,
    before_value text,
    after_value  text,
    changed_by   varchar(30)  NOT NULL,
    changed_at   timestamp    NOT NULL DEFAULT current_timestamp
);

-- =============================================================================
-- 14) BS_SERVICE_CACHE_EVENT : 다중 WAS 메타데이터 Cache 무효화 이벤트 [신규]
--   근거: 7.2 (Key: event_id, 목적 「다중 WAS Cache 무효화」),
--         8.4 「사용 여부 저장 시 Registry 갱신」
-- =============================================================================
CREATE TABLE business_service.bs_service_cache_event (
    event_id        varchar(80) PRIMARY KEY,
    service_id      varchar(100),                               -- NULL = 전체 무효화
    version         varchar(20),
    event_type      varchar(20) NOT NULL
        CHECK (event_type IN ('INVALIDATE','RELOAD','EVICT_ALL')),
    created_at      timestamp   NOT NULL DEFAULT current_timestamp,
    created_by      varchar(30) NOT NULL,
    processed_yn    char(1)     NOT NULL DEFAULT 'N' CHECK (processed_yn IN ('Y','N')),
    processed_at    timestamp,
    server_instance varchar(100)                                -- 이벤트를 소비한 WAS 인스턴스
);
