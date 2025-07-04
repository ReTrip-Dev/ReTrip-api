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
CERT_FILE_PATH="/etc/letsencrypt/live/$MAIN_DOMAIN/fullchain.pem"
NGINX_CONF_DIR="./nginx/conf.d"
NGINX_CONTAINER_NAME="nginx"

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
sudo mkdir -p ./data/certbot/www

if [ ! -f "$CERT_FILE_PATH" ]; then
    echo "SSL 인증서가 존재하지 않습니다. Let's Encrypt 발급 절차를 시작합니다."

    echo "인증서 발급을 위해 임시 Nginx 설정을 적용합니다."
    sudo cp ./nginx-cert-setup.conf $NGINX_CONF_DIR/default.conf

    $DOCKER_COMPOSE up -d nginx

    echo "Nginx가 시작될 때까지 10초 대기합니다..."
    sleep 10

    echo "Certbot으로 SSL 인증서를 요청합니다..."
    $DOCKER_COMPOSE run --rm certbot certonly \
      --webroot --webroot-path=/var/www/certbot \
      -d retrip.kr -d www.retrip.kr -d api.retrip.kr \
      --email $CERTBOT_EMAIL --agree-tos --no-eff-email

    if [ $? -ne 0 ]; then
        echo "SSL 인증서 발급에 실패했습니다. Nginx 로그를 확인하세요."
        $DOCKER_COMPOSE logs nginx
        exit 1
    fi
    echo "SSL 인증서 발급 성공!"
    $DOCKER_COMPOSE down
else
    echo "SSL 인증서가 이미 존재합니다. 발급 단계를 건너뜁니다."
fi


echo "최종 운영 설정을 적용하고 모든 서비스를 시작합니다."

echo "운영용 Nginx 설정을 적용합니다."
sudo cp ./nginx-prod.conf $NGINX_CONF_DIR/default.conf

echo "새로운 Docker 이미지를 pull 합니다"
$DOCKER_COMPOSE pull retrip-app

echo "모든 서비스를 시작/업데이트합니다..."
$DOCKER_COMPOSE up -d

echo "Nginx 설정을 리로드합니다..."
$DOCKER_COMPOSE exec $NGINX_CONTAINER_NAME nginx -s reload

echo "================================================================="
echo "배포가 성공적으로 완료되었습니다!"
echo "서비스 URL: https://$MAIN_DOMAIN"
echo "================================================================="