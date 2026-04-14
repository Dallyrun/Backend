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

@Tag(name = "Share", description = "공유 API")
@RestController
public class ShareController {

    private final ShareService shareService;

    public ShareController(ShareService shareService) {
        this.shareService = shareService;
    }

    @Operation(summary = "공유 카드 데이터 조회")
    @GetMapping("/api/running-sessions/{id}/share-data")
    public ResponseEntity<ApiResponse<ShareDataResponse>> getShareData(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id) {
        ShareDataResponse response = shareService.getShareData(userDetails.getMemberId(), id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "공유 링크 생성")
    @PostMapping("/api/running-sessions/{id}/share-link")
    public ResponseEntity<ApiResponse<ShareLinkResponse>> createShareLink(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id) {
        ShareLinkResponse response = shareService.createShareLink(userDetails.getMemberId(), id);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @Operation(summary = "공유 링크 조회 (인증 불필요)")
    @GetMapping("/api/shares/{shareCode}")
    public ResponseEntity<ApiResponse<ShareDataResponse>> getSharedData(
            @PathVariable String shareCode) {
        ShareDataResponse response = shareService.getSharedData(shareCode);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
