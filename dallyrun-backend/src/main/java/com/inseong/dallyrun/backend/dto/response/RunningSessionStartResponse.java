package com.inseong.dallyrun.backend.dto.response;

import com.inseong.dallyrun.backend.entity.RunningSession;

import java.time.LocalDateTime;

public record RunningSessionStartResponse(
        Long id,
        LocalDateTime startedAt
) {
    public static RunningSessionStartResponse from(RunningSession session) {
        return new RunningSessionStartResponse(session.getId(), session.getStartedAt());
    }
}
