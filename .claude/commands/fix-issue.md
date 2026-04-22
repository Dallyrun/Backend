---
description: GitHub 이슈를 읽고 feature 브랜치에서 구현·커밋·푸시·PR까지 진행합니다
argument-hint: "<이슈번호 | 이슈 URL>"
---

# /fix-issue — GitHub 이슈 해결 명령어

GitHub 이슈를 해결하고 PR을 생성합니다.

## 사용법

```
/fix-issue <이슈번호>
/fix-issue <이슈URL>
```

### 예시

```
/fix-issue 42
/fix-issue https://github.com/Dallyrun/Backend/issues/42
```

## 입력

- 이슈 URL 또는 번호: $ARGUMENTS

## 작업 순서

1. 현재 브랜치가 `main`이 아니라면, 작업 중인 변경사항을 `git stash`로 임시 저장하고 원래 브랜치명을 기억합니다.
2. `main` 브랜치로 checkout하고 최신 변경사항을 pull 받습니다.
   ```bash
   git checkout main
   git pull origin main
   ```
3. 이슈 내용을 확인합니다.
   ```bash
   gh issue view {이슈번호}
   ```
4. 이슈 라벨/내용에 맞는 새 브랜치를 생성합니다.
   - 기능 추가: `feat/이슈-짧은-설명`
   - 버그 수정: `fix/이슈-짧은-설명`
   - 리팩토링: `refactor/이슈-짧은-설명`
   - 문서: `docs/이슈-짧은-설명`
5. `CLAUDE.md` 의 MVC 계층 책임에 맞춰 구현합니다.
6. `/quality` 로 빌드·테스트가 통과하는지 확인합니다.
7. 변경사항을 커밋합니다.
   - **Conventional Commits** 형식 (`feat:`, `fix:`, `refactor:`, `docs:`, `test:`, `chore:`, `ci:`)
   - 커밋 본문에 `Closes #{이슈번호}` 를 포함하여 PR 머지 시 이슈 자동 종료
8. 브랜치를 push하고 PR을 생성합니다 (`create-pr` 스킬 사용).
9. 1단계에서 stash한 내용이 있으면 원래 브랜치로 돌아가 `git stash pop` 으로 복원합니다.
   - 충돌 발생 시 `git stash list` 로 확인 후 수동 해결 안내

## 커밋/PR 주의사항

- 커밋 메시지, PR 본문 어디에도 **Claude/AI 관련 언급을 넣지 않습니다** (Co-Authored-By 포함).
- 커밋 메시지는 한글로 작성합니다.
- PR 본문에 `Closes #{이슈번호}` 를 포함합니다.
- `main` 브랜치에 직접 커밋하지 않습니다.

## 예시 커밋 메시지

```
feat: 러닝 세션 공유 링크 만료 시간 커스터마이징

- ShareConfig 에 maxTtlSeconds 프로퍼티 추가
- ShareService.createLink()가 사용자 지정 TTL 을 받도록 변경
- 최대값 초과 시 INVALID_INPUT 예외

Closes #42
```
