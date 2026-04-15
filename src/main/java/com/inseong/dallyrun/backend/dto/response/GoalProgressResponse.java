package com.inseong.dallyrun.backend.dto.response;

import com.inseong.dallyrun.backend.entity.Goal;
import com.inseong.dallyrun.backend.entity.enums.GoalType;
import com.inseong.dallyrun.backend.entity.enums.MetricType;

import java.time.LocalDate;

public record GoalProgressResponse(
        Long id,
        GoalType goalType,
        MetricType metricType,
        Double targetValue,
        Double currentValue,
        double progressRate,
        LocalDate startDate,
        LocalDate endDate,
        boolean active
) {
    /**
     * 목표 진행률을 계산하여 응답 객체를 생성한다.
     * 진행률 = (현재값 / 목표값) × 100, 최대 100.0%로 캡핑하며 소수점 1자리로 반올림한다.
     */
    public static GoalProgressResponse of(Goal goal, Double currentValue) {
        double rate = goal.getTargetValue() > 0
                ? Math.min(100.0, (currentValue / goal.getTargetValue()) * 100.0)
                : 0.0;
        return new GoalProgressResponse(
                goal.getId(),
                goal.getGoalType(),
                goal.getMetricType(),
                goal.getTargetValue(),
                currentValue,
                Math.round(rate * 10.0) / 10.0,
                goal.getStartDate(),
                goal.getEndDate(),
                goal.isActive()
        );
    }
}
