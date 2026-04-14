package com.inseong.dallyrun.backend.service;

import com.inseong.dallyrun.backend.dto.response.BadgeResponse;
import com.inseong.dallyrun.backend.dto.response.MemberBadgeResponse;
import com.inseong.dallyrun.backend.entity.RunningSession;

import java.util.List;

public interface BadgeService {

    List<BadgeResponse> getAllBadges();

    List<MemberBadgeResponse> getMemberBadges(Long memberId);

    void checkAndAwardBadges(Long memberId, RunningSession session);
}
