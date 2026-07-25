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

## 소스 업데이트 반영

master가 갱신되면 VM에서:

```bash
sudo bash /path/to/Claude_Test/deploy/oci-setup.sh   # 재실행 시 fetch+rebuild+재기동
```

또는 수동으로:

```bash
cd /opt/ifrs17/src && sudo -u ifrs17 git pull && sudo -u ifrs17 ./mvnw -q clean package -DskipTests
sudo install -o ifrs17 -g ifrs17 target/ifrs17-business-service-layer.jar /opt/ifrs17/
sudo systemctl restart ifrs17
```
