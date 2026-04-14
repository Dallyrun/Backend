package com.inseong.dallyrun.backend.dto.response;

import com.inseong.dallyrun.backend.entity.Goal;
import com.inseong.dallyrun.backend.entity.enums.GoalType;
import com.inseong.dallyrun.backend.entity.enums.MetricType;

import java.time.LocalDate;

public record GoalResponse(
        Long id,
        GoalType goalType,
        MetricType metricType,
        Double targetValue,
        LocalDate startDate,
        LocalDate endDate,
        boolean active
) {
    public static GoalResponse from(Goal goal) {
        return new GoalResponse(
                goal.getId(),
                goal.getGoalType(),
                goal.getMetricType(),
                goal.getTargetValue(),
                goal.getStartDate(),
                goal.getEndDate(),
                goal.isActive()
        );
    }
}
