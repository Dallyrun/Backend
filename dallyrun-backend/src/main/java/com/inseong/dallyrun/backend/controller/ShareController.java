package com.inseong.dallyrun.backend.controller;

import com.inseong.dallyrun.backend.dto.response.ApiResponse;
import com.inseong.dallyrun.backend.dto.response.ShareDataResponse;
import com.inseong.dallyrun.backend.dto.response.ShareLinkResponse;
import com.inseong.dallyrun.backend.security.CustomUserDetails;
import com.inseong.dallyrun.backend.service.ShareService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Share", description = "공유 API — 러닝 기록 공유 데이터 조회 및 공유 링크 생성. "
        + "공유 카드 이미지는 앱(프론트)에서 생성하며, 백엔드는 카드에 들어갈 데이터를 제공합니다.")
@RestController
public class ShareController {

    private final ShareService shareService;

    public ShareController(ShareService shareService) {
        this.shareService = shareService;
    }

    @Operation(
            summary = "공유 카드 데이터 조회",
            description = "특정 러닝 세션의 공유 카드에 표시할 데이터를 조회합니다. "
                    + "닉네임, 거리, 시간, 페이스, GPS 경로가 포함됩니다. "
                    + "앱에서 이 데이터로 공유 카드 이미지를 생성합니다."
    )
    @GetMapping("/api/running-sessions/{id}/share-data")
    public ResponseEntity<ApiResponse<ShareDataResponse>> getShareData(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id) {
        ShareDataResponse response = shareService.getShareData(userDetails.getMemberId(), id);
        return ResponseEntity.ok(ApiResponse.of(response));
    }

    @Operation(
            summary = "공유 링크 생성",
            description = "러닝 기록의 공유 링크를 생성합니다. "
                    + "12자리 고유 코드가 생성되며, Redis에 30일간 저장됩니다. "
                    + "반환된 shareUrl로 인증 없이 러닝 데이터를 조회할 수 있습니다."
    )
    @PostMapping("/api/running-sessions/{id}/share-link")
    public ResponseEntity<ApiResponse<ShareLinkResponse>> createShareLink(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id) {
        ShareLinkResponse response = shareService.createShareLink(userDetails.getMemberId(), id);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.of(response));
    }

    @Operation(
            summary = "공유 링크로 러닝 데이터 조회 (인증 불필요)",
            description = "공유 코드로 러닝 데이터를 조회합니다. 인증이 필요 없어 누구나 접근 가능합니다. "
                    + "공유 링크는 생성 후 30일간 유효합니다. 만료된 링크는 404를 반환합니다."
    )
    @GetMapping("/api/shares/{shareCode}")
    public ResponseEntity<ApiResponse<ShareDataResponse>> getSharedData(
            @PathVariable String shareCode) {
        ShareDataResponse response = shareService.getSharedData(shareCode);
        return ResponseEntity.ok(ApiResponse.of(response));
    }
}
