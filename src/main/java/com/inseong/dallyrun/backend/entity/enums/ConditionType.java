package com.inseong.dallyrun.backend.entity.enums;

/**
 * 배지 부여 조건 유형.
 *
 * <ul>
 *   <li>{@link #TOTAL_DISTANCE} — 누적 총 거리(m) ≥ conditionValue</li>
 *   <li>{@link #TOTAL_COUNT} — 누적 완료 세션 수 ≥ conditionValue</li>
 *   <li>{@link #SINGLE_DISTANCE} — 현재 세션의 단일 거리(m) ≥ conditionValue</li>
 *   <li>{@link #STREAK_DAYS} — 연속 러닝 일수 ≥ conditionValue</li>
 *   <li>{@link #EARLY_MORNING_COUNT} — 새벽(04~06시) 시작 세션 누적 횟수 ≥ conditionValue</li>
 *   <li>{@link #LATE_NIGHT_COUNT} — 심야(22~03시 다음날) 시작 세션 누적 횟수 ≥ conditionValue</li>
 * </ul>
 */
public enum ConditionType {
    TOTAL_DISTANCE,
    TOTAL_COUNT,
    SINGLE_DISTANCE,
    STREAK_DAYS,
    EARLY_MORNING_COUNT,
    LATE_NIGHT_COUNT
}
