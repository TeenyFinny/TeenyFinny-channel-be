# TeenyFinny Channel Backend (채널계)

TeenyFinny Channel Backend는 아이들과 청소년을 위한 금융 서비스 및 교육 플랫폼인 TeenyFinny의 **채널계(Channel System)** 역할을 담당하는 서버 애플리케이션입니다. 이 프로젝트는 사용자 인증, 계좌 관리, 금융 목표 설정, 투자, 그리고 코어 뱅킹 시스템(계정계)과의 연동 등을 처리합니다.

## 👥 팀 소개

| <img alt="profile" src ="https://github.com/yes2489.png" width ="100" height ="100" style="border-radius: 50%; object-fit: cover;"> | <img alt="profile" src ="https://github.com/JBL28.png" width ="100" height ="100" style="border-radius: 50%; object-fit: cover;"> | <img alt="profile" src ="https://github.com/mingQ28.png" width ="100" height ="100" style="border-radius: 50%; object-fit: cover;"> | <img alt="profile" src ="https://github.com/hyojeongbae.png" width ="100" height ="100" style="border-radius: 50%; object-fit: cover;"> | <img alt="profile" src ="https://github.com/yangyanghyunjung.png" width ="100" height ="100" style="border-radius: 50%; object-fit: cover;"> | <img alt="profile" src ="https://github.com/CHICHIT.png" width ="100" height ="100" style="border-radius: 50%; object-fit: cover;"> |
| :---------------------------------------------------------------------------------------------------------------------------------: | :-------------------------------------------------------------------------------------------------------------------------------: | :---------------------------------------------------------------------------------------------------------------------------------: | :-------------------------------------------------------------------------------------------------------------------------------------: | :------------------------------------------------------------------------------------------------------------------------------------------: | :---------------------------------------------------------------------------------------------------------------------------------: |
|                                                             양은서 (PM)                                                             |                                                            이정복(PL)                                                             |                                                               박민서                                                                |                                                                 배효정                                                                  |                                                                    양현정                                                                    |                                                               이의섭                                                                |
|                                                [yes2489](https://github.com/yes2489)                                                |                                                 [JBL28](https://github.com/JBL28)                                                 |                                                [mingQ28](https://github.com/mingQ28)                                                |                                              [hyojeongbae](https://github.com/hyojeongbae)                                              |                                           [yangyanghyunjung](https://github.com/yangyanghyunjung)                                            |                                                [CHICHIT](https://github.com/CHICHIT)                                                |

---

## 🛠 기술 스택 (Tech Stack)

- **Language**: Java 17
- **Framework**: Spring Boot 3.5.7
- **Build Tool**: Gradle
- **Database**: MySQL (Production), H2 (Test)
- **Security**: Spring Security, JWT
- **Monitoring**: Spring Boot Actuator, Micrometer, Prometheus
- **Code Quality**: SonarQube, Jacoco

## 📂 프로젝트 구조 (Project Structure)

이 프로젝트는 도메인 주도 설계(DDD) 구조를 따릅니다:

- `auth`: 인증 및 인가 (OAuth, JWT)
- `account`: 사용자 계좌 관리 및 조회
- `card`: 카드 발급 및 관리
- `goal`: 금융 목표 설정 및 추적
- `investment`: 투자 시뮬레이션 및 관리
- `transfer`: 자금 이체 서비스
- `notification`: 사용자 알림 처리
- `quiz`: 금융 상식 퀴즈
- `report`: 사용 리포트 및 분석
- `user`: 사용자 프로필 및 정보 관리
- `admin`: 관리자 기능

## 📦 코어 뱅킹 시스템 연동 (Core Banking System Integration)

TeenyFinny Channel Backend는 실제 금융 거래 및 원장 관리를 위해 **코어 뱅킹 시스템(Core Banking System)**과 연동합니다.

- **통신 방식**: HTTP REST API (RestTemplate 사용)
- **역할 분담**:
  - **Channel System**: 사용자 인증, UI/UX 데이터 가공, 비금융 서비스 로직 처리
  - **Core System**: 계좌 생성, 입출금, 이체, 투자 주문 등 실제 금융 트랜잭션 처리
- **주요 연동 모듈**:
  - `CoreUserClient`: 사용자 정보 동기화 및 계좌 생성 요청
  - `CoreGoalClient`: 목표 계좌 관리 및 거래 내역 조회
  - `CoreTransferClient`: 자동 이체 및 송금 요청
  - `CoreInvestmentClient`: 투자 계좌 관리 및 주식 주문

## 🚀 시작하기 (Getting Started)

### 사전 요구사항 (Prerequisites)

- Java 17 이상
- Docker (선택 사항, 컨테이너 실행 시)
- MySQL (로컬 데이터베이스 사용 시)

### 설치 (Installation)

1. 저장소 클론:
   ```bash
   git clone <repository-url>
   cd TeenyFinny-channel-be
   ```

2. 프로젝트 빌드:
   ```bash
   ./gradlew clean build
   ```

### 애플리케이션 실행 (Running the Application)

Gradle을 사용하여 애플리케이션을 실행합니다:

```bash
./gradlew bootRun
```

애플리케이션은 `8080` 포트에서 실행됩니다.
기본 API 경로: `/channel`

### 설정 (Configuration)

애플리케이션 설정은 `src/main/resources/application.yml`에 위치합니다.
주요 설정:
- **Server Port**: 8080
- **Context Path**: `/channel`
- **Timezone**: Asia/Seoul

## 🧪 테스트 (Testing)

단위 및 통합 테스트 실행:

```bash
./gradlew test
```

테스트 리포트는 Jacoco를 통해 생성됩니다.

## 🐳 Docker

Docker 이미지 빌드:

```bash
docker build -t teenyfinny-channel .
```

## 📊 모니터링 (Monitoring)

Spring Boot Actuator와 Prometheus를 통해 메트릭을 수집합니다: `/actuator/prometheus`
헬스 체크: `/actuator/health`
