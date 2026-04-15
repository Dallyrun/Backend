package com.inseong.dallyrun.backend.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * GPS 좌표 엔티티.
 * 러닝 세션 종료 시 클라이언트로부터 수신한 경로 데이터를 저장한다.
 * sequenceIndex로 시간순 정렬하여 이동 경로를 재구성할 수 있다.
 */
@Entity
@Table(name = "gps_point")
public class GpsPoint extends BaseTimeEntity {

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
