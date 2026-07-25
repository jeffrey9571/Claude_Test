#!/usr/bin/env bash
#
# master 변경분을 VM에 반영하는 경량 스크립트 (pull → build → restart).
# oci-setup.sh로 최초 설치가 끝난 뒤 사용한다. root로 실행(예: sudo bash update.sh).
# GitHub Actions 자동 배포(.github/workflows/deploy.yml)와 수동 업데이트 모두 이 스크립트를 쓴다.
set -euo pipefail

APP_USER="ifrs17"
APP_HOME="/opt/ifrs17"
APP_SRC="${APP_HOME}/src"
JAR_NAME="ifrs17-business-service-layer.jar"
BRANCH="${REPO_BRANCH:-master}"

JAVA_HOME_DIR="$(dirname "$(dirname "$(readlink -f "$(command -v java)")")")"

echo "==> master 최신 소스 받기"
sudo -u "${APP_USER}" git -C "${APP_SRC}" fetch origin "${BRANCH}"
sudo -u "${APP_USER}" git -C "${APP_SRC}" reset --hard "origin/${BRANCH}"

echo "==> 빌드 (mvnw)"
sudo -u "${APP_USER}" env JAVA_HOME="${JAVA_HOME_DIR}" bash -c \
  "cd '${APP_SRC}' && ./mvnw -q -B clean package -DskipTests"

echo "==> jar 교체 및 재기동"
install -o "${APP_USER}" -g "${APP_USER}" \
  "${APP_SRC}/target/${JAR_NAME}" "${APP_HOME}/${JAR_NAME}"
systemctl restart ifrs17

echo "==> 배포 완료: $(date -Is)"
