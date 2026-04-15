package com.inseong.dallyrun.backend.dto.request;

import jakarta.validation.constraints.Size;

public record MemberUpdateRequest(
        @Size(max = 50) String nickname,
        @Size(max = 500) String profileImageUrl
) {
}
