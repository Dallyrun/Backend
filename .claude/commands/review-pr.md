---
description: PR의 변경사항을 분석하고 인라인 코드 리뷰를 작성합니다
argument-hint: "[PR번호] [comment|approve|request-changes]"
---

# /review-pr — PR 코드 리뷰 명령어

PR의 변경사항을 분석하고 인라인 코드 리뷰를 작성합니다.

## 사용법

```
/review-pr [PR번호] [옵션]
```

### 파라미터

| 파라미터 | 설명 | 기본값 |
|----------|------|--------|
| PR번호 | 리뷰할 PR 번호 | 현재 브랜치의 PR |
| 옵션 | `comment`, `approve`, `request-changes` | 자동 판단 |

### 자동 판단 기준

옵션을 지정하지 않으면 리뷰 결과에 따라 자동으로 판단합니다.

| 조건 | 결과 |
|------|------|
| Bug/Security 이슈 있음 | `request-changes` |
| 개선 필요 사항만 있음 | `comment` |
| 문제 없음 | `approve` |

### 예시

```bash
/review-pr                      # 현재 브랜치의 PR (자동 판단)
/review-pr 42                   # 특정 PR (자동 판단)
/review-pr 42 comment           # 강제 코멘트만
/review-pr 42 approve           # 강제 승인
/review-pr 42 request-changes   # 강제 변경 요청
```

## 실행 단계

### 1단계: PR 정보 수집

```bash
gh pr view {PR번호} --json number,title,body,author,baseRefName,headRefName,files,additions,deletions
gh pr view {PR번호} --json files --jq '.files[].path'
gh pr diff {PR번호}
```

### 2단계: 변경사항 분석

파일 타입별 분류:

- `.java` → Java/Spring 리뷰 규칙 적용
- `.md` → 문서 리뷰 (오타, 링크, 코드 블록 유효성)
- `.gradle`, `.gradle.kts` → 빌드 스크립트 검증
- `.yaml`, `.yml` → 설정 파일 검증

#### Java 코드 체크리스트 (`.java`)

**계층 책임**
- [ ] Controller: HTTP 요청/응답과 검증만. 비즈니스 로직 금지
- [ ] Service: 비즈니스 로직. `@Transactional` 적절히 사용 (read-only 포함)
- [ ] Repository: JPA 쿼리만. 비즈니스 로직 금지
- [ ] Entity: 상태 변경 메서드는 명시적으로. setter 남발 금지
- [ ] DTO: request/response 분리, `record` 우선 사용

**견고성**
- [ ] `Optional.get()` 남용 금지 — `orElseThrow(() -> new BusinessException(...))`
- [ ] 체크 예외를 삼키지 않음 (빈 catch 금지)
- [ ] `@Valid` 누락 없음 (컨트롤러 `@RequestBody`에서)
- [ ] N+1 쿼리 위험 (연관 엔티티 조회 시 fetch join / `@EntityGraph`)
- [ ] 트랜잭션 경계가 명확한가 (외부 API 호출이 트랜잭션 내부인지)

**에러 처리**
- [ ] `throw new RuntimeException(...)` 금지 — `BusinessException(ErrorCode.*)` 사용
- [ ] `ErrorCode` 에 대응 항목이 없으면 먼저 추가
- [ ] 로깅은 SLF4J (`LoggerFactory.getLogger`) 사용, `System.out` 금지

**보안**
- [ ] 하드코딩된 시크릿/비밀번호/토큰 없음
- [ ] SQL 인젝션 가능성 (문자열 연결 JPQL 금지, 바인딩 사용)
- [ ] 입력값 검증 (`@NotBlank`, `@Email`, `@Size` 등)
- [ ] 인증/인가 누락 없음 (`@PreAuthorize` 또는 `SecurityConfig`)
- [ ] 민감 정보 응답 노출 없음 (password 해시, 토큰 등)

**테스트**
- [ ] 새/변경된 로직에 테스트가 있는가
- [ ] 정상 케이스 + 예외 케이스 모두 커버
- [ ] 통합 테스트가 필요한 흐름에 단위 테스트만 있지 않은가

### 3단계: 인라인 코멘트 작성

```bash
COMMIT_SHA=$(gh pr view {PR번호} --json headRefOid --jq '.headRefOid')

gh api repos/{owner}/{repo}/pulls/{PR번호}/comments \
  -X POST \
  -F body="코멘트 내용" \
  -F commit_id="$COMMIT_SHA" \
  -F path="파일경로" \
  -F line=줄번호 \
  -F side="RIGHT"
```

#### 코멘트 형식

```
**[카테고리]** 설명
```

예시:
- **[Bug]** `Optional.get()`이 `NoSuchElementException`을 던질 수 있습니다. `orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND))` 사용을 제안합니다.
- **[Layer]** 컨트롤러에 비즈니스 로직이 있습니다. 서비스로 이동해주세요.
- **[Security]** 평문 비밀번호가 로그에 출력됩니다.
- **[Performance]** 루프 내부에서 Repository 호출이 발생합니다. fetch join 또는 `findAllById` 로 변경을 제안합니다.
- **[Style]** 사용하지 않는 import가 있습니다.

#### 카테고리

| 카테고리 | 설명 | 심각도 |
|----------|------|--------|
| `Bug` | 버그/런타임 에러 가능성 | 높음 |
| `Security` | 보안 취약점 | 높음 |
| `Layer` | 계층 책임 위반 | 중간 |
| `Error Handling` | 예외 처리 누락/부적절 | 중간 |
| `Performance` | N+1, 불필요한 쿼리, 비효율 | 중간 |
| `Test` | 테스트 누락/부적절 | 중간 |
| `Style` | 코딩 컨벤션 위반 | 낮음 |
| `Suggestion` | 개선 제안 | 낮음 |
| `Nit` | 사소한 지적 | 매우 낮음 |
| `Question` | 질문/확인 필요 | 정보 |

### 4단계: 전체 리뷰 제출

```bash
# 코멘트
gh pr review {PR번호} --comment --body "$(cat <<'EOF'
## 코드 리뷰 요약

### 잘된 점
- ...

### 개선 제안
- ...

### 주의 필요
- ...
EOF
)"

# 승인
gh pr review {PR번호} --approve --body "LGTM"

# 변경 요청
gh pr review {PR번호} --request-changes --body "위 코멘트 반영 후 다시 요청 부탁드립니다."
```

## 출력 형식

```
========================================
PR #{번호} 코드 리뷰
========================================

변경 파일: N개 (+{additions} -{deletions})

[AuthController.java]
  L42: [Bug] Optional.get() 사용 — NoSuchElementException 가능성
  L78: [Layer] 컨트롤러에 비즈니스 로직

[AuthService.java]
  L15: [Test] 신규 로직에 테스트 없음

----------------------------------------
리뷰 요약
----------------------------------------
버그/보안: 1건
중간 이슈: 2건
스타일/Nit: 0건

총 3개 인라인 코멘트 작성됨
========================================
```

## 주의사항

- 인라인 코멘트는 **변경된 라인**에만 작성 가능
- 삭제된 라인은 `side="LEFT"` 사용
- 이미 작성된 코멘트는 중복 작성하지 않음
- 리뷰 본문에 Claude/AI 관련 언급 금지
