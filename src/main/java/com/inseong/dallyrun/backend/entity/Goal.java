package com.inseong.dallyrun.backend.entity;

import com.inseong.dallyrun.backend.entity.enums.GoalType;
import com.inseong.dallyrun.backend.entity.enums.MetricType;
import jakarta.persistence.*;

import java.time.LocalDate;

/**
 * 러닝 목표 엔티티.
 * 기간(startDate~endDate) 내에 특정 지표(거리/시간/횟수)의 목표값 달성을 추적한다.
 * 삭제 시 물리 삭제 대신 active=false로 비활성화(논리 삭제)한다.
 */
@Entity
@Table(name = "goal")
public class Goal extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GoalType goalType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MetricType metricType;

    @Column(nullable = false)
    private Double targetValue;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    @Column(nullable = false)
    private boolean active = true;

    protected Goal() {
    }

    public Goal(Member member, GoalType goalType, MetricType metricType,
                Double targetValue, LocalDate startDate, LocalDate endDate) {
        this.member = member;
        this.goalType = goalType;
        this.metricType = metricType;
        this.targetValue = targetValue;
        this.startDate = startDate;
        this.endDate = endDate;
        this.active = true;
    }

    public void update(GoalType goalType, MetricType metricType,
                       Double targetValue, LocalDate startDate, LocalDate endDate) {
        if (goalType != null) this.goalType = goalType;
        if (metricType != null) this.metricType = metricType;
        if (targetValue != null) this.targetValue = targetValue;
        if (startDate != null) this.startDate = startDate;
        if (endDate != null) this.endDate = endDate;
    }

    public void deactivate() {
        this.active = false;
    }

    public Long getId() {
        return id;
    }

    public Member getMember() {
        return member;
    }

    public GoalType getGoalType() {
        return goalType;
    }

    public MetricType getMetricType() {
        return metricType;
    }

    public Double getTargetValue() {
        return targetValue;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public boolean isActive() {
        return active;
    }
}
