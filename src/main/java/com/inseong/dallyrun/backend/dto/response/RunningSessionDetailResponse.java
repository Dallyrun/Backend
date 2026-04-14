package com.inseong.dallyrun.backend.dto.response;

import com.inseong.dallyrun.backend.entity.RunningSession;
import com.inseong.dallyrun.backend.entity.enums.SessionStatus;

import java.time.LocalDateTime;
import java.util.List;

public record RunningSessionDetailResponse(
        Long id,
        SessionStatus status,
        LocalDateTime startedAt,
        LocalDateTime endedAt,
        Double distanceMeters,
        Long durationSeconds,
        Double avgPace,
        String memo,
        List<GpsPointDto> gpsPoints,
        LocalDateTime createdAt
) {
    public static RunningSessionDetailResponse from(RunningSession session) {
        List<GpsPointDto> gpsPoints = session.getGpsPoints().stream()
                .map(GpsPointDto::from)
                .toList();
        return new RunningSessionDetailResponse(
                session.getId(),
                session.getStatus(),
                session.getStartedAt(),
                session.getEndedAt(),
                session.getDistanceMeters(),
                session.getDurationSeconds(),
                session.getAvgPace(),
                session.getMemo(),
                gpsPoints,
                session.getCreatedAt()
        );
    }
}
