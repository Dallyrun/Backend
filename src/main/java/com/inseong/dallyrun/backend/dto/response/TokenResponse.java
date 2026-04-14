package com.inseong.dallyrun.backend.dto.response;

public record TokenResponse(
        String accessToken,
        String refreshToken
) {
}
