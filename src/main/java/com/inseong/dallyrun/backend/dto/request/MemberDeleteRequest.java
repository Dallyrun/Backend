package com.inseong.dallyrun.backend.dto.request;

import jakarta.validation.constraints.NotBlank;

/**
 * 회원탈퇴 요청.
 * 계정 탈취 방지를 위해 현재 비밀번호를 한 번 더 받아 검증한다.
 */
public record MemberDeleteRequest(
        @NotBlank(message = "비밀번호는 필수입니다.")
        String password
) {
}
