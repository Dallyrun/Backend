package com.inseong.dallyrun.backend.controller;

import com.inseong.dallyrun.backend.dto.request.OAuthLoginRequest;
import com.inseong.dallyrun.backend.dto.request.RefreshTokenRequest;
import com.inseong.dallyrun.backend.dto.response.ApiResponse;
import com.inseong.dallyrun.backend.dto.response.TokenResponse;
import com.inseong.dallyrun.backend.entity.enums.OAuthProvider;
import com.inseong.dallyrun.backend.security.CustomUserDetails;
import com.inseong.dallyrun.backend.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Auth", description = "인증 API")
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @Operation(summary = "카카오 로그인")
    @PostMapping("/oauth/kakao")
    public ResponseEntity<ApiResponse<TokenResponse>> kakaoLogin(
            @Valid @RequestBody OAuthLoginRequest request) {
        TokenResponse response = authService.oauthLogin(OAuthProvider.KAKAO, request.authCode());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "구글 로그인")
    @PostMapping("/oauth/google")
    public ResponseEntity<ApiResponse<TokenResponse>> googleLogin(
            @Valid @RequestBody OAuthLoginRequest request) {
        TokenResponse response = authService.oauthLogin(OAuthProvider.GOOGLE, request.authCode());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "토큰 갱신")
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<TokenResponse>> refreshToken(
            @Valid @RequestBody RefreshTokenRequest request) {
        TokenResponse response = authService.refreshToken(request.refreshToken());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "로그아웃")
    @DeleteMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        authService.logout(userDetails.getMemberId());
        return ResponseEntity.ok(ApiResponse.success());
    }
}
