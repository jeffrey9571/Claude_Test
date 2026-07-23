## 로컬에서 화면 바로 보기 (PostgreSQL 설치 불필요)

> 이 가이드는 `local_demo` 브랜치에 포함된 `local` 프로파일(H2 인메모리)을 사용한다.
> `master` 브랜치에는 없으므로, 화면을 빠르게 확인하려면 반드시 `local_demo` 브랜치를 사용한다.

DB(PostgreSQL)와 Maven을 설치하지 않아도 되고, 앱을 띄우면 H2 인메모리 DB에 Flyway가
테이블과 파일럿 5종 시드 데이터를 자동 생성한다. 관리 콘솔 화면과 5개 서비스 API를 바로
확인할 수 있다.

### 준비물

- **JDK 21 (권장)** — <https://adoptium.net/temurin/releases/?version=21> 에서 OS/x64/JDK 설치
  - ⚠️ JDK 22 이상(예: 25)은 이 프로젝트가 쓰는 Spring Boot 2.1.6 / Lombok과 호환되지 않아
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
java -version
::     openjdk version "21..." 로 나오면 정상

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
