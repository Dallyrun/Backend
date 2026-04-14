package com.inseong.dallyrun.backend.dto.response;

import com.inseong.dallyrun.backend.entity.GpsPoint;

import java.time.LocalDateTime;

public record GpsPointDto(
        Double latitude,
        Double longitude,
        Double altitude,
        LocalDateTime recordedAt,
        Integer sequenceIndex
) {
    public static GpsPointDto from(GpsPoint gpsPoint) {
        return new GpsPointDto(
                gpsPoint.getLatitude(),
                gpsPoint.getLongitude(),
                gpsPoint.getAltitude(),
                gpsPoint.getRecordedAt(),
                gpsPoint.getSequenceIndex()
        );
    }
}
