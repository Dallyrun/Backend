package com.inseong.dallyrun.backend.controller;

import com.inseong.dallyrun.backend.dto.response.ApiResponse;
import com.inseong.dallyrun.backend.dto.response.BadgeResponse;
import com.inseong.dallyrun.backend.dto.response.MemberBadgeResponse;
import com.inseong.dallyrun.backend.security.CustomUserDetails;
import com.inseong.dallyrun.backend.service.BadgeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Badge", description = "뱃지 API")
@RestController
public class BadgeController {

    private final BadgeService badgeService;

    public BadgeController(BadgeService badgeService) {
        this.badgeService = badgeService;
    }

    @Operation(summary = "전체 뱃지 목록")
    @GetMapping("/api/badges")
    public ResponseEntity<ApiResponse<List<BadgeResponse>>> getAllBadges() {
        List<BadgeResponse> response = badgeService.getAllBadges();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "내 획득 뱃지 목록")
    @GetMapping("/api/members/me/badges")
    public ResponseEntity<ApiResponse<List<MemberBadgeResponse>>> getMyBadges(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        List<MemberBadgeResponse> response = badgeService.getMemberBadges(userDetails.getMemberId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
