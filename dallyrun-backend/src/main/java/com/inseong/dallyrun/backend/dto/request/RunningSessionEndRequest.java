package com.inseong.dallyrun.backend.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.List;

public record RunningSessionEndRequest(
        @Valid
        @NotNull(message = "GPS 데이터는 필수입니다.")
        List<GpsPointRequest> gpsPoints
) {
    public record GpsPointRequest(
            @NotNull(message = "위도는 필수입니다.")
            Double latitude,
            @NotNull(message = "경도는 필수입니다.")
            Double longitude,
            Double altitude,
            LocalDateTime recordedAt,
            @NotNull(message = "순서 인덱스는 필수입니다.")
            Integer sequenceIndex
    ) {
    }
}
