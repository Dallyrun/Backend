package com.inseong.dallyrun.backend.dto.request;

import com.inseong.dallyrun.backend.entity.enums.GoalType;
import com.inseong.dallyrun.backend.entity.enums.MetricType;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;

public record GoalUpdateRequest(
        GoalType goalType,
        MetricType metricType,
        @Positive Double targetValue,
        LocalDate startDate,
        LocalDate endDate
) {
}
