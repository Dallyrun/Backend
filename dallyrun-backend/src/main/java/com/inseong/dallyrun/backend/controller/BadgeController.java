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

@Tag(name = "Badge", description = "뱃지 API — 뱃지는 러닝 종료 시 서버가 자동으로 조건을 판정하여 부여합니다. "
        + "앱에서 별도로 부여를 요청할 필요 없습니다.")
@RestController
public class BadgeController {

    private final BadgeService badgeService;

    public BadgeController(BadgeService badgeService) {
        this.badgeService = badgeService;
    }

    @Operation(
            summary = "전체 뱃지 목록",
            description = "시스템에 등록된 모든 뱃지를 조회합니다. 획득 여부와 무관하게 전체 목록을 반환합니다. "
                    + "앱에서 뱃지 도감 화면을 구성할 때 사용합니다. "
                    + "conditionType/conditionValue로 획득 조건을 표시할 수 있습니다."
    )
    @GetMapping("/api/badges")
    public ResponseEntity<ApiResponse<List<BadgeResponse>>> getAllBadges() {
        List<BadgeResponse> response = badgeService.getAllBadges();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(
            summary = "내가 획득한 뱃지 목록",
            description = "로그인한 사용자가 획득한 뱃지 목록을 조회합니다. "
                    + "각 뱃지의 상세 정보와 획득 시각(achievedAt)이 포함됩니다.\n\n"
                    + "**팁**: 러닝 종료 후 이 API를 호출하여 이전 목록과 비교하면 '새 뱃지 획득' 알림을 표시할 수 있습니다."
    )
    @GetMapping("/api/members/me/badges")
    public ResponseEntity<ApiResponse<List<MemberBadgeResponse>>> getMyBadges(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        List<MemberBadgeResponse> response = badgeService.getMemberBadges(userDetails.getMemberId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
