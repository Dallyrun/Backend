package com.inseong.dallyrun.backend.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * 회원-배지 연결 엔티티 (다대다 관계 매핑 테이블).
 * member_id + badge_id 유니크 제약으로 동일 배지 중복 수여를 방지한다.
 * achievedAt에 배지 획득 시각을 기록한다.
 */
@Entity
@Table(name = "member_badge", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"member_id", "badge_id"})
})
public class MemberBadge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "badge_id", nullable = false)
    private Badge badge;

    @Column(nullable = false)
    private LocalDateTime achievedAt;

    protected MemberBadge() {
    }

    public MemberBadge(Member member, Badge badge) {
        this.member = member;
        this.badge = badge;
        this.achievedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public Member getMember() {
        return member;
    }

    public Badge getBadge() {
        return badge;
    }

    public LocalDateTime getAchievedAt() {
        return achievedAt;
    }
}
