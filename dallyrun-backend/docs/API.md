# Dallyrun Backend API 문서

## 개요

Dallyrun 백엔드는 REST API 기반으로, 모든 응답은 아래 공통 포맷을 따릅니다.

성공 시:
```json
{
  "data": { ... }
}
```

에러 시:
```json
{
  "message": "에러 메시지"
}
```

## 인증

- OAuth2 로그인 후 발급받은 JWT Access Token을 `Authorization` 헤더에 포함합니다.
- `🔓` 표시가 없는 엔드포인트는 인증이 필요합니다.

```
Authorization: Bearer {accessToken}
```

| 토큰 | 만료 시간 | 저장 위치 |
|------|----------|----------|
| Access Token | 30분 | 앱 메모리 |
| Refresh Token | 14일 | 앱 로컬 저장소 |

---

## 전체 흐름 요약

```
┌─────────────────────────────────────────────────────┐
│  1. 로그인                                           │
│  앱에서 카카오/구글 SDK로 authCode 획득               │
│  → POST /api/auth/oauth/kakao (authCode 전송)       │
│  ← accessToken + refreshToken 수신                   │
└──────────────────────┬──────────────────────────────┘
                       ▼
┌─────────────────────────────────────────────────────┐
│  2. 러닝                                             │
│  [시작] POST /api/running-sessions                   │
│    ← sessionId + startedAt 수신                      │
│                                                      │
│  [러닝 중] 서버 통신 없음                              │
│    앱이 자체적으로 GPS 좌표를 로컬에 수집               │
│                                                      │
│  [종료] PATCH /api/running-sessions/{id}/end         │
│    → GPS 좌표 배열 일괄 전송                          │
│    ← 거리, 시간, 페이스 등 계산된 통계 수신            │
└──────────────────────┬──────────────────────────────┘
                       ▼
┌─────────────────────────────────────────────────────┐
│  3. 기록 확인 / 목표 / 뱃지 / 공유                    │
│  히스토리 조회, 목표 달성률 확인, 뱃지 확인, 공유       │
└─────────────────────────────────────────────────────┘
```

---

## 1. Auth — 인증

### 로그인 플로우

```
앱 (카카오 SDK)          백엔드                    카카오 서버
     │                    │                          │
     │── authCode 획득 ──→│                          │
     │                    │── authCode로 토큰 교환 ──→│
     │                    │←── access_token ─────────│
     │                    │── 유저 정보 조회 ────────→│
     │                    │←── email, nickname ──────│
     │                    │                          │
     │                    │ DB에 회원 조회/생성        │
     │                    │ JWT 발급                  │
     │                    │ Redis에 refresh 저장      │
     │                    │                          │
     │←── accessToken ───│                          │
     │    refreshToken    │                          │
```

### `POST /api/auth/oauth/kakao` 🔓

카카오 로그인. 앱에서 카카오 SDK로 받은 인증 코드를 전달합니다.

**Request**
```json
{
  "authCode": "카카오SDK에서_받은_인증코드"
}
```

**Response**
```json
{
  "data": {
    "accessToken": "eyJhbGciOi...",
    "refreshToken": "eyJhbGciOi..."
  }
}
```

### `POST /api/auth/oauth/google` 🔓

구글 로그인. 카카오와 동일한 형식입니다.

### `POST /api/auth/refresh` 🔓

Access Token 만료 시 Refresh Token으로 새 토큰을 발급받습니다.

**Request**
```json
{
  "refreshToken": "eyJhbGciOi..."
}
```

**Response** — 로그인과 동일한 `TokenResponse`

> **안드로이드 구현 가이드**: Access Token이 만료되면(401 응답) 이 API로 갱신 후 원래 요청을 재시도합니다. Refresh Token도 만료되면 로그인 화면으로 이동합니다.

### `DELETE /api/auth/logout`

로그아웃. 서버의 Refresh Token을 삭제합니다.

**Response** `200 OK` (본문 없음)

> **안드로이드 구현 가이드**: 로그아웃 시 앱 로컬의 토큰도 함께 삭제해야 합니다.

---

## 2. Member — 회원

### `GET /api/members/me`

내 프로필 조회.

**Response**
```json
{
  "data": {
    "id": 1,
    "email": "user@kakao.com",
    "nickname": "달리기왕",
    "profileImageUrl": "https://...",
    "oauthProvider": "KAKAO"
  }
}
```

### `PATCH /api/members/me`

프로필 수정. 변경할 필드만 보내면 됩니다.

