package com.inseong.dallyrun.backend.dto.response;

import com.inseong.dallyrun.backend.entity.Badge;
import com.inseong.dallyrun.backend.entity.enums.ConditionType;

public record BadgeResponse(
        Long id,
        String name,
        String description,
        String iconUrl,
        ConditionType conditionType,
        Double conditionValue
) {
    public static BadgeResponse from(Badge badge) {
        return new BadgeResponse(
                badge.getId(),
                badge.getName(),
                badge.getDescription(),
                badge.getIconUrl(),
                badge.getConditionType(),
                badge.getConditionValue()
        );
    }
}
