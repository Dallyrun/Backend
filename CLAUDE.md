# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Dallyrun Backend — 런닝 트래킹 앱을 위한 백엔드 서버. Spring Boot 4.0.5, Java 17, Gradle 9.4.1 기반. PostgreSQL, Redis, Spring Security 사용.

## Build & Run Commands

```bash
./gradlew build          # 빌드
./gradlew bootRun        # 실행
./gradlew test           # 전체 테스트
./gradlew clean build    # 클린 빌드

# 단일 테스트
./gradlew test --tests "com.inseong.dallyrun.backend.SomeTestClass"
./gradlew test --tests "com.inseong.dallyrun.backend.SomeTestClass.methodName"
```

## Architecture (MVC)

- **Base package**: `com.inseong.dallyrun.backend`
- `controller/` — REST API 엔드포인트
- `service/` — 비즈니스 로직
- `repository/` — JPA 데이터 접근 계층
- `entity/` — JPA 엔티티 클래스
- `dto/` — 요청/응답 데이터 전송 객체
- `config/` — Spring 설정 (Redis, Security, CORS, ShareConfig 등)
- `security/` — JWT 인증/인가 (JwtTokenProvider, JwtAuthenticationFilter, CustomUserDetails, CustomUserDetailsService)
- `exception/` — 예외 처리 (ErrorCode, BusinessException, GlobalExceptionHandler)
- `util/` — 유틸리티 (GeoUtils, DateUtils)

## Infrastructure

- **DB**: PostgreSQL (JPA/Hibernate, HikariCP 커넥션 풀)
- **Cache/Session**: Redis (Refresh token, 공유 링크 TTL)
- **Security**: Spring Security 기반 JWT Stateless 인증. `/api/auth/**`, `/uploads/**`, 공유 링크 조회는 인증 불필요
- **Auth**: 이메일/비밀번호 회원가입·로그인 (BCrypt 해시)
- **File Storage**: `storage/FileStorage` 인터페이스 + `LocalFileStorage` 구현 (개발용 로컬 디스크). 저장 루트/베이스 URL은 `storage.local.path` / `storage.base-url` 로 설정. `/uploads/**` 정적 서빙. 추후 S3 등으로 교체 시 인터페이스 구현만 교체.
- **API 문서**: SpringDoc OpenAPI 3 (Swagger UI)
- **Prod 프로파일**: `application-prod.yaml` — `ddl-auto: validate`, `sql.init.mode: never`

## Configuration

- 로컬 환경 설정은 루트의 `.env` 파일로 관리한다. `.env.example`을 복사하여 값을 채운다.
- 운영 환경은 GitHub Actions secrets로 주입한다.
- 민감한 값(비밀번호, 시크릿, OAuth 크리덴셜)은 문서·커밋·이슈·PR 본문 등 git 기록에 절대 포함하지 않는다.

## Workflow

- 작업 시작 시 반드시 **플랜 모드**로 계획을 세운 뒤 승인받고 구현한다.
- 작업은 **feature 브랜치**를 생성하여 진행한다. (예: `feat/경로-모아보기`, `fix/gps-validation`)
- 작업 완료 후 **문서(CLAUDE.md, README.md, docs/API.md 등)를 현재 코드 상태에 맞게 업데이트**한다.
- 작업 완료 후 **커밋 → 푸시 → PR 생성**까지 수행한다. (main 브랜치에 직접 커밋하지 않는다.)

## Git Convention

- **Conventional Commits** 형식 사용: `feat:`, `fix:`, `refactor:`, `docs:`, `test:`, `chore:`, `ci:` 등
- 커밋 메시지, PR 본문 등 git 기록에 Claude/AI 관련 언급(Co-Authored-By 포함)을 절대 포함하지 않는다.

## Code Quality

- **Testable한 코드** 작성: 의존성 주입(DI) 활용, 인터페이스 기반 설계로 테스트 용이성 확보
- 기능 구현 시 **테스트 코드를 반드시 함께 작성**한다.
- 안정적이고 견고한 코드를 우선한다.
