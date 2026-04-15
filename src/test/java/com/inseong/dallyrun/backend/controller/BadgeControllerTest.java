package com.inseong.dallyrun.backend.controller;

import com.inseong.dallyrun.backend.config.SecurityConfig;
import com.inseong.dallyrun.backend.dto.response.BadgeResponse;
import com.inseong.dallyrun.backend.dto.response.MemberBadgeResponse;
import com.inseong.dallyrun.backend.entity.enums.ConditionType;
import com.inseong.dallyrun.backend.security.CustomUserDetails;
import com.inseong.dallyrun.backend.security.CustomUserDetailsService;
import com.inseong.dallyrun.backend.security.JwtAuthenticationFilter;
import com.inseong.dallyrun.backend.security.JwtTokenProvider;
import com.inseong.dallyrun.backend.service.BadgeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BadgeController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class})
class BadgeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BadgeService badgeService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    private final CustomUserDetails testUser = new CustomUserDetails(1L, "test@test.com");

    @Test
    void getAllBadges_success() throws Exception {
        BadgeResponse badge = new BadgeResponse(1L, "첫 러닝", "설명", null,
                ConditionType.TOTAL_COUNT, 1.0);
        when(badgeService.getAllBadges()).thenReturn(List.of(badge));

        mockMvc.perform(get("/api/badges")
                        .with(user(testUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].name").value("첫 러닝"));
    }

    @Test
    void getMyBadges_success() throws Exception {
        BadgeResponse badge = new BadgeResponse(1L, "첫 러닝", "설명", null,
                ConditionType.TOTAL_COUNT, 1.0);
        MemberBadgeResponse memberBadge = new MemberBadgeResponse(1L, badge, LocalDateTime.now());
        when(badgeService.getMemberBadges(1L)).thenReturn(List.of(memberBadge));

        mockMvc.perform(get("/api/members/me/badges")
                        .with(user(testUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].badge.name").value("첫 러닝"));
    }

    @Test
    void getMyBadges_noAuth_returns401() throws Exception {
        mockMvc.perform(get("/api/members/me/badges"))
                .andExpect(status().isUnauthorized());
    }
}
