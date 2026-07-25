#!/usr/bin/env bash
#
# IFRS17 Business Service Layer - OCI(Oracle Cloud) 원클릭 배포 스크립트
# ---------------------------------------------------------------------------
# 대상 : OCI Always Free VM (Ubuntu 22.04 / 24.04, ARM64 Ampere 또는 x86)
# 결과 : master 소스를 빌드·구동하고, DuckDNS 도메인 + Caddy 자동 HTTPS까지 구성
#        최종 접속:  https://<DUCKDNS_SUBDOMAIN>.duckdns.org/console/index.html
#
# 사용법:
#   1) 아래 "사용자 설정" 3개 값을 채운다 (또는 환경변수로 전달).
#   2) chmod +x oci-setup.sh && sudo ./oci-setup.sh
#
# ── 스크립트 밖에서 반드시 먼저 해둘 2가지 (자동화 불가) ─────────────────────
#   (A) OCI 콘솔 > 네트워킹 > VCN > Security List(또는 NSG) 인그레스 규칙에
#       0.0.0.0/0 → TCP 80, 443 허용 추가. (안 하면 HTTPS 인증서 발급/접속 실패)
#   (B) DuckDNS(https://www.duckdns.org) 로그인 → 서브도메인 생성 → 토큰 확인.
# ───────────────────────────────────────────────────────────────────────────
set -euo pipefail

# ===== 사용자 설정 (여기만 수정) ===========================================
DUCKDNS_SUBDOMAIN="${DUCKDNS_SUBDOMAIN:-ifrs17-bsl}"          # -> ifrs17-bsl.duckdns.org
DUCKDNS_TOKEN="${DUCKDNS_TOKEN:-PUT-YOUR-DUCKDNS-TOKEN-HERE}" # DuckDNS 계정 토큰
DB_PASSWORD="${DB_PASSWORD:-change-me-strong-password}"        # PostgreSQL ifrs17_app 비밀번호
REPO_URL="${REPO_URL:-https://github.com/jeffrey9571/Claude_Test.git}"
REPO_BRANCH="${REPO_BRANCH:-master}"
# ==========================================================================

APP_USER="ifrs17"
APP_HOME="/opt/ifrs17"
APP_SRC="${APP_HOME}/src"
JAR_NAME="ifrs17-business-service-layer.jar"
DOMAIN="${DUCKDNS_SUBDOMAIN}.duckdns.org"
DB_NAME="ifrs17"
DB_USER="ifrs17_app"

log() { printf '\n\033[1;34m==> %s\033[0m\n' "$*"; }

if [[ $EUID -ne 0 ]]; then echo "root로 실행하세요: sudo ./oci-setup.sh"; exit 1; fi
if [[ "$DUCKDNS_TOKEN" == "PUT-YOUR-DUCKDNS-TOKEN-HERE" ]]; then
  echo "DUCKDNS_TOKEN을 먼저 설정하세요 (스크립트 상단 또는 환경변수)."; exit 1
fi

# --- 0. 기본 패키지 --------------------------------------------------------
log "0/8 시스템 업데이트 및 기본 패키지 설치"
export DEBIAN_FRONTEND=noninteractive
apt-get update -y
apt-get install -y curl git ca-certificates gnupg lsb-release cron

# --- 1. JDK 21 (Adoptium Temurin) -----------------------------------------
log "1/8 JDK 21 설치"
if ! java -version 2>&1 | grep -q 'version "21'; then
  mkdir -p /etc/apt/keyrings
  curl -fsSL https://packages.adoptium.net/artifactory/api/gpg/key/public \
    | gpg --dearmor -o /etc/apt/keyrings/adoptium.gpg
  echo "deb [signed-by=/etc/apt/keyrings/adoptium.gpg] https://packages.adoptium.net/artifactory/deb $(lsb_release -cs) main" \
    > /etc/apt/sources.list.d/adoptium.list
  apt-get update -y
  apt-get install -y temurin-21-jdk
fi
java -version

# --- 2. PostgreSQL ---------------------------------------------------------
log "2/8 PostgreSQL 설치 및 DB/계정 생성"
apt-get install -y postgresql postgresql-contrib
systemctl enable --now postgresql

# 역할 + DB + 스키마 생성 (이미 있으면 건너뜀). Flyway가 테이블·시드를 채운다.
sudo -u postgres psql -v ON_ERROR_STOP=1 <<SQL
DO \$\$
BEGIN
  IF NOT EXISTS (SELECT FROM pg_roles WHERE rolname = '${DB_USER}') THEN
    CREATE ROLE ${DB_USER} LOGIN PASSWORD '${DB_PASSWORD}';
  ELSE
    ALTER ROLE ${DB_USER} WITH PASSWORD '${DB_PASSWORD}';
  END IF;
END
\$\$;
SELECT 'CREATE DATABASE ${DB_NAME} OWNER ${DB_USER}'
  WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = '${DB_NAME}')\gexec
SQL

sudo -u postgres psql -v ON_ERROR_STOP=1 -d "${DB_NAME}" <<SQL
CREATE SCHEMA IF NOT EXISTS business_service AUTHORIZATION ${DB_USER};
GRANT ALL ON SCHEMA business_service TO ${DB_USER};
SQL

