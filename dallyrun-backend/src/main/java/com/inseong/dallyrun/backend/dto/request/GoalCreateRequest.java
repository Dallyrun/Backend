package com.inseong.dallyrun.backend.dto.request;

import com.inseong.dallyrun.backend.entity.enums.GoalType;
import com.inseong.dallyrun.backend.entity.enums.MetricType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;

public record GoalCreateRequest(
        @NotNull(message = "목표 유형은 필수입니다.")
        GoalType goalType,
        @NotNull(message = "측정 유형은 필수입니다.")
        MetricType metricType,
        @NotNull(message = "목표값은 필수입니다.")
        @Positive(message = "목표값은 양수여야 합니다.")
        Double targetValue,
        @NotNull(message = "시작일은 필수입니다.")
        LocalDate startDate,
        @NotNull(message = "종료일은 필수입니다.")
        LocalDate endDate
) {
}
