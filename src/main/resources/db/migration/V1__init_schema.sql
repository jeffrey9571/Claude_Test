-- IFRS17-BSL-SDD-001 v1.3 7.3 핵심 DDL 기준.
-- 7.2 주요 테이블 중 DDL이 명시되지 않은 테이블은 목적/주요 Key 설명에 따라
-- DB-INC 착수 분석 시 재검토 가능한 초안으로 정의한다.

CREATE SCHEMA IF NOT EXISTS business_service;

-- =====================================================================
-- BS_SERVICE : 서비스 Catalog
-- =====================================================================
CREATE TABLE business_service.bs_service (
  service_id        varchar(100) PRIMARY KEY,
  service_name      varchar(200) NOT NULL,
  domain_code       varchar(30)  NOT NULL,
  service_type      varchar(10)  NOT NULL CHECK (service_type IN ('READ','ACTION')),
  source_system     varchar(30)  NOT NULL DEFAULT 'IFRS17',
  service_description text,
  owner_department  varchar(50)  NOT NULL,
  active_yn         char(1)      NOT NULL DEFAULT 'Y' CHECK (active_yn IN ('Y','N')),
  created_at        timestamp    NOT NULL DEFAULT current_timestamp,
  created_by        varchar(30)  NOT NULL,
  updated_at        timestamp    NOT NULL DEFAULT current_timestamp,
  updated_by        varchar(30)  NOT NULL
);

-- =====================================================================
-- BS_SERVICE_VERSION : 버전·상태·Schema·Timeout
-- =====================================================================
CREATE TABLE business_service.bs_service_version (
  service_id        varchar(100) NOT NULL REFERENCES business_service.bs_service(service_id),
  version           varchar(20)  NOT NULL,
  implementation_bean varchar(200) NOT NULL,
  timeout_ms        integer NOT NULL DEFAULT 30000,
  request_schema    text,
  response_schema   text,
  status_code       varchar(20) NOT NULL DEFAULT 'ACTIVE' CHECK (status_code IN ('DRAFT','ACTIVE','INACTIVE','DEPRECATED')),
  effective_from    timestamp NOT NULL DEFAULT current_timestamp,
  effective_to      timestamp,
  PRIMARY KEY(service_id, version)
);

-- =====================================================================
-- BS_SERVICE_PARAM : 입력 파라미터 정의
-- =====================================================================
CREATE TABLE business_service.bs_service_param (
  service_id        varchar(100) NOT NULL,
  version           varchar(20)  NOT NULL,
  param_name        varchar(100) NOT NULL,
  param_type        varchar(30)  NOT NULL DEFAULT 'STRING',
  required_yn       char(1)      NOT NULL DEFAULT 'N' CHECK (required_yn IN ('Y','N')),
  param_description varchar(300),
  display_order     integer NOT NULL DEFAULT 0,
  PRIMARY KEY (service_id, version, param_name),
  FOREIGN KEY (service_id, version) REFERENCES business_service.bs_service_version(service_id, version)
);

-- =====================================================================
-- BS_SERVICE_ROLE : 서비스 역할 권한
-- =====================================================================
CREATE TABLE business_service.bs_service_role (
  service_id        varchar(100) NOT NULL REFERENCES business_service.bs_service(service_id),
  role_code         varchar(100) NOT NULL,
  created_at        timestamp NOT NULL DEFAULT current_timestamp,
  PRIMARY KEY (service_id, role_code)
);

-- =====================================================================
-- BS_CLIENT : 호출 Client 등록
-- =====================================================================
CREATE TABLE business_service.bs_client (
  client_id         varchar(80) PRIMARY KEY,
  client_name       varchar(200) NOT NULL,
  channel_type      varchar(30) NOT NULL DEFAULT 'PORTAL' CHECK (channel_type IN ('PORTAL','MCP','TEAMS','BATCH','OTHER')),
  allowed_ip_cidr   varchar(50),
  active_yn         char(1) NOT NULL DEFAULT 'Y' CHECK (active_yn IN ('Y','N')),
  created_at        timestamp NOT NULL DEFAULT current_timestamp,
  created_by        varchar(30) NOT NULL
);

-- =====================================================================
-- BS_CLIENT_SERVICE : Client별 호출 허용
-- =====================================================================
CREATE TABLE business_service.bs_client_service (
  client_id         varchar(80) NOT NULL REFERENCES business_service.bs_client(client_id),
  service_id        varchar(100) NOT NULL REFERENCES business_service.bs_service(service_id),
  active_yn         char(1) NOT NULL DEFAULT 'Y' CHECK (active_yn IN ('Y','N')),
  PRIMARY KEY (client_id, service_id)
);

-- =====================================================================
-- BS_SERVICE_CHANNEL : 서비스별 허용 채널과 Client 정책
-- =====================================================================
CREATE TABLE business_service.bs_service_channel (
  service_id        varchar(100) NOT NULL,
  version           varchar(20)  NOT NULL,
  channel_code      varchar(30)  NOT NULL,
  policy_description varchar(300),
  PRIMARY KEY (service_id, version, channel_code),
  FOREIGN KEY (service_id, version) REFERENCES business_service.bs_service_version(service_id, version)
);

