# Dallyrun Backend

런닝 트래킹 앱 **Dallyrun**의 백엔드 서버입니다.
GPS 기반 러닝 기록, 목표 관리, 뱃지 시스템, 소셜 공유 기능을 제공합니다.

## 기술 스택

- **Java 17** / **Spring Boot 4.0.5** / **Gradle 9.4.1**
- **PostgreSQL** — 메인 데이터베이스
- **Redis** — Refresh Token 저장, 공유 링크 캐싱
- **Spring Security** — JWT 기반 Stateless 인증
- **SpringDoc OpenAPI** — Swagger UI API 문서

## 사전 요구사항

- Java 17+
- PostgreSQL
- Redis

## 환경 설정

로컬 개발은 루트의 `.env` 파일로 관리합니다.

```bash
cp .env.example .env
# .env 파일을 열어 각 값을 채워넣으세요.
```

`.env`는 gitignore 되어 커밋되지 않습니다. 운영 환경 값은 GitHub Actions secrets로 주입됩니다.

## 빌드 및 실행

```bash
# 빌드
./gradlew build

# 실행 (.env 설정 필요)
./gradlew bootRun

# 전체 테스트 (외부 환경 불필요, H2 인메모리 사용)
./gradlew test

# 단일 테스트
./gradlew test --tests "com.inseong.dallyrun.backend.SomeTestClass"
```

## API 문서

- **Swagger UI**: `http://localhost:8080/swagger-ui/index.html` (서버 실행 후)
- **API 문서 (Markdown)**: [docs/API.md](docs/API.md)

## 주요 기능

- **Auth** — 이메일/비밀번호 회원가입·로그인, JWT 토큰 발급/갱신
- **Member** — 회원 프로필 조회/수정
- **Running Session** — GPS 기반 러닝 기록 생성/조회/삭제, 통계
- **Goal** — 러닝 목표 설정/관리, 달성률 추적
- **Badge** — 활동 기반 뱃지 시스템
- **Share** — 러닝 기록 공유 링크 생성/조회

## 프로젝트 구조

```
src/main/java/com/inseong/dallyrun/backend/
├── config/        # Spring 설정 (Redis, Security 등)
├── controller/    # REST API 엔드포인트
├── dto/           # 요청/응답 데이터 전송 객체
├── entity/        # JPA 엔티티
├── exception/     # 예외 처리 (ErrorCode, BusinessException, GlobalExceptionHandler)
├── repository/    # JPA 데이터 접근 계층
├── security/      # JWT 인증/인가 (JwtTokenProvider, JwtAuthenticationFilter 등)
├── service/       # 비즈니스 로직
└── util/          # 유틸리티 (GeoUtils, DateUtils)
```
