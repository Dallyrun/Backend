# Tidy First 커밋 전략

구조적 변경과 행동적 변경을 **분리된 커밋**으로 나눕니다. Kent Beck 의 "Tidy First?" 원칙.

## 커밋 분류

### 구조적 변경 (Structure)

행동/동작에는 변화 없이 **코드 구조만** 바꾸는 변경.

- 파일/패키지 이동·분리·병합
- 클래스/메서드/변수 이름 변경
- 시그니처 유지 리팩토링 (Extract Method, Inline 등)
- import 정리
- 접근 제어자 변경 (public → package-private 등) — 호출자 동작이 동일한 경우
- 포맷팅/주석

**커밋 메시지 형식**

```
refactor: <변경 내용>

- 세부 변경 1
- 세부 변경 2
```

**예시**

```
refactor: AuthService 를 인터페이스/구현체로 분리

- AuthService 인터페이스 추출
- AuthServiceImpl 구현체 생성
- 기존 호출자는 그대로 AuthService 주입
```

### 행동적 변경 (Behavior)

**관찰 가능한 동작이 바뀌는** 변경.

- 기능 추가
- 버그 수정
- 검증 규칙 변경
- API 응답 형식/필드 변경
- 성능 특성 변경 (DB 쿼리 개수 등 — 엄밀히는 논쟁 여지 있음)

**커밋 메시지 형식**

```
feat: <기능 설명>
fix: <버그 설명>

- 세부 변경 1
- 세부 변경 2
```

**예시**

```
feat: 러닝 세션 공유 링크 TTL 커스터마이징

- ShareConfig.maxTtlSeconds 추가
- createLink 가 ttlSeconds 파라미터 수용
- 최대값 초과 시 INVALID_INPUT 예외
```

## 커밋 순서

1. **먼저 구조적 변경** → `./gradlew build` 통과 확인
2. **그 다음 행동적 변경** → `./gradlew build` 통과 확인
3. **같은 커밋에 혼합하지 않음**

## 좋은 예

```bash
# 커밋 1: 구조
git commit -m "refactor: ShareService 를 인터페이스/구현체로 분리"

# 커밋 2: 행동
git commit -m "feat: 공유 링크 TTL 커스터마이징 지원"
```

## 나쁜 예

```bash
# 구조+행동 혼합 (금지)
git commit -m "ShareService 분리 및 TTL 커스터마이징"
```

리뷰어는 "이름 바꾼 건가? 로직이 바뀐 건가?" 를 구분할 수 없게 됩니다.

## 판단 기준

"이 변경 하나만 적용하면 테스트가 여전히 모두 같은 결과를 내는가?"

- **예** → 구조적 변경
- **아니오** → 행동적 변경

## 왜?

- 리뷰어가 의도를 명확히 파악 — 구조 커밋은 빠르게 훑고, 행동 커밋에 집중
- 문제 발생 시 롤백 범위 최소화 — 로직 문제가 있을 때 구조 커밋까지 되돌리지 않아도 됨
- 코드 히스토리(`git log`, `git blame`)의 신호 품질 향상
