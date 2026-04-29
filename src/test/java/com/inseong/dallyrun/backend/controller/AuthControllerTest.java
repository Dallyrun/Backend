package com.inseong.dallyrun.backend.controller;

import com.inseong.dallyrun.backend.config.SecurityConfig;
import com.inseong.dallyrun.backend.dto.response.TokenResponse;
import com.inseong.dallyrun.backend.exception.BusinessException;
import com.inseong.dallyrun.backend.exception.ErrorCode;
import com.inseong.dallyrun.backend.security.CustomUserDetails;
import com.inseong.dallyrun.backend.security.CustomUserDetailsService;
import com.inseong.dallyrun.backend.security.JwtAuthenticationFilter;
import com.inseong.dallyrun.backend.security.JwtTokenProvider;
import com.inseong.dallyrun.backend.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
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

    private static final String VALID_DATA_JSON =
            "{\"email\":\"new@test.com\",\"password\":\"Password123!\",\"nickname\":\"러너123\",\"ageBracket\":20,\"gender\":\"MALE\"}";

    private MockMultipartFile dataPart(String json) {
        return new MockMultipartFile("data", "", MediaType.APPLICATION_JSON_VALUE, json.getBytes());
    }

    private MockMultipartFile imagePart() {
        return new MockMultipartFile("image", "p.jpg", MediaType.IMAGE_JPEG_VALUE, new byte[]{1, 2, 3});
    }

    @Test
    void signup_success_returns201() throws Exception {
        TokenResponse tokenResponse = new TokenResponse("access-token", "refresh-token");
        when(authService.signup(any(), any())).thenReturn(tokenResponse);

        mockMvc.perform(multipart("/api/auth/signup")
                        .file(dataPart(VALID_DATA_JSON))
                        .file(imagePart()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.accessToken").value("access-token"))
                .andExpect(jsonPath("$.data.refreshToken").value("refresh-token"));
    }

    @Test
    void signup_missingImage_returns400() throws Exception {
        mockMvc.perform(multipart("/api/auth/signup")
                        .file(dataPart(VALID_DATA_JSON)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void signup_invalidEmail_returns400() throws Exception {
        String json = "{\"email\":\"not-an-email\",\"password\":\"Password123!\",\"nickname\":\"러너123\",\"ageBracket\":20,\"gender\":\"MALE\"}";
        mockMvc.perform(multipart("/api/auth/signup")
                        .file(dataPart(json))
                        .file(imagePart()))
                .andExpect(status().isBadRequest());
    }

    @Test
    void signup_shortPassword_returns400() throws Exception {
        String json = "{\"email\":\"new@test.com\",\"password\":\"short\",\"nickname\":\"러너123\",\"ageBracket\":20,\"gender\":\"MALE\"}";
        mockMvc.perform(multipart("/api/auth/signup")
                        .file(dataPart(json))
                        .file(imagePart()))
                .andExpect(status().isBadRequest());
    }

    @Test
    void signup_passwordMissingSpecialChar_returns400() throws Exception {
        String json = "{\"email\":\"new@test.com\",\"password\":\"Password123\",\"nickname\":\"러너123\",\"ageBracket\":20,\"gender\":\"MALE\"}";
        mockMvc.perform(multipart("/api/auth/signup")
                        .file(dataPart(json))
                        .file(imagePart()))
                .andExpect(status().isBadRequest());
    }

    @Test
    void signup_nicknameTooShort_returns400() throws Exception {
        String json = "{\"email\":\"new@test.com\",\"password\":\"Password123!\",\"nickname\":\"김\",\"ageBracket\":20,\"gender\":\"MALE\"}";
        mockMvc.perform(multipart("/api/auth/signup")
                        .file(dataPart(json))
                        .file(imagePart()))
                .andExpect(status().isBadRequest());
    }

    @Test
    void signup_nicknameWithSpecialChar_returns400() throws Exception {
        String json = "{\"email\":\"new@test.com\",\"password\":\"Password123!\",\"nickname\":\"러너!\",\"ageBracket\":20,\"gender\":\"MALE\"}";
        mockMvc.perform(multipart("/api/auth/signup")
                        .file(dataPart(json))
                        .file(imagePart()))
                .andExpect(status().isBadRequest());
    }

    @Test
    void signup_missingAgeBracket_returns400() throws Exception {
        String json = "{\"email\":\"new@test.com\",\"password\":\"Password123!\",\"nickname\":\"러너123\",\"gender\":\"MALE\"}";
        mockMvc.perform(multipart("/api/auth/signup")
                        .file(dataPart(json))
                        .file(imagePart()))
                .andExpect(status().isBadRequest());
    }

    @Test
    void signup_invalidAgeBracketValue_returns400() throws Exception {
        String json = "{\"email\":\"new@test.com\",\"password\":\"Password123!\",\"nickname\":\"러너123\",\"ageBracket\":25,\"gender\":\"MALE\"}";
        mockMvc.perform(multipart("/api/auth/signup")
                        .file(dataPart(json))
                        .file(imagePart()))
                .andExpect(status().isBadRequest());
    }

    @Test
    void signup_missingGender_returns400() throws Exception {
        String json = "{\"email\":\"new@test.com\",\"password\":\"Password123!\",\"nickname\":\"러너123\",\"ageBracket\":20}";
        mockMvc.perform(multipart("/api/auth/signup")
                        .file(dataPart(json))
                        .file(imagePart()))
                .andExpect(status().isBadRequest());
    }

    @Test
    void signup_duplicateNickname_returns409() throws Exception {
        when(authService.signup(any(), any()))
                .thenThrow(new BusinessException(ErrorCode.NICKNAME_ALREADY_EXISTS));

        mockMvc.perform(multipart("/api/auth/signup")
                        .file(dataPart(VALID_DATA_JSON))
                        .file(imagePart()))
                .andExpect(status().isConflict());
    }

    @Test
    void login_success() throws Exception {
        TokenResponse tokenResponse = new TokenResponse("access-token", "refresh-token");
        when(authService.login(any())).thenReturn(tokenResponse);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"test@test.com\",\"password\":\"Password123!\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").value("access-token"));
    }

    @Test
    void login_invalidEmail_returns400() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"not-an-email\",\"password\":\"Password123!\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_missingPassword_returns400() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"test@test.com\",\"password\":\"\"}"))
                .andExpect(status().isBadRequest());
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
    void logout_withoutAccessToken_returns401() throws Exception {
        mockMvc.perform(delete("/api/auth/logout"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void logout_withWrongMethodWithoutAccessToken_returns401() throws Exception {
        mockMvc.perform(post("/api/auth/logout"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void logout_withAccessToken_returns200() throws Exception {
        when(jwtTokenProvider.validateToken("access-token")).thenReturn(true);
        when(jwtTokenProvider.getMemberIdFromToken("access-token")).thenReturn(1L);
        when(customUserDetailsService.loadUserById(1L))
                .thenReturn(new CustomUserDetails(1L, "test@test.com"));

        mockMvc.perform(delete("/api/auth/logout")
                        .header("Authorization", "Bearer access-token"))
                .andExpect(status().isOk());

        verify(authService).logout(1L);
    }
}
