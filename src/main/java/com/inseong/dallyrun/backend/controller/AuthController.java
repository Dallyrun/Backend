package com.inseong.dallyrun.backend.controller;

import com.inseong.dallyrun.backend.dto.request.LoginRequest;
import com.inseong.dallyrun.backend.dto.request.RefreshTokenRequest;
import com.inseong.dallyrun.backend.dto.request.SignupRequest;
import com.inseong.dallyrun.backend.dto.response.ApiResponse;
import com.inseong.dallyrun.backend.dto.response.TokenResponse;
import com.inseong.dallyrun.backend.security.CustomUserDetails;
import com.inseong.dallyrun.backend.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Auth", description = "인증 API — 이메일/비밀번호 회원가입·로그인, 토큰 갱신, 로그아웃. 가입/로그인/갱신은 인증 불필요.")
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @Operation(
            summary = "회원가입",
            description = "이메일·비밀번호·닉네임으로 신규 회원을 생성하고 즉시 JWT 토큰을 발급합니다. "
                    + "비밀번호는 BCrypt로 해시되어 저장되며, 이메일이 이미 존재하면 409를 반환합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "가입 성공, JWT 토큰 반환"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "유효하지 않은 입력"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "이미 사용 중인 이메일")
    })
    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<TokenResponse>> signup(
            @Valid @RequestBody SignupRequest request) {
        TokenResponse response = authService.signup(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.of(response));
    }

    @Operation(
            summary = "로그인",
            description = "이메일·비밀번호로 인증 후 JWT 토큰을 발급받습니다. "
                    + "회원이 없거나 비밀번호가 틀리면 동일하게 401을 반환합니다 (사용자 열거 방지)."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "로그인 성공, JWT 토큰 반환"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "이메일 또는 비밀번호 불일치")
    })
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<TokenResponse>> login(
            @Valid @RequestBody LoginRequest request) {
        TokenResponse response = authService.login(request);
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
