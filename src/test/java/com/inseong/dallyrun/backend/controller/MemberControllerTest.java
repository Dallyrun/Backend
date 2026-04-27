package com.inseong.dallyrun.backend.controller;

import com.inseong.dallyrun.backend.config.SecurityConfig;
import com.inseong.dallyrun.backend.dto.response.MemberResponse;
import com.inseong.dallyrun.backend.entity.AgeBracket;
import com.inseong.dallyrun.backend.entity.Gender;
import com.inseong.dallyrun.backend.exception.BusinessException;
import com.inseong.dallyrun.backend.exception.ErrorCode;
import com.inseong.dallyrun.backend.security.CustomUserDetails;
import com.inseong.dallyrun.backend.security.CustomUserDetailsService;
import com.inseong.dallyrun.backend.security.JwtAuthenticationFilter;
import com.inseong.dallyrun.backend.security.JwtTokenProvider;
import com.inseong.dallyrun.backend.service.MemberService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import org.springframework.mock.web.MockMultipartFile;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MemberController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class})
class MemberControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MemberService memberService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    private final CustomUserDetails testUser = new CustomUserDetails(1L, "test@test.com");

    @Test
    void getProfile_success() throws Exception {
        MemberResponse response = new MemberResponse(1L, "test@test.com", "테스터",
                "https://img.test/p.jpg", AgeBracket.THIRTIES, Gender.MALE);
        when(memberService.getProfile(1L)).thenReturn(response);

        mockMvc.perform(get("/api/members/me")
                        .with(user(testUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.nickname").value("테스터"));
    }

    @Test
    void updateProfile_success() throws Exception {
        MemberResponse response = new MemberResponse(1L, "test@test.com", "새닉",
                "https://img.test/p.jpg", AgeBracket.THIRTIES, Gender.MALE);
        when(memberService.updateProfile(eq(1L), any())).thenReturn(response);

        mockMvc.perform(patch("/api/members/me")
                        .with(user(testUser))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nickname\":\"새닉\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.nickname").value("새닉"));
    }

    @Test
    void deleteMember_success() throws Exception {
        mockMvc.perform(delete("/api/members/me")
                        .with(user(testUser))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"password\":\"rawPassword\"}"))
                .andExpect(status().isOk());

        verify(memberService).deleteMember(1L, "rawPassword");
    }

    @Test
    void deleteMember_blankPassword_returns400() throws Exception {
        mockMvc.perform(delete("/api/members/me")
                        .with(user(testUser))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"password\":\"\"}"))
                .andExpect(status().isBadRequest());

        verify(memberService, never()).deleteMember(any(), any());
    }

    @Test
    void deleteMember_wrongPassword_returns401() throws Exception {
        doThrow(new BusinessException(ErrorCode.INVALID_CREDENTIALS))
                .when(memberService).deleteMember(eq(1L), eq("wrongPassword"));

        mockMvc.perform(delete("/api/members/me")
                        .with(user(testUser))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"password\":\"wrongPassword\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getProfile_noAuth_returns401() throws Exception {
        mockMvc.perform(get("/api/members/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void updateProfile_invalidNickname_returns400() throws Exception {
        String longNickname = "a".repeat(51);

        mockMvc.perform(patch("/api/members/me")
                        .with(user(testUser))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nickname\":\"" + longNickname + "\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void uploadProfileImage_success() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "avatar.jpg", "image/jpeg", new byte[]{1, 2, 3});
        MemberResponse response = new MemberResponse(
                1L, "test@test.com", "테스터",
                "http://localhost:8080/uploads/profile-images/new.jpg",
                AgeBracket.THIRTIES, Gender.MALE);
        when(memberService.uploadProfileImage(eq(1L), any())).thenReturn(response);

        mockMvc.perform(multipart("/api/members/me/profile-image")
                        .file(file)
                        .with(user(testUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.profileImageUrl")
                        .value("http://localhost:8080/uploads/profile-images/new.jpg"));
    }

    @Test
    void uploadProfileImage_noAuth_returns401() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "avatar.jpg", "image/jpeg", new byte[]{1});

        mockMvc.perform(multipart("/api/members/me/profile-image")
                        .file(file))
                .andExpect(status().isUnauthorized());
    }
}
