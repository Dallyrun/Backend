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
- PostgreSQL (기본: `localhost:5432/dallyrun`)
- Redis (기본: `localhost:6379`)

## 빌드 및 실행

```bash
# 빌드
./gradlew build

# 실행
./gradlew bootRun

# 전체 테스트
./gradlew test

# 단일 테스트
./gradlew test --tests "com.inseong.dallyrun.backend.SomeTestClass"
```

## API 문서

- **Swagger UI**: `http://localhost:8080/swagger-ui/index.html` (서버 실행 후)
- **API 문서 (Markdown)**: [docs/API.md](docs/API.md)

## 프로젝트 구조

```
src/main/java/com/inseong/dallyrun/backend/
├── config/        # Spring 설정 (Redis, Security 등)
├── controller/    # REST API 엔드포인트
├── dto/           # 요청/응답 데이터 전송 객체
├── entity/        # JPA 엔티티
├── exception/     # 예외 처리
├── repository/    # JPA 데이터 접근 계층
├── security/      # JWT, 인증/인가
├── service/       # 비즈니스 로직
└── util/          # 유틸리티 클래스
```
