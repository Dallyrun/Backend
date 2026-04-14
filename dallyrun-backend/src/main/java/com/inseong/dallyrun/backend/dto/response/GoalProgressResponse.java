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
