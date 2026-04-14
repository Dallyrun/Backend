package com.inseong.dallyrun.backend.dto.response;

import com.inseong.dallyrun.backend.entity.MemberBadge;

import java.time.LocalDateTime;

public record MemberBadgeResponse(
        Long id,
        BadgeResponse badge,
        LocalDateTime achievedAt
) {
    public static MemberBadgeResponse from(MemberBadge memberBadge) {
        return new MemberBadgeResponse(
                memberBadge.getId(),
                BadgeResponse.from(memberBadge.getBadge()),
                memberBadge.getAchievedAt()
        );
    }
}
