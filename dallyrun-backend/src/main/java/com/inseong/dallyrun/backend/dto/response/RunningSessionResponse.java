package com.inseong.dallyrun.backend.dto.response;

import com.inseong.dallyrun.backend.entity.RunningSession;
import com.inseong.dallyrun.backend.entity.enums.SessionStatus;

import java.time.LocalDateTime;

public record RunningSessionResponse(
        Long id,
        SessionStatus status,
        LocalDateTime startedAt,
        LocalDateTime endedAt,
        Double distanceMeters,
        Long durationSeconds,
        Double avgPace,
        String memo,
        LocalDateTime createdAt
) {
    public static RunningSessionResponse from(RunningSession session) {
        return new RunningSessionResponse(
                session.getId(),
                session.getStatus(),
                session.getStartedAt(),
                session.getEndedAt(),
                session.getDistanceMeters(),
                session.getDurationSeconds(),
                session.getAvgPace(),
                session.getMemo(),
                session.getCreatedAt()
        );
    }
}
