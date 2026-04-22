package com.inseong.dallyrun.backend.controller;

import com.inseong.dallyrun.backend.dto.request.MemberUpdateRequest;
import com.inseong.dallyrun.backend.dto.response.ApiResponse;
import com.inseong.dallyrun.backend.dto.response.MemberResponse;
import com.inseong.dallyrun.backend.security.CustomUserDetails;
import com.inseong.dallyrun.backend.service.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "Member", description = "회원 API — 프로필 조회/수정, 계정 삭제")
@RestController
@RequestMapping("/api/members")
public class MemberController {

    private final MemberService memberService;

    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    @Operation(
            summary = "내 프로필 조회",
            description = "로그인한 사용자의 프로필 정보를 조회합니다. "
                    + "닉네임, 이메일, 프로필 이미지, 소셜 로그인 제공자 정보를 반환합니다."
    )
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<MemberResponse>> getProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        MemberResponse response = memberService.getProfile(userDetails.getMemberId());
        return ResponseEntity.ok(ApiResponse.of(response));
    }

    @Operation(
            summary = "프로필 수정",
            description = "닉네임, 프로필 이미지 URL을 수정합니다. "
                    + "변경할 필드만 보내면 되며, null인 필드는 기존 값을 유지합니다."
    )
    @PatchMapping("/me")
    public ResponseEntity<ApiResponse<MemberResponse>> updateProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody MemberUpdateRequest request) {
        MemberResponse response = memberService.updateProfile(userDetails.getMemberId(), request);
        return ResponseEntity.ok(ApiResponse.of(response));
    }

    @Operation(
            summary = "프로필 이미지 업로드",
            description = "프로필 이미지를 업로드하여 교체합니다. multipart/form-data 의 `file` 파트로 전송하며, "
                    + "허용 MIME은 image/jpeg, image/png, image/webp 입니다. 최대 크기는 5MB."
    )
    @PostMapping(value = "/me/profile-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<MemberResponse>> uploadProfileImage(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestPart("file") MultipartFile file) {
        MemberResponse response = memberService.uploadProfileImage(userDetails.getMemberId(), file);
        return ResponseEntity.ok(ApiResponse.of(response));
    }

    @Operation(
            summary = "계정 삭제 (탈퇴)",
            description = "회원 계정을 삭제합니다. 관련 러닝 기록, 목표, 뱃지 데이터도 함께 삭제됩니다. "
                    + "이 작업은 되돌릴 수 없습니다."
    )
    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteMember(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        memberService.deleteMember(userDetails.getMemberId());
        return ResponseEntity.ok().build();
    }
}
