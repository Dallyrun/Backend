---
name: create-pr
description: Dallyrun Backend 의 Conventional Commits 규칙과 PR 템플릿에 맞춰 gh CLI 로 Pull Request 를 생성합니다. PR 생성, `gh pr create`, 이슈 해결 후 PR 단계에서 사용합니다.
---

# GitHub PR 생성 Skill

Dallyrun Backend 의 컨벤션에 맞는 형식으로 PR을 생성합니다.

## 언제 사용하나요?

- PR을 생성할 때
- `gh pr create` 명령을 사용할 때
- `/fix-issue` 등 이슈 해결 플로우 마지막 단계

## PR 제목 규칙

### 형식

```
<type>: <설명>
```

**허용 type**: `feat`, `fix`, `refactor`, `docs`, `test`, `chore`, `ci`

**예시:**
- `feat: 러닝 세션 공유 링크 만료 시간 커스터마이징`
- `fix: GPS 좌표 유효성 검증 누락 수정`
- `refactor: AuthService 인터페이스 분리`
- `docs: API 문서에 refresh 엔드포인트 추가`

### 금지

- 제목에 이모지/깃모지 사용 금지 — 프로젝트 컨벤션은 이모지 없는 Conventional Commits
- 제목에 Codex/AI 언급 금지
- 제목에 이슈번호 prefix 금지 (이슈 연결은 본문 `Closes #`)

## PR 본문 형식

```markdown
## 개요

<!-- 이 PR이 무엇을 바꾸는지 1~3줄 요약 -->

## 변경 사항

### Added
- `src/main/java/.../FooController.java`: ...

### Modified
- `src/main/java/.../BarService.java`: ...

### Deleted
- `src/main/java/.../LegacyFoo.java`: ...

## 테스트

- [ ] `./gradlew build` 통과
- [ ] 단위 테스트 추가/수정
- [ ] 수동 확인 (필요시): ...

## 관련 이슈

- Closes #{이슈번호}

## 기타 / 검토 요청

<!-- 리뷰어가 특히 봐주었으면 하는 부분, 배포 주의사항 등 -->
```

## PR 생성 명령어

```bash
gh pr create \
  --base main \
  --title "feat: 러닝 세션 공유 링크 만료 시간 커스터마이징" \
  --body "$(cat <<'EOF'
## 개요

공유 링크의 TTL을 사용자가 지정할 수 있도록 합니다. 기존엔 `ShareConfig` 의 고정값만 사용했습니다.

## 변경 사항

### Modified
- `src/main/java/com/inseong/dallyrun/backend/config/ShareConfig.java`: `maxTtlSeconds` 프로퍼티 추가
- `src/main/java/com/inseong/dallyrun/backend/service/ShareServiceImpl.java`: `createLink(..., ttlSeconds)` 시그니처
- `src/main/java/com/inseong/dallyrun/backend/controller/ShareController.java`: 요청 바디에 `ttlSeconds` 수용

## 테스트

- [x] `./gradlew build` 통과
- [x] `ShareServiceImplTest` 정상/초과/음수 케이스 추가
- [ ] 수동 확인: POST /api/share 로 커스텀 TTL 생성 후 Redis key TTL 확인

## 관련 이슈

- Closes #42
EOF
)"
```

## 생성 전 체크리스트

- [ ] 브랜치가 `main` 이 아님 (feature 브랜치)
- [ ] `./gradlew build` 통과
- [ ] 커밋 메시지에 Codex/AI 언급 없음
- [ ] PR 본문에 Codex/AI 언급 없음 (`Co-Authored-By` 포함)
- [ ] 연관 이슈가 있다면 `Closes #N` 포함
- [ ] 한 커밋에 여러 의도가 섞이지 않음 (리팩토링 + 기능 추가 혼합 금지)

## 생성 후

- PR URL 출력
- 필요시 리뷰어 지정: `gh pr edit {번호} --add-reviewer <user>`
- CI 상태 확인: `gh pr checks {번호}`
