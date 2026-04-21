package com.inseong.dallyrun.backend.service;

import com.inseong.dallyrun.backend.dto.request.LoginRequest;
import com.inseong.dallyrun.backend.dto.request.SignupRequest;
import com.inseong.dallyrun.backend.dto.response.TokenResponse;

public interface AuthService {

    TokenResponse signup(SignupRequest request);

    TokenResponse login(LoginRequest request);

    TokenResponse refreshToken(String refreshToken);

    void logout(Long memberId);
}
