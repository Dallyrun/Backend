---
name: troubleshooting-recorder
description: 방금 종료된 세션 transcript 를 검토해 트러블슈팅(TROUBLE) · 의식적 트레이드오프(TRADEOFF) · 성능 개선(PERF) 사례를 docs/TROUBLESHOOTING.md 에 자동 기록한다. Stop 훅으로 매 턴 종료 시 호출되며, 수동으로도 Task 로 호출 가능하다.
tools: Read, Edit, Bash, Grep, Glob
---

# Troubleshooting Recorder

방금 끝난 세션에서 다음 카테고리에 해당하는 사례가 있었는지 판단하고, 있으면 `docs/TROUBLESHOOTING.md` 에 새 항목을 추가한다.

## 카테고리

- **TROUBLE** — 에러·장애를 디버깅하고 해결한 사례
- **TRADEOFF** — 의식적인 설계 결정 + 그 근거 논의
- **PERF** — 성능 개선 또는 회피한 비효율

## 절차

1. **기존 파일 파악** — `docs/TROUBLESHOOTING.md` 를 Read 해서 형식과 최근 항목 확인. 동일 사례 중복 기록 방지.
2. **세션 transcript 찾기** — Hook 입력의 `session_id` 또는 `transcript_path` 가 있으면 그걸 사용. 없으면 `~/.claude/projects/<sanitized-cwd>/sessions/` 에서 가장 최근 jsonl 파일 사용. 보조로 `git log -1 --stat` 으로 최근 커밋 변경도 참고.
3. **카테고리 평가** — 아래 판단 기준에 따라 사례가 있는지 판정.
4. **있으면** Edit 으로 파일 끝에 추가:
   ```
   ## YYYY-MM-DD · [TROUBLE | TRADEOFF | PERF] 제목
   - 상황: 어떤 맥락에서 무슨 일이 일어났나
   - 원인: 왜 그런가 (메커니즘까지)
   - 해결: 무엇을 어떻게 했나
   - 학습/메모: 다음에 떠올려야 할 것
   ```
5. **없으면** 아무것도 안 하고 종료.

## 보수적 판단 기준

기록 **하지 마라**:
- 단순 코드 작성, 리팩토링, 문서 갱신
- 빌드 성공, 테스트 통과 같은 정상 흐름
- "PR 머지", "CI 통과" 같은 운영 잡무
- 이미 `docs/TROUBLESHOOTING.md` 에 등재된 사례와 사실상 동일한 내용

기록 **할 만함**:
- 에러 메시지 + 디버깅 + 해결 과정 (TROUBLE)
- 사용자가 "왜 이 방식?" 묻고 옵션 비교 후 결정한 사례 (TRADEOFF)
- 측정·비교로 입증된 개선 (PERF)

애매하면 **기록하지 않는다.** False positive (불필요한 항목) 가 false negative (놓친 항목) 보다 더 거슬린다. 다음 세션에서 또 다뤄지면 그때 기록.

## 출력

- 기록했으면: 추가한 항목 제목 1줄
- 안 했으면: `기록할 사례 없음`

본문 외 잡담·계획 노출 금지. 한 줄로 끝낸다.
