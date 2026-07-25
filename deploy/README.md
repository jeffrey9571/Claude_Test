# OCI 배포 가이드 (무료 서버 + HTTPS)

OCI(Oracle Cloud) Always Free VM에 master 소스를 올려 **언제 어디서든 접속 가능한
HTTPS 서버**로 구동하는 방법이다. 모든 구성요소가 무료다.

```
[브라우저] ──https──▶ [Caddy :443] ──▶ [Spring Boot :8080] ──▶ [PostgreSQL]
                          └ Let's Encrypt 인증서 자동 발급/갱신
도메인: ifrs17-bsl.duckdns.org (DuckDNS 무료)
```

최종 접속 주소: **`https://ifrs17-bsl.duckdns.org/console/index.html`**

## 준비물 (모두 무료)

| 항목 | 발급처 |
| --- | --- |
| OCI VM (Ubuntu, ARM 권장 2 OCPU/12GB) | <https://cloud.oracle.com> Always Free |
| DuckDNS 서브도메인 + 토큰 | <https://www.duckdns.org> (구글/GitHub 로그인) |

## 1) OCI 콘솔에서 먼저 할 일 (스크립트로 자동화 불가)

**네트워킹 > VCN > Security List(또는 NSG) > 인그레스 규칙 추가**

| Source | Protocol | Dest Port |
| --- | --- | --- |
| `0.0.0.0/0` | TCP | `80` |
| `0.0.0.0/0` | TCP | `443` |

> 이걸 빠뜨리면 HTTPS 인증서 발급도, 외부 접속도 안 된다. **가장 흔한 실수.**

## 2) DuckDNS에서 할 일

1. duckdns.org 로그인 → `ifrs17-bsl` 서브도메인 생성
2. 페이지 상단의 **token** 값 복사

## 3) VM에서 스크립트 실행

```bash
# VM에 SSH 접속 후
git clone -b master https://github.com/jeffrey9571/Claude_Test.git
cd Claude_Test/deploy

# 값 3개를 환경변수로 전달 (또는 oci-setup.sh 상단을 직접 수정)
sudo DUCKDNS_SUBDOMAIN=ifrs17-bsl \
     DUCKDNS_TOKEN=<복사한_토큰> \
     DB_PASSWORD=<원하는_DB_비밀번호> \
     bash oci-setup.sh
```

스크립트가 하는 일 (약 3~5분):

1. JDK 21(Temurin) 설치
2. PostgreSQL 설치 + `ifrs17` DB / `ifrs17_app` 계정 / `business_service` 스키마 생성
3. master 소스 clone → `mvnw`로 빌드
4. `systemd` 서비스(`ifrs17`)로 앱 등록·기동 (DB 비밀번호는 환경변수로 주입, 소스 무수정)
5. DuckDNS에 VM 공인 IP 연결 + 5분마다 자동 갱신(cron)
6. OS 방화벽 80/443 개방
7. Caddy 설치 → **Let's Encrypt HTTPS 자동 발급**

## 4) 접속 및 확인

```
https://ifrs17-bsl.duckdns.org/console/index.html
```

인증서 발급에 최초 1~2분 걸린다. 안 되면:

```bash
systemctl status ifrs17          # 앱 상태
journalctl -u ifrs17 -f          # 앱 로그
journalctl -u caddy -f           # 인증서 발급 로그
cat /opt/ifrs17/duckdns.log      # DuckDNS 응답(OK 여야 정상)
```

## 보안 주의

- 포트를 열면 **IP/도메인만 알면 누구나 콘솔에 접근**할 수 있다. 개인/데모용이 아니라면:
  - OCI Security List의 Source를 **내 IP만**(`<내공인IP>/32`)으로 제한, 또는
  - Caddy에 Basic Auth 추가(`basicauth` 지시어) 등 인증을 반드시 붙인다.
- 콘솔 API는 `X-User-Roles: BS_CONSOLE_ADMIN` 헤더를 요구한다(운영자 권한 제한).

## 소스 업데이트 반영 (수동)

master가 갱신되면 VM에서 경량 업데이트 스크립트 하나만 실행하면 된다
(pull → 빌드 → 재기동):

```bash
sudo bash /opt/ifrs17/src/deploy/update.sh
```

## 자동 배포 (GitHub Actions) — 권장

master에 push되는 즉시 GitHub가 VM에 SSH로 접속해 위 `update.sh`를 대신 실행한다.
**한 번 설정해두면, 이후 소스가 바뀌어도 VM에서 아무 명령도 칠 필요가 없다.**
공개(public) 저장소이므로 GitHub Actions 실행은 **무제한 무료**다.

```
master에 push  ──▶  GitHub Actions 자동 실행  ──▶  VM에 SSH → update.sh (pull·빌드·재기동)
```

워크플로우 파일은 이미 저장소에 포함돼 있다: `.github/workflows/deploy.yml`.
작동시키려면 **아래 두 단계만** 하면 된다.

### 1) VM 접속용 SSH 키 준비

가장 간단한 방법은 **OCI VM을 만들 때 사용한 SSH 키 쌍을 그대로 재사용**하는 것이다
(그 키의 공개키는 이미 VM에 등록돼 있다). 이때 개인키 파일 내용이 곧 `VM_SSH_KEY`다.

전용 배포 키를 새로 만들고 싶다면(더 안전) VM에서:

```bash
ssh-keygen -t ed25519 -f ~/deploy_key -N ""          # 키 쌍 생성
cat ~/deploy_key.pub >> ~/.ssh/authorized_keys        # 공개키를 VM에 등록
cat ~/deploy_key                                       # 이 개인키 전체를 VM_SSH_KEY에 넣는다
```

> OCI Ubuntu의 기본 사용자 `ubuntu`는 비밀번호 없이 `sudo`가 되므로(cloud-init 기본),
> 워크플로우의 `sudo bash ... update.sh`가 그대로 동작한다.

### 2) GitHub Secret 3개 등록

저장소 **Settings → Secrets and variables → Actions → New repository secret**에서:

| 이름 | 값 |
| --- | --- |
| `VM_HOST` | VM 공인 IP (예: `140.238.1.2`) |
| `VM_USER` | SSH 사용자 (OCI Ubuntu 기본 `ubuntu`) |
| `VM_SSH_KEY` | SSH **개인키 전체 내용** (`-----BEGIN ... END-----` 포함) |

> ⚠️ 개인키는 반드시 **Secret**으로만 넣는다. 소스코드·README에 절대 붙여넣지 않는다.
> Claude(작성자)는 이 Secret에 접근할 수 없으므로, 이 등록은 저장소 소유자가 직접 해야 한다.

### 동작 확인

- master에 push하거나, **Actions 탭 → Deploy to OCI VM → Run workflow**로 수동 실행
- 진행 상황은 **Actions 탭**에서 실시간 로그로 확인
- 실패 시: SSH 접속(키/IP/보안그룹 22번 포트), VM의 `/opt/ifrs17/src` 존재 여부부터 점검

> VM을 아직 안 만들었어도 무방하다. 워크플로우는 Secret이 없으면 실패만 할 뿐이니,
> 나중에 VM 준비 후 Secret 3개만 채우면 그때부터 자동 배포가 작동한다.
