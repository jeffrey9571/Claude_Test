# IFRS17 Business Service Layer

`IFRS17_Business_Service_Layer_DBINC_Development_Specification_v1.3.docx`
(문서번호 IFRS17-BSL-SDD-001, v1.3) 기준으로 구현한 Phase 1(Read 중심) Business
Service Layer입니다.

## 기술 스택

- Java 8, Spring Boot 2.7 (Spring MVC / Spring Data JPA)
- PostgreSQL (운영), H2 PostgreSQL-호환 모드 (테스트)
- Flyway (DB 마이그레이션)
- Maven

## 구조 (4.1 권장 패키지 구조)

```
com.koreanre.ifrs17.businessservice
  api.controller        REST Controller (실행/Catalog/CallLog/Console)
  api.dto.request/response  표준 Request/Response DTO
  core.context           ServiceContext, RequestContextResolver
  core.dispatcher         BusinessServiceDispatcher
  core.executor           BusinessServiceExecutor, BusinessServiceHandler
  core.validator          ValidationUtils
  core.security           ClientValidator, AuthorizationService
  core.audit              AuditLogger
  core.masking            MaskingPolicy
  core.metadata           ServiceMetadataRepository, CatalogQueryService
  core.response           StandardResponseBuilder
  core.exception          ErrorCode, BusinessServiceException 및 하위 예외
  domain.{closing,journal,expense,statement,csm}  5개 파일럿 서비스
  legacy.adapter / legacy.existing  Legacy Adapter 및 기존 Service Stub
  console                 Business Service Console 백엔드
  persistence.model/mapper  JPA Entity / Repository
```

## 공통 처리 Framework

`BusinessServiceExecutor`가 10.4 의사코드의 표준 처리 순서를 그대로 구현합니다:

`resolveContext → validateClient → loadMetadata → authorizeService → writeAuditStart
→ invokeHandler(Timeout 보호) → maskResult → buildSuccessResponse → writeAuditSuccess`,
예외 발생 시 `convertStandardError → writeAuditFailure`.

## REST API (5장)

- `POST /api/business-services/v1/{serviceId}:execute`
- `GET  /api/business-services/v1/catalog`, `GET /catalog/{serviceId}`
- `GET  /api/business-services/v1/calls/{requestId}`

필수 Header: `Authorization`, `X-Client-ID` (필수), `X-User-ID` (SSO 대체 임시 Header),
`X-Request-ID`/`X-Trace-ID` (선택, 미입력 시 서버 생성).

> 미결사항 #2(현행 SSO Token/Session에서 취득 가능한 사용자 속성)가 확정되기 전까지는
> `X-User-ID`/`X-User-Roles`/`X-Department-Code` Header를 SSO Context 소스로 임시
> 사용합니다. 운영 반영 전 `RequestContextResolver`를 실제 SSO Token 파싱 로직으로
> 교체해야 합니다.

## 5개 파일럿 서비스 (9장)

`IFRS17.CLOSING.STATUS`, `IFRS17.JOURNAL.STATUS`, `IFRS17.EXPENSE.STATUS`,
`IFRS17.STATEMENT.STATUS`, `IFRS17.CSM.STATUS` — 각 서비스는 `Handler → LegacyAdapter
→ Existing*Service(Stub)` 구조입니다. Stub은 개발/테스트용이며, DB-INC 착수 분석 후
"현행 매핑서"에 따라 실제 기존 Service 호출로 교체(Bean 대체)해야 합니다
(10.3 Legacy Adapter 방식).

## Business Service Console (8장)

- 백엔드: `/api/console/services` (CON-01 서비스 명세 관리 CRUD, Bean 존재/Interface
  구현/Service ID 일치 검증 포함)
- Test 화면(CON-02)은 별도 API 없이 운영과 동일한 `/execute` Endpoint를 그대로
  사용합니다(8.6).
- 프론트엔드: `src/main/resources/static/console/index.html` (정적 페이지, 서버 기동
  후 `/console/index.html`로 접근)
- Console API는 `X-User-Roles`에 `BS_CONSOLE_ADMIN`을 요구합니다(6.2 운영자 권한 제한).

## 빌드 / 테스트

```bash
mvn compile
mvn test
mvn spring-boot:run   # PostgreSQL 접속정보는 application.yml 또는 환경변수로 설정
```

테스트는 `src/test/resources/application.yml`의 H2(PostgreSQL 호환 모드)로 Flyway
마이그레이션을 그대로 적용해 실행됩니다.

## 데이터베이스 (7장)

`business_service` Schema, `BS_` 접두 테이블 13종. `src/main/resources/db/migration/`
`V1__init_schema.sql`(DDL), `V2__seed_pilot_services.sql`(파일럿 5종 Catalog +
개발용 Client `MCP-IFRS17-01` 시드).

## 남은 작업 / 미결사항 (14장)

- 5개 서비스별 실제 기존 Service/DAO 매핑 ("현행 매핑서")
- 실제 SSO Token/Session 연동으로 `RequestContextResolver` 교체
- IFRS17 권한 테이블 연계 (`AuthorizationService`에 기존 권한 Service 결합)
- OpenAPI/Swagger 산출물, 배포·Rollback 절차서 등 13.2 제출 산출물

이 저장소의 구현 범위는 착수 기준서(v1.3)가 정의한 Phase 1(READ 서비스 5종 +
공통 Framework + 단순화된 Console)까지이며, Action 서비스·복잡한 Lifecycle/승인
Workflow·대시보드는 8.1/1.4에 따라 의도적으로 제외했습니다.
