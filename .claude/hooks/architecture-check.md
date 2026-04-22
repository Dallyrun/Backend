---
description: Reminder to align with Spring MVC layered architecture
trigger: explicit
---

# Architecture Alignment Hook

## 목적

새 파일을 만들거나 리팩토링할 때 Dallyrun Backend 의 MVC 계층 구조를 따르는지 확인합니다.

기본 패키지: `com.inseong.dallyrun.backend`

## 계층별 책임

| 패키지 | 책임 | 금지 사항 |
|--------|------|-----------|
| `controller/` | HTTP 요청/응답 파싱, 입력 검증, 응답 래핑 | 비즈니스 로직, 직접 Repository 호출 |
| `service/` | 비즈니스 로직, 트랜잭션 경계, 도메인 규칙 | `HttpServletRequest`, `ResponseEntity` 참조 |
| `repository/` | JPA 쿼리, 영속성 접근 | 비즈니스 로직, 다른 Repository 의존 |
| `entity/` | 영속 상태와 불변 규칙, 상태 변경 메서드 | 외부 의존 주입, 영속성/전송 혼재 |
| `dto/` | 데이터 전송 객체 (request/response 분리) | 비즈니스 로직, 엔티티 직접 상속 |
| `config/` | Spring 설정 (Security, CORS, Redis 등) | 비즈니스 로직 |
| `security/` | JWT 인증/인가, UserDetails | 비즈니스 로직 |
| `exception/` | ErrorCode, BusinessException, GlobalExceptionHandler | — |
| `util/` | 순수 유틸리티 함수 (`GeoUtils`, `DateUtils`) | 상태 보유, Spring 빈 의존 |

## 체크리스트

새 파일 생성/리팩토링 전:

### 1. 패키지 위치
- [ ] 이 클래스는 어느 계층에 속하는가?
- [ ] 파일명이 계층 네이밍 규칙을 따르는가? (`*Controller`, `*Service`/`*ServiceImpl`, `*Repository`, `*Request`/`*Response`)

### 2. 계층 책임
- [ ] Controller 에 비즈니스 로직이 들어가지 않는가?
- [ ] Service 가 HTTP 타입 (`HttpServletRequest`, `ResponseEntity`) 을 참조하지 않는가?
- [ ] Repository 가 다른 Repository 를 의존하지 않는가?
- [ ] Entity 에 Spring 빈 주입이나 외부 서비스 호출이 없는가?

### 3. 의존성 방향
- [ ] Controller → Service → Repository 방향으로 흐르는가?
- [ ] 역방향 의존 (Service → Controller 등) 이 없는가?
- [ ] 순환 의존이 없는가?

### 4. DTO
- [ ] Entity 가 Controller 레이어를 직접 넘나들지 않는가?
- [ ] Request DTO 는 `dto/request/`, Response DTO 는 `dto/response/` 에 있는가?
- [ ] DTO 는 `record` 로 작성되었는가?

### 5. 트랜잭션
- [ ] 쓰기 작업에 `@Transactional` 이 붙어 있는가?
- [ ] 읽기 전용은 `@Transactional(readOnly = true)` 인가?
- [ ] 트랜잭션 내부에서 외부 API 호출/긴 작업을 하지 않는가?

### 6. 인증/보안
- [ ] 인증이 필요한 엔드포인트가 `SecurityConfig` 에서 공개되어 있지 않은가?
- [ ] 사용자 ID 는 `@AuthenticationPrincipal CustomUserDetails` 로 받는가? (Path/Query 로 받아 인가 우회 가능성 없음)

## 참조 문서

- `CLAUDE.md` — 프로젝트 개요와 워크플로우
- `.claude/rules/api-design.md` — Controller/DTO 상세 규칙
- `.claude/rules/tidy-first-commit.md` — 구조/행동 커밋 분리
