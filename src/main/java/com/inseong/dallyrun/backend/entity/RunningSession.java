package com.inseong.dallyrun.backend.entity;

import com.inseong.dallyrun.backend.entity.enums.SessionStatus;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 러닝 세션 엔티티.
 * 생성 시 IN_PROGRESS 상태로 시작하며, 종료 시 거리·시간·페이스를 기록하고 COMPLETED로 전환된다.
 * GpsPoint와 1:N 관계를 가지며, sequenceIndex 오름차순으로 정렬된다.
 */
@Entity
@Table(name = "running_session")
public class RunningSession extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SessionStatus status;

    @Column(nullable = false)
    private LocalDateTime startedAt;

    private LocalDateTime endedAt;

    private Double distanceMeters;

    private Long durationSeconds;

    private Double avgPace;

    @Column(length = 500)
    private String memo;

    @OneToMany(mappedBy = "runningSession", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sequenceIndex ASC")
    private List<GpsPoint> gpsPoints = new ArrayList<>();

    protected RunningSession() {
    }

    public RunningSession(Member member) {
        this.member = member;
        this.status = SessionStatus.IN_PROGRESS;
        this.startedAt = LocalDateTime.now();
    }

    public void complete(LocalDateTime endedAt, Double distanceMeters, Long durationSeconds, Double avgPace) {
        this.status = SessionStatus.COMPLETED;
        this.endedAt = endedAt;
        this.distanceMeters = distanceMeters;
        this.durationSeconds = durationSeconds;
        this.avgPace = avgPace;
    }

    public void updateMemo(String memo) {
        this.memo = memo;
    }

    public Long getId() {
        return id;
    }

    public Member getMember() {
        return member;
    }

    public SessionStatus getStatus() {
        return status;
    }

    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    public LocalDateTime getEndedAt() {
        return endedAt;
    }

    public Double getDistanceMeters() {
        return distanceMeters;
    }

    public Long getDurationSeconds() {
        return durationSeconds;
    }

    public Double getAvgPace() {
        return avgPace;
    }

    public String getMemo() {
        return memo;
    }

    public List<GpsPoint> getGpsPoints() {
        return gpsPoints;
    }
}
