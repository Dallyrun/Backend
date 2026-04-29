# 트러블슈팅 · 트레이드오프 · 성능 개선 기록

개발 중 마주친 에러, 의식적인 설계 결정(트레이드오프), 성능 개선 사례를 시간순으로 기록한다.
이력서·포트폴리오 작성 시 근거 자료로 활용한다.

## 형식

```
## YYYY-MM-DD · [TROUBLE | TRADEOFF | PERF] 제목
- 상황: 어떤 맥락에서 무슨 일이 일어났나
- 원인: 왜 그런가 (가능하면 메커니즘까지)
- 해결: 무엇을 어떻게 했나
- 학습/메모: 다음에 비슷한 상황에서 떠올려야 할 것
```

분류 약어:
- **TROUBLE** — 에러·장애 트러블슈팅
- **TRADEOFF** — 의식적인 설계 결정과 그 근거
- **PERF** — 성능 개선 또는 회피한 비효율

---

## 2026-04-29 · TROUBLE 로그아웃 permitAll 로 인한 null principal 위험

- 상황: `DELETE /api/auth/logout` 컨트롤러는 `@AuthenticationPrincipal` 의 `memberId` 로 Redis refresh token 을 삭제하지만, `SecurityConfig` 에서 `/api/auth/**` 전체를 `permitAll` 로 열어두고 있었다.
- 원인: Spring Security matcher 가 먼저 매칭된 규칙을 적용하므로 로그아웃도 인증 없이 컨트롤러까지 도달할 수 있었다. 토큰이 없으면 `userDetails` 가 null 이 되어 의도한 401 대신 null principal 오류가 날 수 있었다.
- 해결: `/api/auth/logout` matcher 를 `/api/auth/**` 보다 앞에 두고 `authenticated()` 로 지정. 무인증 요청은 HTTP method 와 무관하게 401, 유효한 Access Token 의 `DELETE` 요청은 200 을 반환하는 컨트롤러 테스트를 추가했다.
- 학습/메모: 같은 prefix 의 공개 API 와 인증 API 가 섞이면 넓은 `permitAll` 보다 예외 엔드포인트를 먼저 선언해야 한다.

## 2026-04-24 · TROUBLE Hibernate ALTER TABLE NOT NULL 실패

- 상황: `Member` 엔티티에 `ageBracket`, `gender` (NOT NULL) 컬럼 추가 후 앱 기동 시 `column "age_bracket" of relation "member" contains null values` 에러로 ALTER 실패. 앱은 떴지만 회원 관련 쿼리 런타임 오류
- 원인: `ddl-auto: update` 는 기존 행이 있는 테이블에 NOT NULL 컬럼을 추가할 수 없음 (디폴트 값을 모르기 때문). Hibernate 가 정책상 자동 backfill 하지 않음
- 해결: 개발 환경이라 `docker compose down -v` 로 볼륨 째 삭제 → 새 스키마로 재생성. (운영이라면 마이그레이션 스크립트로 default 채우기 → NOT NULL 변경 2단계)
- 학습: `ddl-auto: update` 는 enum 추가, NOT NULL 추가, 체크 제약 변경 등 **검사 변경**에 약하다. 운영 시점부터는 Flyway/Liquibase 도입 검토

## 2026-04-24 · TROUBLE Frontend "Failed to fetch" / CORS 403 OPTIONS

- 상황: 회원가입 호출 시 프론트에서 "Failed to fetch", 백엔드 access log 에 `OPTIONS /api/auth/login 403`
- 원인: `.env` 의 `CORS_ALLOWED_ORIGINS=http://localhost:3000` 만 등록돼있고 Vite 가 포트 충돌로 5174 로 fallback. 브라우저가 preflight(`OPTIONS`) 차단
- 해결 (1차): `.env` 에 `http://localhost:5174` 추가
- 해결 (2차, 근본): `WebConfig.addCorsMappings` 를 `allowedOrigins` → `allowedOriginPatterns` 로 변경. `.env` 를 `http://localhost:*` 와일드카드로 일원화 → 포트 변동에 무관
- 학습: Spring Security 의 `allowedOrigins` 는 exact match 만, 와일드카드 + `allowCredentials(true)` 를 같이 쓰려면 `allowedOriginPatterns` 가 필요. 개발 편의 vs 운영 보안은 환경변수로 분리 관리

## 2026-04-24 · TROUBLE Android EPERM (Operation not permitted)

- 상황: 안드로이드 앱에서 로그인 호출 시 `java.net.SocketException: socket failed: EPERM`
- 원인: `AndroidManifest.xml` 에 `<uses-permission android:name="android.permission.INTERNET"/>` 미선언. OS 가 소켓 생성 자체를 차단
- 해결: 매니페스트에 `INTERNET` 권한 추가 + (HTTP 평문이므로) `usesCleartextTraffic="true"` 또는 `network_security_config.xml`
- 학습: 안드로이드 네트워크 에러는 EPERM (권한) / ECONNREFUSED (백엔드 다운) / ETIMEDOUT (네트워크 도달 불가) 분리해서 봐야 함

## 2026-04-24 · TROUBLE Android Socket Timeout (192.0.0.4 → 192.168.x)

