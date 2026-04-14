package com.inseong.dallyrun.backend.controller;

import com.inseong.dallyrun.backend.dto.request.OAuthLoginRequest;
import com.inseong.dallyrun.backend.dto.request.RefreshTokenRequest;
import com.inseong.dallyrun.backend.dto.response.ApiResponse;
import com.inseong.dallyrun.backend.dto.response.TokenResponse;
import com.inseong.dallyrun.backend.entity.enums.OAuthProvider;
import com.inseong.dallyrun.backend.security.CustomUserDetails;
import com.inseong.dallyrun.backend.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Auth", description = "인증 API — OAuth2 소셜 로그인, 토큰 갱신, 로그아웃. 로그인/갱신은 인증 불필요.")
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @Operation(
            summary = "카카오 로그인",
            description = "앱에서 카카오 SDK로 받은 인증 코드(authCode)를 전달하면, "
                    + "백엔드가 카카오 서버에서 유저 정보를 조회하여 회원 가입/로그인 처리 후 JWT를 발급합니다. "
                    + "신규 유저는 자동으로 회원 생성됩니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "로그인 성공, JWT 토큰 반환"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "유효하지 않은 인증 코드")
    })
    @PostMapping("/oauth/kakao")
    public ResponseEntity<ApiResponse<TokenResponse>> kakaoLogin(
            @Valid @RequestBody OAuthLoginRequest request) {
        TokenResponse response = authService.oauthLogin(OAuthProvider.KAKAO, request.authCode());
        return ResponseEntity.ok(ApiResponse.of(response));
    }

    @Operation(
            summary = "구글 로그인",
            description = "앱에서 구글 SDK로 받은 인증 코드(authCode)를 전달하면, "
                    + "백엔드가 구글 서버에서 유저 정보를 조회하여 회원 가입/로그인 처리 후 JWT를 발급합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "로그인 성공, JWT 토큰 반환"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "유효하지 않은 인증 코드")
    })
    @PostMapping("/oauth/google")
    public ResponseEntity<ApiResponse<TokenResponse>> googleLogin(
            @Valid @RequestBody OAuthLoginRequest request) {
        TokenResponse response = authService.oauthLogin(OAuthProvider.GOOGLE, request.authCode());
        return ResponseEntity.ok(ApiResponse.of(response));
    }

    @Operation(
            summary = "토큰 갱신",
            description = "Access Token이 만료되었을 때 Refresh Token으로 새로운 토큰 쌍을 발급받습니다. "
                    + "기존 Refresh Token은 무효화되고 새 Refresh Token이 발급됩니다 (Rotation). "
                    + "Refresh Token이 유효하지 않으면 401을 반환하므로 재로그인이 필요합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "토큰 갱신 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "유효하지 않거나 만료된 Refresh Token")
    })
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<TokenResponse>> refreshToken(
            @Valid @RequestBody RefreshTokenRequest request) {
        TokenResponse response = authService.refreshToken(request.refreshToken());
        return ResponseEntity.ok(ApiResponse.of(response));
    }

    @Operation(
            summary = "로그아웃",
            description = "서버에 저장된 Refresh Token을 삭제합니다. "
                    + "앱에서도 로컬에 저장된 Access Token과 Refresh Token을 함께 삭제해야 합니다."
    )
    @DeleteMapping("/logout")
    public ResponseEntity<Void> logout(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        authService.logout(userDetails.getMemberId());
        return ResponseEntity.ok().build();
    }
}
