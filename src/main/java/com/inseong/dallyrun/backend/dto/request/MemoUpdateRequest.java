package com.inseong.dallyrun.backend.dto.request;

import jakarta.validation.constraints.Size;

public record MemoUpdateRequest(
        @Size(max = 500) String memo
) {
}