- 상황: INTERNET 권한 추가 후에도 `SocketTimeoutException: failed to connect to /192.168.55.118 from /192.0.0.4`
- 원인: 폰이 같은 WiFi 가 아니라 모바일 데이터에 붙어있어서 사설 IP 도달 불가. `192.0.0.4` 는 안드로이드의 CLAT/NAT64 클라이언트 주소 (IPv6-only 네트워크 흔적)
- 해결: 폰을 맥과 동일 WiFi 로 재연결. 폰 IP 가 `192.168.55.x` 인지 설정에서 확인
- 학습: 로컬 IP 로 통신할 때는 양 기기가 같은 L2/L3 네트워크에 있는지부터 확인. 클라이언트 측 IP 가 사설 대역 밖이면 거의 다 네트워크 문제

## 2026-04-24 · TROUBLE Tomcat access log /dev/stdout.YYYY-MM-DD

- 상황: `server.tomcat.accesslog` 를 stdout 으로 보내려고 `directory: /dev`, `prefix: stdout`, `suffix: ""` 설정했더니 앱 기동 실패. `Failed to open access log file [/dev/stdout.2026-04-24]`
- 원인: Tomcat AccessLogValve 의 기본 `file-date-format=.yyyy-MM-dd` 와 `rotate=true` 가 자동으로 날짜 suffix 를 파일명에 붙임 → `/dev/stdout.2026-04-24` 라는 존재하지 않는 경로 생성 시도
- 해결: `file-date-format: ""` + `rotate: false` 추가
- 학습: `/dev/stdout` 트릭은 6개 속성 (`enabled`, `directory`, `prefix`, `suffix`, `file-date-format`, `rotate`) 모두 잘 설정해야 동작. 버그 후에는 PR 별도로 fix 분리해서 추적성 유지

## 2026-04-27 · TROUBLE badge_condition_type_check 위반

- 상황: `ConditionType` enum 에 `EARLY_MORNING_COUNT`, `LATE_NIGHT_COUNT` 추가 후 `data.sql` INSERT 가 `badge_condition_type_check` 위반으로 실패. 앱 기동 자체가 막힘
- 원인: PostgreSQL 의 `CHECK (condition_type IN ('TOTAL_DISTANCE', ...))` 제약이 첫 테이블 생성 시점의 enum 값으로 굳어짐. `ddl-auto: update` 가 enum 변경을 따라 체크 제약을 갱신하지 않음 (Hibernate 한계)
- 해결: 개발 환경에서 `docker compose down -v` 로 DB 재생성 → Hibernate 가 6개 값 포함한 새 체크 제약 생성. (데이터 보존 시: `ALTER TABLE badge DROP CONSTRAINT badge_condition_type_check; ALTER TABLE ... ADD CONSTRAINT ... CHECK (...)` 수동 처리)
- 학습: `@Enumerated(EnumType.STRING)` + DB 체크 제약 조합은 enum 변경 시 자동 동기화 안 됨. 운영 환경엔 마이그레이션 스크립트로 명시적 처리 필요

## 2026-04-27 · TROUBLE PR BEHIND status — branch protection 막힘

- 상황: PR 두 개를 순차 머지하려는데 첫 번째 머지 후 두 번째가 `mergeStateStatus: BEHIND` 로 거부됨. "2 of 2 required status checks are expected"
- 원인: 보호룰이 status check 이 **최신 base(main)에 대해 통과한 것** 만 인정. 첫 PR 머지 후 main 이 앞서가서 두 번째 PR 의 CI 결과가 stale
- 해결: `gh pr update-branch <PR번호> --rebase` → CI 재실행 대기 → 머지
- 학습: 같은 시점에 PR 여러 개 열려 있으면 상호 의존성 없이도 순서 영향 받음. "Update branch" 한 번이면 충분

## 2026-04-26 · TRADEOFF Soft Delete 자식 cascade 처리 안 함

- 상황: 회원 soft delete 시 RunningSession, Goal, MemberBadge, GpsPoint 같은 자식 엔티티를 어떻게 처리할지 결정 필요
- 검토한 옵션:
  1. 자식도 함께 soft delete (cascade)
  2. 자식 hard delete
  3. 자식 그대로 두고 Member 만 차단
- 채택: **3번**. 이유: `Member` 에 `@SQLRestriction("deleted_at IS NULL")` 만 걸면 모든 조회가 자동 필터되고, 자식은 Member 를 거쳐서만 접근 가능하므로 자연스럽게 차단. cascade 코드를 추가하면 복잡도만 올라가고 보안 효과는 동일
- 학습: cross-cutting 정책은 한 곳(엔티티) 에 정의하면 N개 자식에 대해 일관 적용된다. **방어선을 줄여서 보안 효과는 유지하는 트레이드오프**

## 2026-04-27 · TRADEOFF 공유 링크 즉시 정리 안 함

- 상황: 회원 탈퇴 시 그 회원 세션의 공유 코드(Redis) 를 즉시 정리할지
- 검토한 옵션:
  1. Redis 역인덱스(`member:{id}:shares`) 유지 + 탈퇴 시 SCAN/DEL
  2. TTL(30일) 자연 소멸 + 읽기 시점 차단
- 채택: **2번**. 이유: 역인덱스는 항상 동기화 비용 발생, SCAN 은 운영 시 비싼 연산. 읽기 시점에 `MemberRepository.findById` 결과로 차단하면 데이터 노출은 0이고 키만 만료까지 남음 (보안상 무해)
- 학습: 보안 요구사항이 "노출 차단" 이면 자료 정리는 부수적. 요구사항을 한 단계 떨어뜨려서 정확히 보면 인프라 복잡도가 줄어든다