**Request**
```json
{
  "nickname": "새닉네임",
  "profileImageUrl": "https://new-image-url"
}
```

### `DELETE /api/members/me`

계정 삭제 (탈퇴).

---

## 3. Running Session — 러닝

### 러닝 데이터 흐름 (중요)

```
┌─────────────────────────────────────────────────┐
│  앱 (Android)                                    │
│                                                  │
│  1. 러닝 시작 버튼 클릭                           │
│     → POST /api/running-sessions                 │
│     ← { id: 42, startedAt: "2026-04-14T..." }   │
│     → sessionId=42 를 메모리에 저장               │
│                                                  │
│  2. 러닝 중 (서버 통신 없음)                      │
│     → GPS Provider로 좌표 수집                    │
│     → 로컬 배열에 축적:                           │
│       [                                          │
│         { lat, lng, alt, time, index: 0 },       │
│         { lat, lng, alt, time, index: 1 },       │
│         ...                                      │
│       ]                                          │
│     → 앱 자체적으로 실시간 거리/페이스 계산 (UI용)  │
│                                                  │
│  3. 러닝 종료 버튼 클릭                           │
│     → PATCH /api/running-sessions/42/end         │
│       Body: { gpsPoints: [...수집한 전체 배열] }  │
│     ← 서버가 거리/시간/페이스 계산 후 응답         │
│                                                  │
│  ※ 러닝 중 앱 강제종료 대비:                      │
│    GPS 데이터를 Room DB 등에 중간 저장 권장        │
└─────────────────────────────────────────────────┘
```

### `POST /api/running-sessions`

러닝 시작. 이미 진행 중인 세션이 있으면 `409 Conflict`.

**Response** `201 Created`
```json
{
  "data": {
    "id": 42,
    "startedAt": "2026-04-14T09:30:00"
  }
}
```

### `PATCH /api/running-sessions/{id}/end`

러닝 종료 + GPS 데이터 일괄 전송. 서버가 총 거리, 소요 시간, 평균 페이스를 계산합니다.

**Request**
```json
{
  "gpsPoints": [
    {
      "latitude": 37.5666,
      "longitude": 126.9784,
      "altitude": 38.5,
      "recordedAt": "2026-04-14T09:30:05",
      "sequenceIndex": 0
    },
    {
      "latitude": 37.5670,
      "longitude": 126.9790,
      "altitude": 39.0,
      "recordedAt": "2026-04-14T09:30:10",
      "sequenceIndex": 1
    }
  ]
}
```

| 필드 | 필수 | 설명 |
|------|------|------|
| `latitude` | O | 위도 |
| `longitude` | O | 경도 |
| `altitude` | X | 고도 (미터). GPS에서 제공 안 하면 null |
| `recordedAt` | X | GPS 좌표가 기록된 시각 |
| `sequenceIndex` | O | 순서 (0부터 시작). 경로 순서 보장용 |

> **안드로이드 구현 가이드**: GPS 수집 주기는 1~3초 권장. 너무 짧으면 데이터가 커지고, 너무 길면 경로가 부정확합니다. `sequenceIndex`는 수집 순서대로 0, 1, 2... 를 부여하면 됩니다.

**Response**
```json
{
  "data": {
    "id": 42,
    "status": "COMPLETED",
    "startedAt": "2026-04-14T09:30:00",
    "endedAt": "2026-04-14T10:00:00",
    "distanceMeters": 5234.7,
    "durationSeconds": 1800,
    "avgPace": 5.73,
    "memo": null,
    "createdAt": "2026-04-14T09:30:00"
  }
}
```

| 응답 필드 | 설명 |
|----------|------|
| `distanceMeters` | 총 거리 (미터). GPS 좌표 기반 Haversine 공식으로 계산 |
| `durationSeconds` | 총 시간 (초). startedAt ~ 종료 시점 |
| `avgPace` | 평균 페이스 (분/km). 예: 5.73 → 1km당 5분 44초 |

### `GET /api/running-sessions`

러닝 히스토리 목록. 페이징 지원 (최신순).

**Query Params**
| 파라미터 | 기본값 | 설명 |
|---------|-------|------|
| `page` | 0 | 페이지 번호 (0부터 시작) |
| `size` | 20 | 페이지당 항목 수 |

**Response**
```json
{
  "data": {
    "content": [
      {
        "id": 42,
        "status": "COMPLETED",
        "startedAt": "2026-04-14T09:30:00",
        "endedAt": "2026-04-14T10:00:00",
        "distanceMeters": 5234.7,
        "durationSeconds": 1800,
        "avgPace": 5.73,
        "memo": "한강 러닝",
        "createdAt": "2026-04-14T09:30:00"
      }
    ],
    "totalElements": 15,
    "totalPages": 1,
    "number": 0,
    "size": 20,
    "first": true,
    "last": true
  }
}
```

