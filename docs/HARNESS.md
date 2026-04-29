# Shared Engineering Harness

이 문서는 Dallyrun Backend 에서 Claude Code 와 Codex 가 공통으로 따르는 검증 게이트와 피드백 루프를 정의한다. 도구별 진입 문서(`CLAUDE.md`, `AGENTS.md`)보다 이 문서를 우선해서 확인한다.

## 목적

- 작업 시작 전에 같은 맥락을 읽고 같은 품질 기준으로 판단한다.
- 구현 중 빠른 피드백을 만들고, PR 전에는 CI 와 같은 기준으로 검증한다.
- 에러, 트레이드오프, 성능 개선 사례를 한 곳에 기록해 다음 작업의 입력으로 재사용한다.

## Gate 1. Context

작업 전 다음 파일을 확인한다.

- `README.md`: 실행 방법과 프로젝트 개요
- `docs/API.md`: 공개 API 계약
- `docs/HARNESS.md`: 공통 검증 게이트
- `CLAUDE.md` 또는 `AGENTS.md`: 현재 도구의 세부 규칙
- 관련 코드: 변경이 예상되는 controller/service/repository/entity/config/security 계층

보안, 인증, 파일 저장, 공유 링크, DB 스키마 변경은 영향 범위를 더 넓게 본다.

## Gate 2. Plan

구현 전에 다음을 짧게 정리한다.

- 목표: 사용자가 원하는 결과
- 변경 범위: 건드릴 파일과 계층
- 위험 지점: 인증/인가, Redis TTL, 파일 경로, JPA 관계, 예외 처리, API 호환성
- 검증 전략: 실행할 테스트와 문서 업데이트 대상

작업이 단순 문서 수정이면 계획은 짧게 유지해도 된다. 동작 변경이면 테스트 전략을 반드시 포함한다.

## Gate 3. Implementation

기존 MVC 구조와 로컬 패턴을 따른다.

- Controller: HTTP 요청/응답, 입력 검증, 인증 주체 전달
- Service: 비즈니스 로직과 트랜잭션 경계
- Repository: JPA 쿼리와 데이터 접근
- Entity: 도메인 상태와 관계 매핑
- DTO: 요청/응답 분리, 가능하면 `record` 사용
- Exception: `ErrorCode`, `BusinessException`, `GlobalExceptionHandler` 흐름 유지

기능 변경 시 테스트를 함께 작성한다. 테스트를 통과시키기 위해 검증 로직을 약화하거나 실패 테스트를 비활성화하지 않는다.

## Gate 4. Verification

변경 범위에 맞춰 가장 빠른 검증부터 실행한다.

- 특정 로직 변경: `./gradlew test --tests "패턴"`
- 일반 기능 변경: `./gradlew test`
- PR 전 최종 검증: `./gradlew build`
- 문서만 변경: 테스트를 생략할 수 있으나 PR 본문에 `문서 변경만 포함되어 미실행`을 명시한다.

실패하면 실패 테스트, 에러 메시지, 관련 파일을 먼저 식별한다. 테스트 우회보다 구현 또는 테스트 기대값 중 무엇이 잘못됐는지 판단한다.

## Gate 5. Documentation

다음 변경은 문서를 업데이트한다.

- API 요청/응답, 상태 코드, 인증 요구사항 변경: `docs/API.md`
- 실행 방법, 환경 변수, 인프라 구성 변경: `README.md`
- 도구 작업 규칙, 검증 게이트, 피드백 루프 변경: `docs/HARNESS.md`, `CLAUDE.md`, `AGENTS.md`
- 에러, 트레이드오프, 성능 개선 사례: `docs/TROUBLESHOOTING.md`

민감한 값은 문서, 커밋 메시지, PR 본문에 남기지 않는다.

## Gate 6. PR

PR 전 체크리스트:

- feature 브랜치에서 작업했다.
- 커밋 메시지는 Conventional Commits 형식이다.
- 코드 변경에는 테스트가 포함되어 있다.
- 필요한 문서가 현재 코드 상태와 맞다.
- `./gradlew build` 결과 또는 미실행 사유가 PR 본문에 있다.
- git 기록에 Claude, Codex, AI, `Co-Authored-By` 언급을 남기지 않았다.

PR 본문에는 개요, 변경 사항, 테스트, 관련 이슈, 검토 요청을 포함한다.

## Feedback Loop

작업 중 다음 사례가 있으면 `docs/TROUBLESHOOTING.md`에 기록한다.

- **TROUBLE**: 에러 또는 장애를 디버깅하고 해결한 사례
- **TRADEOFF**: 여러 선택지를 비교하고 의식적으로 결정한 설계 사례
- **PERF**: 성능 개선 또는 비효율 회피 사례

기록은 보수적으로 남긴다. 단순 코드 작성, 문서 정리, CI 통과 같은 정상 흐름은 기록하지 않는다. 이미 같은 사례가 있으면 중복 기록하지 않는다.

## Backend-Specific Checks

- 인증/인가 변경: `SecurityConfig`, JWT 필터, 인증 제외 경로 확인
- 공유 링크 변경: Redis key, TTL, 만료 동작, 인증 불필요 조회 경로 확인
- 파일 저장 변경: `FileStorage`, `LocalFileStorage`, 저장 루트, base URL, `/uploads/**` 정적 서빙 확인
- 엔티티 변경: JPA 관계, enum/check constraint, 운영 프로파일의 `ddl-auto: validate` 영향 확인
- 예외 변경: `ErrorCode`와 HTTP 응답 매핑 일관성 확인
- API 변경: `docs/API.md`와 테스트가 새 계약을 반영하는지 확인
