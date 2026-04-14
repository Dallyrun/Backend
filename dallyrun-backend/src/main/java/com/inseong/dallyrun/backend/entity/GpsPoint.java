package com.inseong.dallyrun.backend.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "gps_point")
public class GpsPoint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "running_session_id", nullable = false)
    private RunningSession runningSession;

    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;

    private Double altitude;

    private LocalDateTime recordedAt;

    @Column(nullable = false)
    private Integer sequenceIndex;

    protected GpsPoint() {
    }

    public GpsPoint(RunningSession runningSession, Double latitude, Double longitude,
                    Double altitude, LocalDateTime recordedAt, Integer sequenceIndex) {
        this.runningSession = runningSession;
        this.latitude = latitude;
        this.longitude = longitude;
        this.altitude = altitude;
        this.recordedAt = recordedAt;
        this.sequenceIndex = sequenceIndex;
    }

    public Long getId() {
        return id;
    }

    public RunningSession getRunningSession() {
        return runningSession;
    }

    public Double getLatitude() {
        return latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public Double getAltitude() {
        return altitude;
    }

    public LocalDateTime getRecordedAt() {
        return recordedAt;
    }

    public Integer getSequenceIndex() {
        return sequenceIndex;
    }
}
