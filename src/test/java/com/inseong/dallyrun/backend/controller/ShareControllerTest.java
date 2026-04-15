package com.inseong.dallyrun.backend.controller;

import com.inseong.dallyrun.backend.config.SecurityConfig;
import com.inseong.dallyrun.backend.dto.response.ShareDataResponse;
import com.inseong.dallyrun.backend.dto.response.ShareLinkResponse;
import com.inseong.dallyrun.backend.security.CustomUserDetails;
import com.inseong.dallyrun.backend.security.CustomUserDetailsService;
import com.inseong.dallyrun.backend.security.JwtAuthenticationFilter;
import com.inseong.dallyrun.backend.security.JwtTokenProvider;
import com.inseong.dallyrun.backend.service.ShareService;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ShareController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class})
class ShareControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ShareService shareService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    private final CustomUserDetails testUser = new CustomUserDetails(1L, "test@test.com");

    @Test
    void getShareData_success() throws Exception {
        ShareDataResponse response = new ShareDataResponse("테스터",
                LocalDateTime.now(), LocalDateTime.now(), 5000.0, 1800L, 6.0, List.of());
        when(shareService.getShareData(1L, 1L)).thenReturn(response);

        mockMvc.perform(get("/api/running-sessions/1/share-data")
                        .with(user(testUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.nickname").value("테스터"));
    }

    @Test
    void createShareLink_success() throws Exception {
        ShareLinkResponse response = new ShareLinkResponse("abc123def456ghij", "/api/shares/abc123def456ghij");
        when(shareService.createShareLink(1L, 1L)).thenReturn(response);

        mockMvc.perform(post("/api/running-sessions/1/share-link")
                        .with(user(testUser)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.shareCode").value("abc123def456ghij"));
    }

    @Test
    void getSharedData_success() throws Exception {
        ShareDataResponse response = new ShareDataResponse("테스터",
                LocalDateTime.now(), LocalDateTime.now(), 5000.0, 1800L, 6.0, List.of());
        when(shareService.getSharedData("abc123")).thenReturn(response);

        mockMvc.perform(get("/api/shares/abc123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.nickname").value("테스터"));
    }

    @Test
    void getShareData_noAuth_returns401() throws Exception {
        mockMvc.perform(get("/api/running-sessions/1/share-data"))
                .andExpect(status().isUnauthorized());
    }
}
