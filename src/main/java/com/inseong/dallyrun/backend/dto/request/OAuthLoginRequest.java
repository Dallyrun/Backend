package com.inseong.dallyrun.backend.dto.request;

import jakarta.validation.constraints.NotBlank;

public record OAuthLoginRequest(
        @NotBlank(message = "인증 코드는 필수입니다.")
        String authCode
) {
}