### `GET /api/running-sessions/{id}`

러닝 상세 조회. GPS 경로 데이터를 포함합니다.

**Response**
```json
{
  "data": {
    "id": 42,
    "status": "COMPLETED",
    "startedAt": "2026-04-14T09:30:00",
    "endedAt": "2026-04-14T10:00:00",
    "distanceMeters": 5234.7,
    "durationSeconds": 1800,
    "avgPace": 5.73,
    "memo": null,
    "gpsPoints": [
      { "latitude": 37.5666, "longitude": 126.9784, "altitude": 38.5, "recordedAt": "...", "sequenceIndex": 0 },
      { "latitude": 37.5670, "longitude": 126.9790, "altitude": 39.0, "recordedAt": "...", "sequenceIndex": 1 }
    ],
    "createdAt": "2026-04-14T09:30:00"
  }
}
```

> **안드로이드 구현 가이드**: 목록에서는 GPS 데이터가 빠져있고, 상세에서만 포함됩니다. 지도에 경로를 그릴 때는 상세 API를 호출하세요.

### `PATCH /api/running-sessions/{id}`

메모 수정.

**Request**
```json
{
  "memo": "한강 러닝 최고!"
}
```

### `DELETE /api/running-sessions/{id}`

러닝 기록 삭제.

---

## 4. Goal — 목표

### `POST /api/goals`

목표 생성.

**Request**
```json
{
  "goalType": "WEEKLY",
  "metricType": "DISTANCE",
  "targetValue": 30000,
  "startDate": "2026-04-13",
  "endDate": "2026-04-19"
}
```

| 필드 | 값 | 설명 |
|------|---|------|
| `goalType` | `WEEKLY` / `MONTHLY` | 주간 / 월간 |
| `metricType` | `DISTANCE` / `TIME` / `COUNT` | 거리(m) / 시간(초) / 횟수 |
| `targetValue` | 숫자 | DISTANCE=미터, TIME=초, COUNT=횟수 |
| `startDate` | `yyyy-MM-dd` | 시작일 |
| `endDate` | `yyyy-MM-dd` | 종료일 |

**Response** `201 Created`
```json
{
  "data": {
    "id": 1,
    "goalType": "WEEKLY",
    "metricType": "DISTANCE",
    "targetValue": 30000,
    "startDate": "2026-04-13",
    "endDate": "2026-04-19",
    "active": true
  }
}
```

### `GET /api/goals`

활성 목표 목록.

### `GET /api/goals/{id}`

목표 상세 + 달성률.

**Response**
```json
{
  "data": {
    "id": 1,
    "goalType": "WEEKLY",
    "metricType": "DISTANCE",
    "targetValue": 30000,
    "currentValue": 15200,
    "progressRate": 50.7,
    "startDate": "2026-04-13",
    "endDate": "2026-04-19",
    "active": true
  }
}
```

| 응답 필드 | 설명 |
|----------|------|
| `currentValue` | 현재까지 달성한 값 (해당 기간 내 완료된 러닝 기준) |
| `progressRate` | 달성률 (%). 최대 100.0 |

### `PATCH /api/goals/{id}`

목표 수정. 변경할 필드만 보냅니다.

### `DELETE /api/goals/{id}`

목표 삭제 (비활성 처리).

---

## 5. Badge — 뱃지

뱃지는 러닝 종료 시 서버가 자동으로 조건을 판정하여 부여합니다. 앱에서 별도로 요청할 필요 없습니다.

### 뱃지 종류

| 이름 | 조건 | 값 |
|------|------|---|
| 첫 러닝 | 총 완료 횟수 | 1회 |
| 10회 달성 | 총 완료 횟수 | 10회 |
| 50회 달성 | 총 완료 횟수 | 50회 |
| 100회 달성 | 총 완료 횟수 | 100회 |
| 5K 러너 | 1회 거리 | 5,000m |
| 10K 러너 | 1회 거리 | 10,000m |
| 하프 마라톤 | 1회 거리 | 21,097m |
| 풀 마라톤 | 1회 거리 | 42,195m |
| 100km 돌파 | 누적 거리 | 100,000m |
| 500km 돌파 | 누적 거리 | 500,000m |
| 7일 연속 | 연속 러닝 | 7일 |
| 30일 연속 | 연속 러닝 | 30일 |

