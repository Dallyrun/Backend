package com.inseong.dallyrun.backend.service;

import com.inseong.dallyrun.backend.dto.request.GoalCreateRequest;
import com.inseong.dallyrun.backend.dto.response.GoalProgressResponse;
import com.inseong.dallyrun.backend.dto.response.GoalResponse;
import com.inseong.dallyrun.backend.entity.Goal;
import com.inseong.dallyrun.backend.entity.Member;
import com.inseong.dallyrun.backend.entity.enums.GoalType;
import com.inseong.dallyrun.backend.entity.enums.MetricType;
import com.inseong.dallyrun.backend.entity.enums.OAuthProvider;
import com.inseong.dallyrun.backend.exception.BusinessException;
import com.inseong.dallyrun.backend.repository.GoalRepository;
import com.inseong.dallyrun.backend.repository.MemberRepository;
import com.inseong.dallyrun.backend.repository.RunningSessionRepository;
import com.inseong.dallyrun.backend.support.TestEntityHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GoalServiceTest {

    @Mock
    private GoalRepository goalRepository;
    @Mock
    private MemberRepository memberRepository;
    @Mock
    private RunningSessionRepository runningSessionRepository;

    @InjectMocks
    private GoalServiceImpl goalService;

    private Member testMember;

    @BeforeEach
    void setUp() {
        testMember = new Member("test@test.com", "테스터", null, OAuthProvider.KAKAO, "kakao-1");
        TestEntityHelper.setId(testMember, 1L);
    }

    @Test
    void createGoal_success() {
        when(memberRepository.findById(1L)).thenReturn(Optional.of(testMember));
        when(goalRepository.save(any(Goal.class))).thenAnswer(inv -> inv.getArgument(0));

        GoalCreateRequest request = new GoalCreateRequest(
                GoalType.WEEKLY, MetricType.DISTANCE, 10000.0,
                LocalDate.of(2026, 4, 13), LocalDate.of(2026, 4, 19));

        GoalResponse response = goalService.createGoal(1L, request);

        assertNotNull(response);
        assertEquals(GoalType.WEEKLY, response.goalType());
        assertEquals(MetricType.DISTANCE, response.metricType());
        assertEquals(10000.0, response.targetValue());
    }

    @Test
    void getActiveGoals_returnsList() {
        Goal goal = new Goal(testMember, GoalType.WEEKLY, MetricType.DISTANCE, 10000.0,
                LocalDate.of(2026, 4, 13), LocalDate.of(2026, 4, 19));
        when(goalRepository.findByMemberIdAndActiveTrue(1L)).thenReturn(List.of(goal));

        List<GoalResponse> goals = goalService.getActiveGoals(1L);

        assertEquals(1, goals.size());
    }

    @Test
    void getGoalProgress_distanceMetric() {
        Goal goal = new Goal(testMember, GoalType.WEEKLY, MetricType.DISTANCE, 10000.0,
                LocalDate.of(2026, 4, 13), LocalDate.of(2026, 4, 19));
        TestEntityHelper.setId(goal, 1L);
        when(goalRepository.findById(1L)).thenReturn(Optional.of(goal));
        when(runningSessionRepository.sumDistanceBetween(eq(1L), any(), any())).thenReturn(5000.0);

        GoalProgressResponse response = goalService.getGoalProgress(1L, 1L);

        assertEquals(5000.0, response.currentValue());
        assertEquals(50.0, response.progressRate());
    }

    @Test
    void getGoalProgress_countMetric() {
        Goal goal = new Goal(testMember, GoalType.MONTHLY, MetricType.COUNT, 20.0,
                LocalDate.of(2026, 4, 1), LocalDate.of(2026, 4, 30));
        TestEntityHelper.setId(goal, 1L);
        when(goalRepository.findById(1L)).thenReturn(Optional.of(goal));
        when(runningSessionRepository.countCompletedBetween(eq(1L), any(), any())).thenReturn(10L);

        GoalProgressResponse response = goalService.getGoalProgress(1L, 1L);

        assertEquals(10.0, response.currentValue());
        assertEquals(50.0, response.progressRate());
    }

    @Test
    void getGoalProgress_exceeds100Percent_cappedAt100() {
        Goal goal = new Goal(testMember, GoalType.WEEKLY, MetricType.DISTANCE, 5000.0,
                LocalDate.of(2026, 4, 13), LocalDate.of(2026, 4, 19));
        TestEntityHelper.setId(goal, 1L);
        when(goalRepository.findById(1L)).thenReturn(Optional.of(goal));
        when(runningSessionRepository.sumDistanceBetween(eq(1L), any(), any())).thenReturn(10000.0);

        GoalProgressResponse response = goalService.getGoalProgress(1L, 1L);

        assertEquals(100.0, response.progressRate());
    }

    @Test
    void deleteGoal_deactivatesGoal() {
        Goal goal = new Goal(testMember, GoalType.WEEKLY, MetricType.DISTANCE, 10000.0,
                LocalDate.of(2026, 4, 13), LocalDate.of(2026, 4, 19));
        TestEntityHelper.setId(goal, 1L);
        when(goalRepository.findById(1L)).thenReturn(Optional.of(goal));

        goalService.deleteGoal(1L, 1L);

        assertFalse(goal.isActive());
    }

    @Test
    void getGoalProgress_goalNotFound_throwsException() {
        when(goalRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(BusinessException.class, () -> goalService.getGoalProgress(1L, 99L));
    }
}
