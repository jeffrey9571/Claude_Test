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

## 로컬에서 화면 바로 보기 (PostgreSQL 설치 불필요)

> 이 가이드는 `local_demo` 브랜치에 포함된 `local` 프로파일(H2 인메모리)을 사용한다.
> `master` 브랜치에는 없으므로, 화면을 빠르게 확인하려면 반드시 `local_demo` 브랜치를 사용한다.

DB(PostgreSQL)와 Maven을 설치하지 않아도 되고, 앱을 띄우면 H2 인메모리 DB에 Flyway가
테이블과 파일럿 5종 시드 데이터를 자동 생성한다. 관리 콘솔 화면과 5개 서비스 API를 바로
확인할 수 있다.

### 준비물

- **JDK 21 (권장)** — <https://adoptium.net/temurin/releases/?version=21> 에서 OS/x64/JDK 설치
  - ⚠️ JDK 22 이상(예: 25)은 이 프로젝트가 쓰는 Spring Boot 2.7 / Lombok과 호환되지 않아
    빌드가 실패할 수 있다. **반드시 JDK 21을 사용**한다.
- 그 외 Maven·PostgreSQL은 불필요(Maven Wrapper `mvnw` 내장).

### 소스 내려받기

- **Git이 있으면**: `git clone -b local_demo https://github.com/jeffrey9571/Claude_Test.git`
- **Git이 없으면**: GitHub 저장소에서 `local_demo` 브랜치 선택 → **Code ▾ → Download ZIP** →
  압축 해제. (코드가 갱신될 때마다 ZIP을 다시 받아야 하므로, 반복 사용 시 Git 설치를 권장한다.)

### 실행 — Windows (명령 프롬프트)

같은 창에서 순서대로 실행한다. `JAVA_HOME`/`PATH`는 새 창을 열 때마다 다시 설정해야 한다.

```cmd
:: (1) JDK 21을 사용하도록 지정 — 아래 경로의 jdk-21... 폴더명은 실제 설치명으로 바꾼다
::     설치 폴더명 확인:  dir "%LOCALAPPDATA%\Programs\Eclipse Adoptium"
set JAVA_HOME=C:\Users\<사용자>\AppData\Local\Programs\Eclipse Adoptium\jdk-21.0.x.x-hotspot
set PATH=%JAVA_HOME%\bin;%PATH%
java -version                :: openjdk version "21..." 로 나오면 정상

:: (2) 프로젝트 폴더로 이동 (예시 경로)
D:
cd D:\Claude_Test-local_demo\Claude_Test-local_demo

:: (3) 빌드 (소스가 바뀌었거나 ZIP을 새로 받은 경우 필요)
mvnw.cmd clean package -DskipTests

:: (4) 콘솔 한글 로그가 깨지지 않도록 UTF-8로 전환 후 실행
chcp 65001
java -jar target\ifrs17-business-service-layer.jar --spring.profiles.active=local
```

> 이미 빌드해 둔 `target\...jar`이 있고 소스 변경이 없다면 (3)을 건너뛰고 (1)(4)만 하면 된다.

### 실행 — macOS / Linux

```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 21 2>/dev/null || echo "$JAVA_HOME")   # macOS 예시
./mvnw clean package -DskipTests
java -jar target/ifrs17-business-service-layer.jar --spring.profiles.active=local
```

### 접속

- **관리 콘솔**: <http://localhost:8080/console/index.html>
  - CON-01 서비스 명세 관리(목록·상세·수정), CON-02 서비스 Test(Request 입력→실행→응답 확인)
  - 화면 상단 인증 Header 입력칸은 기본값이 채워져 있어 그대로 **실행**만 누르면 된다.
- **H2 콘솔(선택)**: <http://localhost:8080/h2-console>
  - JDBC URL `jdbc:h2:mem:ifrs17_bsl`, 사용자 `sa`, 비밀번호 공란
- **서버 종료**: 실행한 창에서 `Ctrl + C`

H2는 인메모리이므로 애플리케이션을 종료하면 데이터가 초기화된다(데모 목적). 운영은
`master` 브랜치의 기본 프로파일(PostgreSQL)을 사용한다.

### 요청 흐름 로그로 확인하기

`local_demo` 브랜치에는 요청이 서버의 각 계층을 통과할 때마다 진입 로그(`>>> [진입] ...`)가
콘솔에 출력된다. 콘솔 화면에서 버튼을 누르면 `java -jar`를 실행한 창에 아래처럼 흐름이 찍힌다.

```
>>> [진입] BusinessServiceController.execute() - serviceId=IFRS17.CLOSING.STATUS
>>> [진입] BusinessServiceExecutor.execute() - 표준 처리 순서 시작
    [1단계] Context 생성 → [2단계] Client 검증 → [3단계] Catalog 조회 →
    [4단계] 권한 확인 → [5단계] 감사 시작 → [6단계] Handler 실행 →
    [7단계] 마스킹 → [8단계] 감사 성공
>>> [진입] ClosingStatusBusinessService.process() - 결산 진행상태 조회
>>> [진입] ClosingStatusLegacyAdapter.findStatus() - 기존 결산 Service 호출
>>> [진입] ExistingClosingServiceStub.findClosingStatus() - (Stub) 현행 결산 데이터 생성
```

### 자주 겪는 문제

