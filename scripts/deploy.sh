#!/bin/bash

# =================================================================
#  Retrip 프로젝트 배포 및 SSL 인증서 자동화 스크립트
# =================================================================

echo "환경 설정을 시작합니다..."
if [ ! -f .env ]; then
    echo ".env 파일이 없습니다. 스크립트를 중단합니다."
    exit 1
fi

export $(grep -v '^#' .env | xargs)

MAIN_DOMAIN="retrip.kr"
CERT_FILE_PATH="./data/certbot/conf/live/$MAIN_DOMAIN/fullchain.pem"
NGINX_CONF_DIR="./nginx/conf.d"
NGINX_CONTAINER_NAME="nginx"
WHITELIST_FILE="$NGINX_CONF_DIR/allowed_ips.rules"

if command -v docker-compose &> /dev/null; then
    DOCKER_COMPOSE="docker-compose"
elif docker compose version &> /dev/null; then
    DOCKER_COMPOSE="docker compose"
else
    echo "Docker Compose를 찾을 수 없습니다! 스크립트를 중단합니다."
    exit 1
fi

echo "Docker Compose 명령어: $DOCKER_COMPOSE"
sudo mkdir -p $NGINX_CONF_DIR
sudo mkdir -p ./data/certbot/conf
sudo mkdir -p ./data/certbot/www

setup_whitelist() {
    echo "화이트리스트 설정을 확인합니다..."

    # 환경변수에서 화이트리스트 설정 확인
    if [ -z "$WHITELIST_IPS" ]; then
        echo "WARNING: WHITELIST_IPS 환경변수가 설정되지 않았습니다."
        echo "모든 IP에서 접근이 허용됩니다."

        # 기본 설정 (모든 IP 허용)
        sudo tee "$WHITELIST_FILE" > /dev/null << EOF

EOF
        return 0
    fi

    echo "화이트리스트가 설정되었습니다: $WHITELIST_IPS"

    # 화이트리스트 파일 생성
    sudo tee "$WHITELIST_FILE" > /dev/null << EOF
EOF

    # 쉼표로 구분된 IP들을 처리
    IFS=',' read -ra IPS <<< "$WHITELIST_IPS"
    for ip in "${IPS[@]}"; do
        # 공백 제거
        ip=$(echo "$ip" | xargs)

        # IP 형식 검증
        if [[ $ip =~ ^[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}(/[0-9]{1,2})?$ ]]; then
            echo "allow $ip;" | sudo tee -a "$WHITELIST_FILE" > /dev/null
            echo "  - 허용된 IP: $ip"
        else
            echo "WARNING: 잘못된 IP 형식입니다: $ip"
        fi
    done

    # 마지막에 deny all 추가
    echo "deny all;" | sudo tee -a "$WHITELIST_FILE" > /dev/null

    echo "화이트리스트 설정이 완료되었습니다."
    echo "설정된 내용:"
    cat "$WHITELIST_FILE"
}

check_certificate() {
    # sudo 권한으로 파일 존재 확인
    if sudo [ -f "$CERT_FILE_PATH" ]; then
        echo "기존 SSL 인증서를 찾았습니다: $CERT_FILE_PATH"

        # sudo 권한으로 openssl 실행
        if sudo openssl x509 -checkend 2592000 -noout -in "$CERT_FILE_PATH" > /dev/null 2>&1; then
            echo "인증서가 유효합니다. (30일 이상 남음)"
            return 0
        else
            echo "인증서가 30일 이내에 만료됩니다. 갱신이 필요합니다."
            return 1
        fi
    else
        echo "SSL 인증서가 존재하지 않습니다."
        return 1
    fi
}

renew_certificate() {
    echo "인증서 갱신을 시도합니다..."
    $DOCKER_COMPOSE run --rm certbot renew

    if [ $? -eq 0 ]; then
        echo "인증서 갱신 성공!"
        return 0
    else
        echo "인증서 갱신 실패. 새로 발급을 시도합니다."
        return 1
    fi
}

issue_new_certificate() {
    echo "새로운 SSL 인증서 발급을 시작합니다."

    echo "인증서 발급을 위해 임시 Nginx 설정을 적용합니다."
    sudo cp ./nginx-cert-setup.conf $NGINX_CONF_DIR/default.conf

    $DOCKER_COMPOSE up -d nginx

    echo "Nginx가 시작될 때까지 10초 대기합니다..."
    sleep 10

    echo "Certbot으로 SSL 인증서를 요청합니다..."
    $DOCKER_COMPOSE run --rm certbot certonly \
      --webroot --webroot-path=/var/www/certbot \
      -d retrip.kr \
      -d www.retrip.kr \
      -d api.retrip.kr \
      -d grafana.retrip.kr \
      -d prometheus.retrip.kr \
      --email $CERTBOT_EMAIL --agree-tos --no-eff-email

    if [ $? -ne 0 ]; then
        echo "SSL 인증서 발급에 실패했습니다."
        echo "Let's Encrypt 발급 제한에 걸렸을 가능성이 있습니다."
        echo "다음 중 하나를 시도해보세요:"
        echo "1. 기존 인증서 파일을 수동으로 복사"
        echo "2. 발급 제한 해제까지 대기"
        echo "3. 스테이징 환경에서 테스트"

        $DOCKER_COMPOSE logs nginx
        return 1
    fi

    echo "SSL 인증서 발급 성공!"
    $DOCKER_COMPOSE down
    return 0
}

# 화이트리스트 설정
setup_whitelist

if check_certificate; then
    echo "기존 인증서를 사용합니다."
else
    echo "인증서 처리가 필요합니다."

    if [ -f "$CERT_FILE_PATH" ]; then
        if ! renew_certificate; then
            echo "갱신 실패. 새 인증서 발급을 건너뜁니다."
            echo "기존 인증서를 그대로 사용합니다."
        fi
    else
        if ! issue_new_certificate; then
            echo "인증서 발급 실패. HTTP로 서비스를 시작합니다."
            echo "수동으로 인증서를 설정한 후 다시 배포하세요."
        fi
    fi
fi

echo "최종 운영 설정을 적용하고 모든 서비스를 시작합니다."

echo "운영용 Nginx 설정을 적용합니다."
sudo cp ./nginx-prod.conf $NGINX_CONF_DIR/default.conf

echo "기존 컨테이너를 종료합니다..."
$DOCKER_COMPOSE down

echo "새로운 Docker 이미지를 pull 합니다"
$DOCKER_COMPOSE pull retrip-app

echo "모든 서비스를 시작/업데이트합니다..."
$DOCKER_COMPOSE up -d

echo "Nginx 설정을 리로드합니다..."
$DOCKER_COMPOSE exec $NGINX_CONTAINER_NAME nginx -s reload

echo "================================================================="
echo "배포가 성공적으로 완료되었습니다!"
echo "서비스 URL: https://$MAIN_DOMAIN"
if [ -f "$WHITELIST_FILE" ] && grep -q "deny all" "$WHITELIST_FILE"; then
    echo "화이트리스트가 적용되었습니다. 허용된 IP 주소에서만 모니터링 도구 접근이 가능합니다."
fi
echo "================================================================="