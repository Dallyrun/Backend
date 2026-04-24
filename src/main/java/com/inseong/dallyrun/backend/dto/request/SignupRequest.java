package com.inseong.dallyrun.backend.dto.request;

import com.inseong.dallyrun.backend.entity.AgeBracket;
import com.inseong.dallyrun.backend.entity.Gender;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record SignupRequest(
        @NotBlank(message = "이메일은 필수입니다.")
        @Email(message = "유효한 이메일 형식이어야 합니다.")
        String email,

        @NotBlank(message = "비밀번호는 필수입니다.")
        @Size(min = 8, max = 30, message = "비밀번호는 8자 이상 30자 이하여야 합니다.")
        @Pattern(
                regexp = "^(?=.*[A-Za-z])(?=.*[0-9])(?=.*[!-/:-@\\[-`{-~])[A-Za-z0-9!-/:-@\\[-`{-~]+$",
                message = "비밀번호는 영문자, 숫자, ASCII 특수기호를 각 1자 이상 포함해야 하며 허용된 ASCII 문자만 사용할 수 있습니다."
        )
        String password,

        @NotBlank(message = "닉네임은 필수입니다.")
        @Size(min = 2, max = 12, message = "닉네임은 2자 이상 12자 이하여야 합니다.")
        @Pattern(
                regexp = "^[가-힣A-Za-z0-9]+$",
                message = "닉네임은 한글, 영문, 숫자만 사용할 수 있습니다."
        )
        String nickname,

        @NotNull(message = "연령대는 필수입니다.")
        AgeBracket ageBracket,

        @NotNull(message = "성별은 필수입니다.")
        Gender gender
) {
}
