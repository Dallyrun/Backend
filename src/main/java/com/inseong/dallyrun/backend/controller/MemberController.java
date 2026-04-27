package com.inseong.dallyrun.backend.controller;

import com.inseong.dallyrun.backend.dto.request.MemberDeleteRequest;
import com.inseong.dallyrun.backend.dto.request.MemberUpdateRequest;
import com.inseong.dallyrun.backend.dto.response.ApiResponse;
import com.inseong.dallyrun.backend.dto.response.MemberResponse;
import com.inseong.dallyrun.backend.security.CustomUserDetails;
import com.inseong.dallyrun.backend.service.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
            description = "회원 계정을 soft delete 합니다. 계정 탈취 방지를 위해 현재 비밀번호를 본문으로 한 번 더 받아 검증합니다. "
                    + "탈퇴 후에는 모든 조회·로그인이 차단되며, 보관 중인 refresh token 도 즉시 폐기됩니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "탈퇴 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "비밀번호 누락 등 입력 오류"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "비밀번호 불일치"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "회원을 찾을 수 없음 (이미 탈퇴 등)")
    })
    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteMember(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody MemberDeleteRequest request) {
        memberService.deleteMember(userDetails.getMemberId(), request.password());
        return ResponseEntity.ok().build();
    }
}
