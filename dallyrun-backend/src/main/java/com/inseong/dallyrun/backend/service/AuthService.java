package com.inseong.dallyrun.backend.service;

import com.inseong.dallyrun.backend.dto.response.TokenResponse;
import com.inseong.dallyrun.backend.entity.enums.OAuthProvider;

public interface AuthService {

    TokenResponse oauthLogin(OAuthProvider provider, String authCode);

    TokenResponse refreshToken(String refreshToken);

    void logout(Long memberId);
}
