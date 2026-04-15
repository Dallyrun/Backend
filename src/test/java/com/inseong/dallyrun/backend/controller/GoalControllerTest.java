package com.inseong.dallyrun.backend.controller;

import com.inseong.dallyrun.backend.config.SecurityConfig;
import com.inseong.dallyrun.backend.dto.response.GoalProgressResponse;
import com.inseong.dallyrun.backend.dto.response.GoalResponse;
import com.inseong.dallyrun.backend.entity.enums.GoalType;
import com.inseong.dallyrun.backend.entity.enums.MetricType;
import com.inseong.dallyrun.backend.security.CustomUserDetails;
import com.inseong.dallyrun.backend.security.CustomUserDetailsService;
import com.inseong.dallyrun.backend.security.JwtAuthenticationFilter;
import com.inseong.dallyrun.backend.security.JwtTokenProvider;
import com.inseong.dallyrun.backend.service.GoalService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(GoalController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class})
class GoalControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GoalService goalService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    private final CustomUserDetails testUser = new CustomUserDetails(1L, "test@test.com");

    @Test
    void createGoal_success() throws Exception {
        GoalResponse response = new GoalResponse(1L, GoalType.WEEKLY, MetricType.DISTANCE,
                10000.0, LocalDate.of(2026, 4, 13), LocalDate.of(2026, 4, 19), true);
        when(goalService.createGoal(eq(1L), any())).thenReturn(response);

        String body = """
                {
                    "goalType": "WEEKLY",
                    "metricType": "DISTANCE",
                    "targetValue": 10000.0,
                    "startDate": "2026-04-13",
                    "endDate": "2026-04-19"
                }
                """;

        mockMvc.perform(post("/api/goals")
                        .with(user(testUser))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.goalType").value("WEEKLY"));
    }

    @Test
    void getActiveGoals_success() throws Exception {
        GoalResponse response = new GoalResponse(1L, GoalType.WEEKLY, MetricType.DISTANCE,
                10000.0, LocalDate.of(2026, 4, 13), LocalDate.of(2026, 4, 19), true);
        when(goalService.getActiveGoals(1L)).thenReturn(List.of(response));

        mockMvc.perform(get("/api/goals")
                        .with(user(testUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].goalType").value("WEEKLY"));
    }

    @Test
    void getGoalProgress_success() throws Exception {
        GoalProgressResponse response = new GoalProgressResponse(1L, GoalType.WEEKLY,
                MetricType.DISTANCE, 10000.0, 5000.0, 50.0,
                LocalDate.of(2026, 4, 13), LocalDate.of(2026, 4, 19), true);
        when(goalService.getGoalProgress(1L, 1L)).thenReturn(response);

        mockMvc.perform(get("/api/goals/1")
                        .with(user(testUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.progressRate").value(50.0));
    }

    @Test
    void updateGoal_success() throws Exception {
        GoalResponse response = new GoalResponse(1L, GoalType.WEEKLY, MetricType.DISTANCE,
                20000.0, LocalDate.of(2026, 4, 13), LocalDate.of(2026, 4, 19), true);
        when(goalService.updateGoal(eq(1L), eq(1L), any())).thenReturn(response);

        mockMvc.perform(patch("/api/goals/1")
                        .with(user(testUser))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"targetValue\": 20000.0}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.targetValue").value(20000.0));
    }

    @Test
    void deleteGoal_success() throws Exception {
        mockMvc.perform(delete("/api/goals/1")
                        .with(user(testUser)))
                .andExpect(status().isOk());

        verify(goalService).deleteGoal(1L, 1L);
    }

    @Test
    void createGoal_noAuth_returns401() throws Exception {
        mockMvc.perform(post("/api/goals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"goalType\":\"WEEKLY\",\"metricType\":\"DISTANCE\",\"targetValue\":10000}"))
                .andExpect(status().isUnauthorized());
    }
}
