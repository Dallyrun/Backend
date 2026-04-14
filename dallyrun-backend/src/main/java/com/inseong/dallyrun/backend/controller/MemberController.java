package com.inseong.dallyrun.backend.controller;

import com.inseong.dallyrun.backend.dto.request.MemberUpdateRequest;
import com.inseong.dallyrun.backend.dto.response.ApiResponse;
import com.inseong.dallyrun.backend.dto.response.MemberResponse;
import com.inseong.dallyrun.backend.security.CustomUserDetails;
import com.inseong.dallyrun.backend.service.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Member", description = "회원 API")
@RestController
@RequestMapping("/api/members")
public class MemberController {

    private final MemberService memberService;

    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    @Operation(summary = "내 프로필 조회")
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<MemberResponse>> getProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        MemberResponse response = memberService.getProfile(userDetails.getMemberId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "프로필 수정")
    @PatchMapping("/me")
    public ResponseEntity<ApiResponse<MemberResponse>> updateProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody MemberUpdateRequest request) {
        MemberResponse response = memberService.updateProfile(userDetails.getMemberId(), request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "계정 삭제")
    @DeleteMapping("/me")
    public ResponseEntity<ApiResponse<Void>> deleteMember(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        memberService.deleteMember(userDetails.getMemberId());
        return ResponseEntity.ok(ApiResponse.success());
    }
}
