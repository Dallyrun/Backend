---
name: architecture-alignment
description: Dallyrun Backend 의 표준 Spring Boot 계층형 아키텍처(Controller/Service/Repository + Entity/DTO/Exception) 일관성 점검. 새 파일 생성·리팩토링·도메인 추가 전에 호출하여 계층 분리, 의존성 방향, 횡단 관심사가 어긋나지 않는지 확인한다.
trigger: explicit
---

# Architecture Alignment

## 목적

모든 코드 변경이 Dallyrun Backend 의 정해진 계층형(Layered MVC) 구조에 맞도록 유지한다. 도메인 디렉토리(`src/domain/{name}/`) 형태가 아니라, **기술 계층 단위**로 디렉토리를 나누는 Spring Boot 표준 구조이며, 도메인 로직은 Service + Entity 가 함께 담당한다 (DDD-lite).

## 체크리스트

새 파일을 만들거나 기존 파일을 옮기기 **전에** 다음을 확인한다.

### 1. 계층별 책임

| 디렉토리 / 파일 패턴 | 책임 | 금지 |
|---|---|---|
| `controller/*Controller.java` | REST 엔드포인트, 요청 매핑, 응답 형태, OpenAPI 어노테이션 | 비즈니스 로직, Repository 직접 호출 |
| `service/*Service.java` (interface) + `*ServiceImpl.java` | 비즈니스 로직, `@Transactional` 경계, 도메인 간 협력 | HTTP·요청 매핑, JPA 어노테이션 |
| `repository/*Repository.java` | `JpaRepository` 확장. JPQL · 메서드 쿼리 | 비즈니스 분기, 트랜잭션 시작 |
| `entity/*.java` | JPA 엔티티 + 도메인 메서드 (`softDelete()`, `complete()`, `updateProfile()` 등) | Service 호출, DTO 의존 |
| `dto/request/*.java`, `dto/response/*.java` | 순수 데이터 전송. `record` + Bean Validation | 비즈니스 로직, 영속성 매핑 |
| `exception/ErrorCode.java`, `BusinessException.java`, `GlobalExceptionHandler.java` | 도메인 에러 분류 + 일관된 응답 envelope | 다른 곳에서 직접 throw 새 Exception 만들기 |
| `config/*.java` | `@Configuration` 빈, `application.yaml` 바인딩 | 비즈니스 로직 |
| `security/*.java` | JWT, `UserDetails`, 인증 필터 | 도메인 로직 |
| `storage/FileStorage.java` 인터페이스 + 구현체 | 외부 자원 추상화 (로컬→S3 교체 가능) | 인터페이스 우회한 직접 호출 |
| `util/*.java` | 정적 메서드 모음 (예: `GeoUtils.calculateDistance`) | DI 주입, 상태 보유 |

### 2. 의존성 방향

```
Controller → Service → Repository
                 ↓
              Entity
```

- ❌ Controller 가 Repository 직접 호출
- ❌ Entity 가 Service 호출 (도메인 메서드는 self-contained)
- ✅ Service 끼리 의존 가능 (예: `MemberServiceImpl` 이 `AuthService.logout()` 사용)
- ✅ Service 가 외부 추상화(`FileStorage`) 의존

### 3. 횡단 관심사 (cross-cutting)

| 관심사 | 표준 |
|---|---|
| 인증·인가 | `SecurityConfig` + `JwtAuthenticationFilter` 만 변경. 컨트롤러는 `@AuthenticationPrincipal CustomUserDetails` 주입 |
| 에러 응답 | `throw new BusinessException(ErrorCode.X)` 만 사용. 새 에러는 `ErrorCode` enum 에 먼저 추가 |
| 응답 envelope | 성공: `ApiResponse.of(data)` · 실패: `GlobalExceptionHandler` 가 `{ "message": ... }` 자동 변환 |
| Soft delete | 엔티티에 `@SQLRestriction("deleted_at IS NULL")` + `softDelete()` 도메인 메서드 |
| Validation | DTO 에 `@NotBlank`, `@Pattern`, `@Size` 등. 컨트롤러는 `@Valid` |
| 트랜잭션 | `*ServiceImpl` 클래스 레벨 `@Transactional`, 읽기 전용은 메서드 레벨 `@Transactional(readOnly = true)` |

### 4. 새 기능(도메인) 추가 절차

예: "팔로우" 기능 추가 시

1. `entity/Follow.java` (도메인 메서드 포함)
2. `repository/FollowRepository.java`
3. `service/FollowService.java` (interface) + `service/FollowServiceImpl.java`
4. `controller/FollowController.java`
5. `dto/request/FollowRequest.java`, `dto/response/FollowResponse.java`
6. 필요 시 `exception/ErrorCode.java` 에 `FOLLOW_*` 코드 추가
7. 필요 시 `data.sql` 시드 / 마이그레이션 추가
8. 테스트: `*ServiceTest` (단위) + `*ControllerTest` (`@WebMvcTest` 슬라이스)
9. `docs/API.md` 갱신
10. CLAUDE.md / 다른 문서에 영향 있으면 동기 갱신

### 5. 도메인 디렉토리 분리(DDD per-domain) 미채택 이유

현재 도메인 6개 (auth · member · running · goal · badge · share) 로 코드량 적정. 기술 계층 분리가 IDE 탐색·신규 합류자 onboarding 비용 낮음. 도메인이 20+ 로 성장하거나 팀이 도메인별 분담을 시작하면 `domain/{name}/{layer}` 구조로 마이그레이션 검토.

### 6. 참고 문서

- `CLAUDE.md` — 프로젝트 전반 룰 (워크플로우, 커밋 컨벤션 등)
- `docs/API.md` — REST API 명세 (변경 시 반드시 함께 갱신)
- `docs/TROUBLESHOOTING.md` — 과거 트레이드오프·트러블슈팅 (반복 문제 회피)

## 호출 시점

이 스킬은 `trigger: explicit` 으로, 다음 상황에 명시적으로 호출한다:

- 새 컨트롤러 / 서비스 / 엔티티 생성 직전
- 기존 파일을 다른 디렉토리로 옮길 때
- "도메인이 늘어나는데 디렉토리를 어떻게 나눌까?" 같은 구조 결정이 필요할 때
- 코드 리뷰에서 계층 책임이 모호해 보일 때