-- =====================================================================
-- BS_SERVICE_DEPLOYMENT : 배포된 구현체·JAR/WAR·Bean·Checksum 정보
-- =====================================================================
CREATE TABLE business_service.bs_service_deployment (
  deployment_id     varchar(80) PRIMARY KEY,
  service_id        varchar(100) NOT NULL,
  version           varchar(20)  NOT NULL,
  artifact_name     varchar(200) NOT NULL,
  bean_name         varchar(200) NOT NULL,
  checksum          varchar(128),
  deployed_at       timestamp NOT NULL DEFAULT current_timestamp,
  deployed_by       varchar(30) NOT NULL,
  FOREIGN KEY (service_id, version) REFERENCES business_service.bs_service_version(service_id, version)
);

-- =====================================================================
-- BS_SERVICE_APPROVAL : 등록·변경·활성화 승인 이력
-- =====================================================================
CREATE TABLE business_service.bs_service_approval (
  approval_id       varchar(80) PRIMARY KEY,
  service_id        varchar(100) NOT NULL,
  version           varchar(20)  NOT NULL,
  approval_type     varchar(30) NOT NULL CHECK (approval_type IN ('REGISTER','UPDATE','ACTIVATE','DEACTIVATE')),
  requested_by      varchar(30) NOT NULL,
  requested_at      timestamp NOT NULL DEFAULT current_timestamp,
  approved_by       varchar(30),
  approved_at       timestamp,
  approval_status   varchar(20) NOT NULL DEFAULT 'PENDING' CHECK (approval_status IN ('PENDING','APPROVED','REJECTED')),
  comment           varchar(500)
);

-- =====================================================================
-- BS_CHANGE_HISTORY : 메타데이터 변경이력 (Catalog 전반 감사 추적)
-- =====================================================================
CREATE TABLE business_service.bs_change_history (
  change_id         varchar(80) PRIMARY KEY,
  entity_type       varchar(50) NOT NULL,
  entity_id         varchar(200) NOT NULL,
  change_type       varchar(20) NOT NULL CHECK (change_type IN ('CREATE','UPDATE','ENABLE','DISABLE','DELETE')),
  changed_by        varchar(30) NOT NULL,
  changed_at        timestamp NOT NULL DEFAULT current_timestamp,
  change_summary    varchar(500)
);

-- =====================================================================
-- BS_SERVICE_CHANGE_LOG : 서비스 명세 변경 전/후 상세 이력
-- =====================================================================
CREATE TABLE business_service.bs_service_change_log (
  change_id         varchar(80) PRIMARY KEY,
  service_id        varchar(100) NOT NULL,
  version            varchar(20) NOT NULL,
  before_json       text,
  after_json        text,
  changed_by        varchar(30) NOT NULL,
  changed_at        timestamp NOT NULL DEFAULT current_timestamp
);

-- =====================================================================
-- BS_SERVICE_CACHE_EVENT : 다중 WAS 메타데이터 Cache 무효화 이벤트
-- =====================================================================
CREATE TABLE business_service.bs_service_cache_event (
  event_id          varchar(80) PRIMARY KEY,
  service_id        varchar(100) NOT NULL,
  event_type        varchar(20) NOT NULL DEFAULT 'INVALIDATE' CHECK (event_type IN ('INVALIDATE','RELOAD')),
  issued_at         timestamp NOT NULL DEFAULT current_timestamp,
  issued_by         varchar(30) NOT NULL,
  consumed_yn       char(1) NOT NULL DEFAULT 'N' CHECK (consumed_yn IN ('Y','N'))
);

-- =====================================================================
-- BS_CALL_LOG : 호출 감사로그
-- =====================================================================
CREATE TABLE business_service.bs_call_log (
  request_id        varchar(80) PRIMARY KEY,
  trace_id          varchar(80),
  parent_request_id varchar(80),
  service_id        varchar(100) NOT NULL,
  service_version   varchar(20) NOT NULL,
  client_id         varchar(80) NOT NULL,
  user_id           varchar(50),
  department_code   varchar(50),
  roles             varchar(300),
  requested_at      timestamp NOT NULL,
  completed_at      timestamp,
  elapsed_ms        integer,
  status_code       varchar(20) NOT NULL,
  http_status       integer,
  error_code        varchar(50),
  error_id          varchar(80),
  parameter_hash    varchar(128),
  result_count      integer,
  sensitive_access_flag char(1) NOT NULL DEFAULT 'N' CHECK (sensitive_access_flag IN ('Y','N')),
  remote_ip         varchar(64),
  auth_type         varchar(30),
  authorization_result varchar(20),
  server_instance   varchar(100),
  application_version varchar(30)
);
CREATE INDEX ix_bs_call_log_01 ON business_service.bs_call_log(service_id, requested_at DESC);
CREATE INDEX ix_bs_call_log_02 ON business_service.bs_call_log(user_id, requested_at DESC);
CREATE INDEX ix_bs_call_log_03 ON business_service.bs_call_log(status_code, requested_at DESC);

-- =====================================================================
-- BS_ACCESS_ENTITY : 접근 업무객체 이력
-- =====================================================================
CREATE TABLE business_service.bs_access_entity (
  request_id        varchar(80) NOT NULL REFERENCES business_service.bs_call_log(request_id),
  entity_type       varchar(50) NOT NULL,
  entity_id         varchar(200) NOT NULL,
  PRIMARY KEY (request_id, entity_type, entity_id)
);
