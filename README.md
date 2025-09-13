# 🌄 ReTrip - 여행을 요약해주는 새로운 방식의 SNS

Retrip은 여행을 좋아하는 사람들을 위한 이미지 기반 여행 요약 SNS입니다. 사용자가 업로드한 여행 사진 속 메타데이터와 이미지 정보를 기반으로, 여행의 전체적인 분위기와 정보를 하나의 이미지로 요약해주는 특별한 서비스를 제공합니다.

## 프로젝트 소개 

- Retrip(Remember Your Trip)은 여행을 좋아하는 사람들을 위한 이미지 기반 여행 요약 SNS입니다.
- 사용자의 여행 사진을 업로드하면, 위치 및 시간 정보와 이미지 분석을 통해 여행 데이터를 추출합니다.
- 추출된 정보를 기반으로 여행 요약본을 생성하고, 하나의 이미지로 시각화하여 제공합니다.
- 생성된 요약 이미지는 SNS 피드처럼 공유하거나, 히스토리 기능을 통해서 추억 여행을 떠날 수 있습니다.

<br>

## 팀원 구성

<div align="center">

| **김용범** | **오일우** | 
| :------: |  :------: |
| [<img src="https://avatars.githubusercontent.com/u/88239689?v=4" height=150 width=150> <br/> @Bumnote](https://github.com/Bumnote) | [<img src="https://avatars.githubusercontent.com/u/53050413?v=4" height=150 width=150> <br/> @Oilwoo](https://github.com/Oilwoo) |
</div>

<br>

## 1. 개발 환경

- **Front**: HTML, Vue3, Vuetify3
- **Back-end**: Spring Boot 3.4.5, Java 21, Spring Data JPA, Spring Security
- **Database**: MySQL, Redis
- **AI/ML**: OpenAI GPT-4 Vision API
- **인프라**: Docker, Nginx, AWS S3
- **버전 및 이슈관리**: Github, Github Issues, Github Project
- **협업 툴**: Discord, Notion
- **서비스 배포 환경**: AWS EC2, Docker Compose

<br>

## 2. 채택한 개발 기술과 브랜치 전략

### 기술 스택 상세

#### Backend Framework
- **Spring Boot 3.4.5** (Java 21) - 최신 LTS 버전 사용으로 안정성 확보
- **Spring Web MVC** - RESTful API 구현
- **Spring Data JPA** - ORM을 통한 효율적인 데이터베이스 관리
- **Spring Security** - OAuth2 기반 소셜 로그인 (Kakao)

#### AI & 이미지 처리
- **OpenAI GPT-4 Vision API** - 이미지 분석 및 여행 패턴 인식
- **metadata-extractor 2.18.0** - EXIF 메타데이터 추출 (GPS, 시간 정보)
- **TwelveMonkeys ImageIO** - HEIF, HEIC 등 다양한 이미지 포맷 지원
- **Thumbnailator** - 이미지 리사이징 및 최적화

#### 모니터링
- **Spring Actuator** - 애플리케이션 상태 모니터링
- **Prometheus & Micrometer** - 메트릭 수집 및 모니터링

### 브랜치 전략
- **main**: 프로덕션 배포 브랜치
- **dev**: 개발 통합 브랜치
- **feature/기능명**: 기능 개발 브랜치
- **refactor/리팩토링명**: 리팩토링 브랜치

<br>

## 3. 프로젝트 구조

```
retrip-api/
├── src/main/java/ssafy/retrip/
│   ├── api/
│   │   ├── controller/          # REST API 엔드포인트
│   │   │   └── retrip/
│   │   │       ├── RetripController.java
│   │   │       └── response/    # API 응답 DTO
│   │   └── service/
│   │       ├── openai/          # OpenAI GPT 연동
│   │       │   ├── GptImageAnalysisService.java
│   │       │   ├── OpenAiClient.java
│   │       │   └── response/    # GPT 응답 모델
│   │       └── retrip/          # 핵심 비즈니스 로직
│   │           ├── RetripService.java
│   │           ├── ImageConverter.java
│   │           └── info/        # 도메인 정보 객체
│   ├── config/                  # 설정 클래스
│   │   ├── SecurityConfig.java  # Spring Security 설정
│   │   ├── OpenAiConfig.java    # OpenAI 클라이언트 설정
│   │   ├── RedisConfig.java     # Redis 설정
│   │   └── WebConfig.java       # CORS 설정
│   ├── domain/                  # JPA 엔티티
│   │   ├── retrip/
│   │   │   ├── Retrip.java      # 여행 요약 엔티티
│   │   │   ├── TimeSlot.java    # 시간대 Enum
│   │   │   └── RetripRepository.java
│   │   └── place/
│   │       └── RecommendationPlace.java  # 추천 장소 엔티티
│   └── utils/                   # 유틸리티 클래스
│       ├── ImageMetaDataUtil.java  # 이미지 메타데이터 추출
│       ├── CoordinateUtil.java     # GPS 좌표 처리
│       └── DistanceUtil.java       # 거리 계산
├── docker/                      # Docker 설정
├── nginx/                       # Nginx 설정 (SSL, 리버스 프록시)
├── scripts/                     # 배포 스크립트
└── src/main/resources/
    ├── application.yml          # 애플리케이션 설정
    └── analysis.prompt          # GPT 분석 프롬프트
```

<br>

## 4. 역할 분담

### 💁🏻‍♂️ 김용범
- AI 모델 연동 및 프롬프트 엔지니어링
- 이미지 메타데이터 추출 및 처리
- GPS 기반 위치 분석 로직 구현
- Docker 및 인프라 구성

<br>
    
### 💁🏻‍♂️ 오일우
- Spring Boot 백엔드 아키텍처 설계
- RESTful API 개발
- 데이터베이스 설계 및 JPA 구현
- Spring Security 및 OAuth2 인증 구현

<br>

## 5. 개발 기간 및 작업 관리

### 개발 기간

- 전체 개발 기간 : 2025-04-28 ~ ing
- 백엔드 API 개발 : 2025-04-28 ~ 2025-05-10
- AI 모델 연동 : 2025-05-11 ~ 2025-05-20
- 인프라 구축 : 2025-05-21 ~ 2025-05-25
- 테스트 및 최적화 : 2025-05-26 ~ ing

<br>

### 작업 관리

- Github Projects를 활용한 칸반 보드 운영
- 주 2회 정기 미팅을 통한 진행상황 공유
- Discord를 통한 실시간 커뮤니케이션

<br>

## 6. 신경 쓴 부분

### 🎯 이미지 처리 최적화
- 대용량 이미지 처리를 위한 메모리 효율적인 리사이징
- HEIF/HEIC 등 최신 이미지 포맷 지원
- 메타데이터 보존하면서 이미지 크기 최적화

### 🤖 AI 분석 정확도
- GPT-4 Vision 프롬프트 최적화로 분석 정확도 향상
- 이미지 메타데이터와 AI 분석 결과의 교차 검증
- 위치 정보 기반 맞춤형 추천 알고리즘

### 🔒 보안 및 안정성
- Spring Security를 통한 인증/인가 처리
- 이미지 업로드 시 악성 파일 검증
- Rate Limiting으로 API 남용 방지

### 📊 모니터링
- Prometheus + Grafana를 통한 실시간 모니터링
- 주요 비즈니스 메트릭 추적
- 에러 로깅 및 알림 시스템

<br>

## 7. 페이지별 기능

### 📸 이미지 업로드 및 분석
- 다중 이미지 업로드 (5~20장)
- 실시간 업로드 진행률 표시
- 이미지 미리보기 및 순서 조정

### 🗺️ 여행 요약 결과
- AI 기반 여행 스타일 분석 (MBTI, 여행 성향)
- 시각적 여행 요약 카드 생성
- 위치 기반 추천 장소 제공
- 여행 통계 (이동 거리, 주요 활동 시간대 등)

### 📱 SNS 공유
- 생성된 요약 이미지 다운로드
- 주요 SNS 플랫폼 공유 기능
- 고유 URL을 통한 결과 공유

<br>

## 8. 트러블 슈팅

### 🔧 대용량 이미지 처리 메모리 이슈
- **문제**: 고해상도 이미지 다량 업로드 시 OutOfMemoryError 발생
- **해결**: 
  - ImageIO 대신 TwelveMonkeys 라이브러리 사용
  - 스트리밍 방식으로 이미지 처리
  - 적절한 JVM 힙 메모리 설정

### 🌐 CORS 이슈
- **문제**: 프론트엔드-백엔드 간 CORS 정책 위반
- **해결**: 
  - WebConfig에서 명시적 CORS 설정
  - 환경별 허용 Origin 분리 관리

### ⚡ GPT API 응답 시간 최적화
- **문제**: GPT-4 Vision API 응답 시간이 길어 UX 저하
- **해결**: 
  - 이미지 사전 압축으로 요청 크기 감소
  - 비동기 처리 및 진행 상태 표시
  - 응답 캐싱으로 재요청 시 성능 향상

<br>

## 9. 개선 목표

- 🚀 실시간 분석 진행률 표시 (WebSocket)
- 🌍 다국어 지원 확대
- 📊 더 정교한 여행 패턴 분석
- 🎨 다양한 요약 템플릿 제공
- 👥 소셜 기능 강화 (팔로우, 좋아요, 댓글)
- 📱 모바일 앱 개발

<br>

## 10. 프로젝트 후기

### 💁🏻‍♂️ 김용범
AI 기술을 실제 서비스에 적용하면서 프롬프트 엔지니어링의 중요성을 깨달았습니다. 특히 이미지 메타데이터와 AI 분석을 결합하여 더 정확한 결과를 도출하는 과정이 흥미로웠습니다. Docker와 인프라 구성을 통해 DevOps 역량도 향상시킬 수 있었습니다.

<br>

### 💁🏻‍♂️ 오일우
Spring Boot와 JPA를 활용한 백엔드 개발 경험을 쌓을 수 있었고, 특히 대용량 이미지 처리와 성능 최적화 과정에서 많은 것을 배웠습니다. OAuth2 인증과 보안 설정을 구현하면서 실무에서 필요한 보안 지식도 습득할 수 있었습니다.

<br>

---

## 🚀 Quick Start

### 요구사항
- Java 21+
- MySQL 8.0+
- Redis 7.0+
- Docker & Docker Compose (선택사항)

### 로컬 환경 실행
```bash
# 1. 프로젝트 클론
git clone https://github.com/ReTrip-Dev/ReTrip-api.git
cd ReTrip-api

# 2. application-secret.yml 설정
# src/main/resources/application-secret.yml 파일 생성 후 필요한 설정 추가

# 3. 애플리케이션 실행
./gradlew bootRun
```

### API 테스트
```bash
# 이미지 업로드 및 분석
curl -X POST http://localhost:8080/api/images/uploads \
  -F "images=@photo1.jpg" \
  -F "images=@photo2.jpg" \
  -F "images=@photo3.jpg" \
  -F "images=@photo4.jpg" \
  -F "images=@photo5.jpg"
```

## 📞 문의

프로젝트에 대한 문의사항은 [Issues](https://github.com/ReTrip-Dev/ReTrip-api/issues)를 통해 남겨주세요.