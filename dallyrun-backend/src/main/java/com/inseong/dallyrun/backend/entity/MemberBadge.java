package com.inseong.dallyrun.backend.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

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
