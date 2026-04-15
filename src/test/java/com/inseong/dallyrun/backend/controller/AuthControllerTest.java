package com.inseong.dallyrun.backend.controller;

import com.inseong.dallyrun.backend.config.SecurityConfig;
import com.inseong.dallyrun.backend.dto.response.TokenResponse;
import com.inseong.dallyrun.backend.entity.enums.OAuthProvider;
import com.inseong.dallyrun.backend.security.CustomUserDetailsService;
import com.inseong.dallyrun.backend.security.JwtAuthenticationFilter;
import com.inseong.dallyrun.backend.security.JwtTokenProvider;
import com.inseong.dallyrun.backend.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class})
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @Test
    void kakaoLogin_success() throws Exception {
        TokenResponse tokenResponse = new TokenResponse("access-token", "refresh-token");
        when(authService.oauthLogin(eq(OAuthProvider.KAKAO), anyString()))
                .thenReturn(tokenResponse);

        mockMvc.perform(post("/api/auth/oauth/kakao")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"authCode\":\"test-auth-code\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").value("access-token"))
                .andExpect(jsonPath("$.data.refreshToken").value("refresh-token"));
    }

    @Test
    void refreshToken_success() throws Exception {
        TokenResponse tokenResponse = new TokenResponse("new-access", "new-refresh");
        when(authService.refreshToken("old-refresh")).thenReturn(tokenResponse);

        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"old-refresh\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").value("new-access"));
    }

    @Test
    void kakaoLogin_missingAuthCode_returns400() throws Exception {
        mockMvc.perform(post("/api/auth/oauth/kakao")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"authCode\":\"\"}"))
                .andExpect(status().isBadRequest());
    }
}
