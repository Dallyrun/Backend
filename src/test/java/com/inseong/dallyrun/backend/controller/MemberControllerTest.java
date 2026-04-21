package com.inseong.dallyrun.backend.controller;

import com.inseong.dallyrun.backend.config.SecurityConfig;
import com.inseong.dallyrun.backend.dto.response.MemberResponse;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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
        MemberResponse response = new MemberResponse(1L, "test@test.com", "테스터", null);
        when(memberService.getProfile(1L)).thenReturn(response);

        mockMvc.perform(get("/api/members/me")
                        .with(user(testUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.nickname").value("테스터"));
    }

    @Test
    void updateProfile_success() throws Exception {
        MemberResponse response = new MemberResponse(1L, "test@test.com", "새닉", null);
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
                        .with(user(testUser)))
                .andExpect(status().isOk());

        verify(memberService).deleteMember(1L);
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
}
