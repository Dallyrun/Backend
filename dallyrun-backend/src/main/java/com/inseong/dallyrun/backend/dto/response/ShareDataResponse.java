package com.inseong.dallyrun.backend.dto.response;

import com.inseong.dallyrun.backend.entity.RunningSession;

import java.time.LocalDateTime;
import java.util.List;

public record ShareDataResponse(
        String nickname,
        LocalDateTime startedAt,
        LocalDateTime endedAt,
        Double distanceMeters,
        Long durationSeconds,
        Double avgPace,
        List<GpsPointDto> gpsPoints
) {
    public static ShareDataResponse of(String nickname, RunningSession session) {
        List<GpsPointDto> gpsPoints = session.getGpsPoints().stream()
                .map(GpsPointDto::from)
                .toList();
        return new ShareDataResponse(
                nickname,
                session.getStartedAt(),
                session.getEndedAt(),
                session.getDistanceMeters(),
                session.getDurationSeconds(),
                session.getAvgPace(),
                gpsPoints
        );
    }
}
