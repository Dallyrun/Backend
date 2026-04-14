package com.inseong.dallyrun.backend.service.oauth;

import com.inseong.dallyrun.backend.entity.enums.OAuthProvider;

public record OAuthUserInfo(
        OAuthProvider provider,
        String providerId,
        String email,
        String nickname,
        String profileImageUrl
) {
}