# --- 3. 앱 사용자 및 소스 빌드 --------------------------------------------
log "3/8 소스 내려받기 및 빌드 (mvnw)"
id -u "${APP_USER}" &>/dev/null || useradd -r -m -d "${APP_HOME}" -s /usr/sbin/nologin "${APP_USER}"
mkdir -p "${APP_HOME}"
if [[ -d "${APP_SRC}/.git" ]]; then
  git -C "${APP_SRC}" fetch origin "${REPO_BRANCH}"
  git -C "${APP_SRC}" checkout "${REPO_BRANCH}"
  git -C "${APP_SRC}" reset --hard "origin/${REPO_BRANCH}"
else
  git clone -b "${REPO_BRANCH}" "${REPO_URL}" "${APP_SRC}"
fi
chown -R "${APP_USER}:${APP_USER}" "${APP_HOME}"

# JAVA_HOME 자동 탐지
JAVA_HOME_DIR="$(dirname "$(dirname "$(readlink -f "$(command -v java)")")")"
sudo -u "${APP_USER}" env JAVA_HOME="${JAVA_HOME_DIR}" bash -c \
  "cd '${APP_SRC}' && ./mvnw -q -B clean package -DskipTests"
install -o "${APP_USER}" -g "${APP_USER}" \
  "${APP_SRC}/target/${JAR_NAME}" "${APP_HOME}/${JAR_NAME}"

# --- 4. systemd 서비스 등록 (DB 비밀번호는 환경변수로 주입) ----------------
log "4/8 systemd 서비스 등록"
cat > /etc/systemd/system/ifrs17.service <<UNIT
[Unit]
Description=IFRS17 Business Service Layer
After=network.target postgresql.service
Wants=postgresql.service

[Service]
User=${APP_USER}
Environment=JAVA_HOME=${JAVA_HOME_DIR}
Environment=SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/${DB_NAME}
Environment=SPRING_DATASOURCE_USERNAME=${DB_USER}
Environment=SPRING_DATASOURCE_PASSWORD=${DB_PASSWORD}
ExecStart=${JAVA_HOME_DIR}/bin/java -jar ${APP_HOME}/${JAR_NAME}
SuccessExitStatus=143
Restart=on-failure
RestartSec=5

[Install]
WantedBy=multi-user.target
UNIT
systemctl daemon-reload
systemctl enable --now ifrs17.service

# --- 5. DuckDNS 갱신 (초기 1회 + 5분마다 cron) ----------------------------
log "5/8 DuckDNS 도메인 연결"
DUCK_SCRIPT="${APP_HOME}/duckdns-update.sh"
cat > "${DUCK_SCRIPT}" <<DUCK
#!/usr/bin/env bash
curl -fsS "https://www.duckdns.org/update?domains=${DUCKDNS_SUBDOMAIN}&token=${DUCKDNS_TOKEN}&ip=" -o "${APP_HOME}/duckdns.log"
DUCK
chmod +x "${DUCK_SCRIPT}"
"${DUCK_SCRIPT}"
echo "DuckDNS 응답: $(cat "${APP_HOME}/duckdns.log")   (OK 이면 정상)"
( crontab -l 2>/dev/null | grep -v duckdns-update.sh; echo "*/5 * * * * ${DUCK_SCRIPT}" ) | crontab -

# --- 6. OS 방화벽 (80/443/22 허용) ----------------------------------------
log "6/8 OS 방화벽 규칙 (80/443 개방)"
# OCI Ubuntu는 iptables에 기본 REJECT가 있어 80/443을 명시 허용해야 한다.
iptables -C INPUT -p tcp --dport 80  -j ACCEPT 2>/dev/null || iptables -I INPUT -p tcp --dport 80  -j ACCEPT
iptables -C INPUT -p tcp --dport 443 -j ACCEPT 2>/dev/null || iptables -I INPUT -p tcp --dport 443 -j ACCEPT
apt-get install -y netfilter-persistent iptables-persistent || true
netfilter-persistent save || true

# --- 7. Caddy 설치 + 자동 HTTPS -------------------------------------------
log "7/8 Caddy 설치 및 HTTPS 리버스 프록시 구성"
if ! command -v caddy &>/dev/null; then
  curl -1sLf 'https://dl.cloudsmith.io/public/caddy/stable/gpg.key' \
    | gpg --dearmor -o /usr/share/keyrings/caddy-stable-archive-keyring.gpg
  curl -1sLf 'https://dl.cloudsmith.io/public/caddy/stable/debian.deb.txt' \
    > /etc/apt/sources.list.d/caddy-stable.list
  apt-get update -y
  apt-get install -y caddy
fi
cat > /etc/caddy/Caddyfile <<CADDY
${DOMAIN} {
    encode gzip
    reverse_proxy localhost:8080
}
CADDY
systemctl reload caddy || systemctl restart caddy

# --- 8. 완료 --------------------------------------------------------------
log "8/8 완료"
cat <<DONE

────────────────────────────────────────────────────────────
 배포 완료. Caddy가 Let's Encrypt 인증서를 발급하는 데 최대 1~2분 걸립니다.

   콘솔 화면 :  https://${DOMAIN}/console/index.html
   실행 API  :  https://${DOMAIN}/api/business-services/v1/{serviceId}:execute

 상태 확인:
   systemctl status ifrs17     # 앱
   systemctl status caddy      # HTTPS
   journalctl -u ifrs17 -f     # 앱 로그

 접속이 안 되면 점검:
   1) OCI Security List/NSG 인그레스에 TCP 80,443 (0.0.0.0/0) 열었는가
   2) DuckDNS 응답이 OK 인가:  cat ${APP_HOME}/duckdns.log
   3) 인증서 발급 로그:         journalctl -u caddy -f
────────────────────────────────────────────────────────────
DONE
