---
description: Gradle 테스트를 실행하고 실패 원인을 분석합니다
argument-hint: "[클래스명|메서드패턴] (선택)"
---

# /test — 테스트 실행 명령어

테스트를 실행하고 결과를 분석합니다.

## 기본 명령

```bash
./gradlew test                 # 전체 테스트
./gradlew test --info          # 상세 로그 포함
./gradlew clean test           # 깨끗한 상태에서 실행
```

## 단일 테스트 실행

```bash
# 클래스 단위
./gradlew test --tests "com.inseong.dallyrun.backend.service.AuthServiceImplTest"

# 메서드 단위
./gradlew test --tests "com.inseong.dallyrun.backend.service.AuthServiceImplTest.signup_중복이메일이면_예외"

# 패턴
./gradlew test --tests "*AuthService*"
```

## 옵션

- `/test` — 모든 테스트
- `/test [클래스명 또는 메서드 패턴]` — 특정 테스트만
- `/test --rerun` — Gradle 캐시 무시하고 재실행

## 실패 시 행동

1. 실패한 테스트 식별 — Gradle HTML 리포트 경로: `build/reports/tests/test/index.html`
2. 실패 메시지와 스택트레이스 확인
3. 관련 프로덕션 코드 분석
4. 원인 파악 후 수정안 제시
5. **사용자 승인 후 수정** (테스트를 먼저 우회하거나 `@Disabled`로 끄지 않기)

## 출력 형식

```
테스트 결과: X passed, Y failed, Z skipped

실패한 테스트:
- com.inseong.dallyrun.backend.service.AuthServiceImplTest.signup_중복이메일이면_예외
  원인: EMAIL_ALREADY_EXISTS 대신 MEMBER_NOT_FOUND가 던져짐
  관련 파일: src/main/java/.../service/AuthServiceImpl.java:42
```

## 주의

- 테스트 실패 → 프로덕션 버그일 가능성을 먼저 의심
- 테스트를 통과시키기 위해 프로덕션 코드의 검증을 제거하지 않기
- 통합 테스트가 DB를 요구하면 Testcontainers/H2 설정 확인
