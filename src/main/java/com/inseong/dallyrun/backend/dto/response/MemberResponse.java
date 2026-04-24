package com.inseong.dallyrun.backend.dto.response;

import com.inseong.dallyrun.backend.entity.AgeBracket;
import com.inseong.dallyrun.backend.entity.Gender;
import com.inseong.dallyrun.backend.entity.Member;

public record MemberResponse(
        Long id,
        String email,
        String nickname,
        String profileImageUrl,
        AgeBracket ageBracket,
        Gender gender
) {
    public static MemberResponse from(Member member) {
        return new MemberResponse(
                member.getId(),
                member.getEmail(),
                member.getNickname(),
                member.getProfileImageUrl(),
                member.getAgeBracket(),
                member.getGender()
        );
    }
}
