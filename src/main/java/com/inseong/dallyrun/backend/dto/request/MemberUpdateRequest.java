package com.inseong.dallyrun.backend.dto.request;

public record MemberUpdateRequest(
        String nickname,
        String profileImageUrl
) {
}