| 증상 | 원인 / 해결 |
| --- | --- |
| `The JAVA_HOME environment variable is not defined correctly` | `set JAVA_HOME=...`을 안 했거나 경로 오타. `dir "%LOCALAPPDATA%\Programs\Eclipse Adoptium"`로 실제 폴더명 확인 후 다시 지정 |
| `cannot find symbol ... getXxx()` 빌드 오류 | JDK 22+ 사용 중. **JDK 21**로 교체하고 `set JAVA_HOME`을 21로 지정 |
| `java -version`이 계속 예전 버전으로 나옴 | `set JAVA_HOME` 뒤에 `set PATH=%JAVA_HOME%\bin;%PATH%`를 추가해야 `java`도 21을 가리킨다 |
| 콘솔 한글 로그가 `?`/깨짐 | 실행 전 같은 창에서 `chcp 65001` 실행(UTF-8 전환). 클래스·메서드명은 인코딩과 무관하게 항상 보인다 |
| `Port 8080 was already in use` | 이전에 띄운 서버가 살아있음. 해당 창에서 `Ctrl + C`로 종료 후 다시 실행 |

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

### 추가검토사항

- **서비스는 "최신 버전" 기준으로만 조회/관리됨**

  `BS_SERVICE_VERSION`은 하나의 `service_id`에 여러 `version`(1.0, 1.1, 2.0 ...)이
  동시에 존재할 수 있도록 설계되어 있고, **실행(런타임) 계층은 이를 완전히 지원**한다.
  `POST /execute` 호출 시 `serviceVersion`을 지정하면 `ServiceMetadataRepository
  .findActive(serviceId, version)`이 정확히 그 버전을 찾아 해당 Bean을 실행하며,
  버전별로 서로 다른 `implementation_bean`(Spring Bean)을 매핑할 수 있다.

  다만 아래 두 곳은 **서비스당 최신 버전(가장 최근 `effective_from`) 하나만** 보여주도록
  구현되어 있어, 한 Service ID에 여러 버전이 등록돼 있어도 예전 버전은 화면/응답에
  노출되지 않는다 (DB에는 남아있고 실제 호출은 계속 가능).

  - **Catalog API** (`GET /catalog`, `GET /catalog/{serviceId}`): `CatalogQueryService
    .toEntry()`가 `findFirstByServiceIdAndStatusCodeOrderByEffectiveFromDesc(serviceId,
    "ACTIVE")`로 ACTIVE 버전 중 최신 1건만 `activeVersion` 필드에 담아 반환한다.
  - **Console CON-01 서비스 명세 관리** (`/api/console/services`): `ServiceSpecService
    .requireLatestVersion()`이 항상 최신 버전만 조회/수정 대상으로 삼는다. 특정 Service
    ID의 전체 버전 이력을 목록으로 보여주는 API/화면은 없다.

  문서(IFRS17-BSL-SDD-001 v1.3)는 Catalog/Console 응답의 정확한 JSON 스키마를
  규정하지 않아, 이는 문서상 제약이 아니라 Phase 1 단순화 구현 과정에서 생긴 설계상
  공백이다. 다중 버전을 화면/API에서 목록으로 노출하려면 `CatalogEntry`에 `versions`
  배열 필드를 추가(하위호환 유지 가능)하고, Console에 버전 목록 조회 API
  (`GET /api/console/services/{serviceId}/versions`)를 추가하는 확장이 필요하다.

- **JSON 통신 구현 (넥사크로17 화면 연동)**

  현재 IFRS17 업무 화면은 **넥사크로17(Nexacro17)** 기반이며, 기존 화면·배치는
  `gfn_transaction`을 통해 **XML** 방식으로 서버와 통신한다. 반면 본 Business
  Service Layer는 설계서 2.1절("API 응답에는 화면 표시용 HTML이 아니라 구조화
  JSON을 반환한다")에 따라 **JSON 기반 REST API**로 구현되어 있어, 두 방식이
  서로 다르다.

  따라서 넥사크로17 화면에서 이 API를 직접 호출하려면 넥사크로17이 기본 제공하는
  `gfn_transaction`(XML 통신 함수)을 그대로 재사용할 수 없고, **별도의 JavaScript
  통신 로직(예: 넥사크로17의 External Script 영역에서 `XMLHttpRequest`/`fetch`
  등으로 JSON 요청·응답 처리)을 새로 구현**해야 한다.

  - **인증/Header 전달**: `Authorization`, `X-Client-ID`, `X-User-ID` 등 필수
    Header(5.2절)를 JS 통신 로직에서 직접 채워 넣어야 하며, 기존 SSO 세션 정보를
    넥사크로17 → JS로 어떻게 넘길지 확인 필요 (미결사항 #2와 연결됨)
  - **에러 처리**: 표준 Error Response(5.5절, `error.code`/`error.message`)를
    넥사크로17 화면의 기존 오류 처리 패턴과 어떻게 맞출지 정의 필요
  - **영향 범위**: 이 저장소(Business Service Layer)는 JSON API 제공까지가
    범위이며, 넥사크로17 화면 쪽 JS 통신 모듈 구현은 화면 개발 담당(별도 산출물)
    쪽 작업이다.

  (TODO: 구체적인 JS 통신 모듈 설계/구현 방식은 추후 보강)
