package com.inseong.dallyrun.backend.dto.response;

import com.inseong.dallyrun.backend.entity.Member;

public record MemberResponse(
        Long id,
        String email,
        String nickname,
        String profileImageUrl
) {
    public static MemberResponse from(Member member) {
        return new MemberResponse(
                member.getId(),
                member.getEmail(),
                member.getNickname(),
                member.getProfileImageUrl()
        );
    }
}
