package com.inseong.dallyrun.backend.dto.request;

import com.inseong.dallyrun.backend.entity.enums.GoalType;
import com.inseong.dallyrun.backend.entity.enums.MetricType;

import java.time.LocalDate;

public record GoalUpdateRequest(
        GoalType goalType,
        MetricType metricType,
        Double targetValue,
        LocalDate startDate,
        LocalDate endDate
) {
}
