package com.inseong.dallyrun.backend.controller;

import com.inseong.dallyrun.backend.config.SecurityConfig;
import com.inseong.dallyrun.backend.dto.response.RunningSessionResponse;
import com.inseong.dallyrun.backend.dto.response.RunningSessionStartResponse;
import com.inseong.dallyrun.backend.entity.enums.SessionStatus;
import com.inseong.dallyrun.backend.security.CustomUserDetails;
import com.inseong.dallyrun.backend.security.CustomUserDetailsService;
import com.inseong.dallyrun.backend.security.JwtAuthenticationFilter;
import com.inseong.dallyrun.backend.security.JwtTokenProvider;
import com.inseong.dallyrun.backend.service.RunningSessionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RunningSessionController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class})
class RunningSessionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RunningSessionService runningSessionService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    private final CustomUserDetails testUser = new CustomUserDetails(1L, "test@test.com");

    @Test
    void startSession_returns201() throws Exception {
        RunningSessionStartResponse response = new RunningSessionStartResponse(1L, LocalDateTime.now());
        when(runningSessionService.startSession(1L)).thenReturn(response);

        mockMvc.perform(post("/api/running-sessions")
                        .with(user(testUser)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1));
    }

    @Test
    void getHistory_returnsList() throws Exception {
        RunningSessionResponse session = new RunningSessionResponse(
                1L, SessionStatus.COMPLETED, LocalDateTime.now(), LocalDateTime.now(),
                5000.0, 1800L, 6.0, null, LocalDateTime.now());
        Page<RunningSessionResponse> page = new PageImpl<>(List.of(session));

        when(runningSessionService.getHistory(eq(1L), any())).thenReturn(page);

        mockMvc.perform(get("/api/running-sessions")
                        .with(user(testUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].id").value(1));
    }

    @Test
    void endSession_success() throws Exception {
        RunningSessionResponse response = new RunningSessionResponse(
                1L, SessionStatus.COMPLETED, LocalDateTime.now(), LocalDateTime.now(),
                5000.0, 1800L, 6.0, null, LocalDateTime.now());
        when(runningSessionService.endSession(eq(1L), eq(1L), any())).thenReturn(response);

        String body = """
                {
                    "gpsPoints": [
                        {"latitude": 37.5666, "longitude": 126.9784, "altitude": 10.0, "sequenceIndex": 0},
                        {"latitude": 37.5700, "longitude": 126.9800, "altitude": 10.0, "sequenceIndex": 1}
                    ]
                }
                """;

        mockMvc.perform(patch("/api/running-sessions/1/end")
                        .with(user(testUser))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("COMPLETED"));
    }

    @Test
    void startSession_noAuth_returns401() throws Exception {
        mockMvc.perform(post("/api/running-sessions"))
                .andExpect(status().isUnauthorized());
    }
}