### `GET /api/badges`

전체 뱃지 목록 (획득 여부 무관).

**Response**
```json
{
  "data": [
    {
      "id": 1,
      "name": "첫 러닝",
      "description": "첫 번째 러닝을 완료했어요!",
      "iconUrl": "/badges/first-run.png",
      "conditionType": "TOTAL_COUNT",
      "conditionValue": 1.0
    }
  ]
}
```

### `GET /api/members/me/badges`

내가 획득한 뱃지 목록.

**Response**
```json
{
  "data": [
    {
      "id": 1,
      "badge": {
        "id": 1,
        "name": "첫 러닝",
        "description": "첫 번째 러닝을 완료했어요!",
        "iconUrl": "/badges/first-run.png",
        "conditionType": "TOTAL_COUNT",
        "conditionValue": 1.0
      },
      "achievedAt": "2026-04-14T10:00:05"
    }
  ]
}
```

> **안드로이드 구현 가이드**: 러닝 종료 응답을 받은 후 뱃지 목록을 다시 조회하면 새로 획득한 뱃지를 확인할 수 있습니다. 러닝 종료 후 뱃지 목록 API를 호출하여 이전 목록과 비교하면 "새 뱃지 획득" 알림을 표시할 수 있습니다.

---

## 6. Share — 공유

공유 카드 이미지는 앱(프론트)에서 생성합니다. 백엔드는 카드에 들어갈 데이터와 공유 링크를 제공합니다.

### `GET /api/running-sessions/{id}/share-data`

공유 카드에 표시할 데이터 조회.

**Response**
```json
{
  "data": {
    "nickname": "달리기왕",
    "startedAt": "2026-04-14T09:30:00",
    "endedAt": "2026-04-14T10:00:00",
    "distanceMeters": 5234.7,
    "durationSeconds": 1800,
    "avgPace": 5.73,
    "gpsPoints": [ ... ]
  }
}
```

### `POST /api/running-sessions/{id}/share-link`

공유 링크 생성. Redis에 30일간 저장.

**Response** `201 Created`
```json
{
  "data": {
    "shareCode": "a1b2c3d4e5f6",
    "shareUrl": "/api/shares/a1b2c3d4e5f6"
  }
}
```

### `GET /api/shares/{shareCode}` 🔓

공유 링크로 러닝 데이터 조회. 인증 불필요.

**Response** — `ShareDataResponse`와 동일

---

## 에러 코드

| HTTP | 코드 | 메시지 |
|------|------|--------|
| 400 | INVALID_INPUT | 잘못된 입력입니다. |
| 400 | INVALID_OAUTH_CODE | 유효하지 않은 인증 코드입니다. |
| 401 | INVALID_TOKEN | 유효하지 않은 토큰입니다. |
| 401 | EXPIRED_TOKEN | 만료된 토큰입니다. |
| 401 | UNAUTHORIZED | 인증이 필요합니다. |
| 403 | ACCESS_DENIED | 접근 권한이 없습니다. |
| 404 | MEMBER_NOT_FOUND | 회원을 찾을 수 없습니다. |
| 404 | RUNNING_SESSION_NOT_FOUND | 러닝 세션을 찾을 수 없습니다. |
| 404 | GOAL_NOT_FOUND | 목표를 찾을 수 없습니다. |
| 404 | BADGE_NOT_FOUND | 뱃지를 찾을 수 없습니다. |
| 404 | SHARE_NOT_FOUND | 공유 링크를 찾을 수 없습니다. |
| 409 | RUNNING_SESSION_ALREADY_ACTIVE | 이미 진행 중인 러닝 세션이 있습니다. |
| 409 | RUNNING_SESSION_ALREADY_COMPLETED | 이미 종료된 러닝 세션입니다. |
| 500 | INTERNAL_ERROR | 서버 내부 오류가 발생했습니다. |

---

## Enum 값 정리

| Enum | 값 | 설명 |
|------|---|------|
| OAuthProvider | `KAKAO`, `GOOGLE` | 소셜 로그인 제공자 |
| SessionStatus | `IN_PROGRESS`, `COMPLETED` | 러닝 세션 상태 |
| GoalType | `WEEKLY`, `MONTHLY` | 목표 유형 |
| MetricType | `DISTANCE`, `TIME`, `COUNT` | 목표 측정 기준 (미터/초/횟수) |
| ConditionType | `TOTAL_DISTANCE`, `TOTAL_COUNT`, `SINGLE_DISTANCE`, `STREAK_DAYS` | 뱃지 조건 유형 |
