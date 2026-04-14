package com.inseong.dallyrun.backend.service;

import com.inseong.dallyrun.backend.dto.request.GoalCreateRequest;
import com.inseong.dallyrun.backend.dto.request.GoalUpdateRequest;
import com.inseong.dallyrun.backend.dto.response.GoalProgressResponse;
import com.inseong.dallyrun.backend.dto.response.GoalResponse;
import com.inseong.dallyrun.backend.entity.Goal;
import com.inseong.dallyrun.backend.entity.Member;
import com.inseong.dallyrun.backend.entity.enums.MetricType;
import com.inseong.dallyrun.backend.exception.BusinessException;
import com.inseong.dallyrun.backend.exception.ErrorCode;
import com.inseong.dallyrun.backend.repository.GoalRepository;
import com.inseong.dallyrun.backend.repository.MemberRepository;
import com.inseong.dallyrun.backend.repository.RunningSessionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
@Transactional
public class GoalServiceImpl implements GoalService {

    private final GoalRepository goalRepository;
    private final MemberRepository memberRepository;
    private final RunningSessionRepository runningSessionRepository;

    public GoalServiceImpl(GoalRepository goalRepository,
                           MemberRepository memberRepository,
                           RunningSessionRepository runningSessionRepository) {
        this.goalRepository = goalRepository;
        this.memberRepository = memberRepository;
        this.runningSessionRepository = runningSessionRepository;
    }

    @Override
    public GoalResponse createGoal(Long memberId, GoalCreateRequest request) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        Goal goal = new Goal(member, request.goalType(), request.metricType(),
                request.targetValue(), request.startDate(), request.endDate());

        return GoalResponse.from(goalRepository.save(goal));
    }

    @Override
    @Transactional(readOnly = true)
    public List<GoalResponse> getActiveGoals(Long memberId) {
        return goalRepository.findByMemberIdAndActiveTrue(memberId).stream()
                .map(GoalResponse::from)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public GoalProgressResponse getGoalProgress(Long memberId, Long goalId) {
        Goal goal = getOwnedGoal(memberId, goalId);
        double currentValue = calculateCurrentValue(memberId, goal);
        return GoalProgressResponse.of(goal, currentValue);
    }

    @Override
    public GoalResponse updateGoal(Long memberId, Long goalId, GoalUpdateRequest request) {
        Goal goal = getOwnedGoal(memberId, goalId);
        goal.update(request.goalType(), request.metricType(),
                request.targetValue(), request.startDate(), request.endDate());
        return GoalResponse.from(goal);
    }

    @Override
    public void deleteGoal(Long memberId, Long goalId) {
        Goal goal = getOwnedGoal(memberId, goalId);
        goal.deactivate();
    }

    private Goal getOwnedGoal(Long memberId, Long goalId) {
        Goal goal = goalRepository.findById(goalId)
                .orElseThrow(() -> new BusinessException(ErrorCode.GOAL_NOT_FOUND));
        if (!goal.getMember().getId().equals(memberId)) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }
        return goal;
    }

    private double calculateCurrentValue(Long memberId, Goal goal) {
        LocalDateTime start = goal.getStartDate().atStartOfDay();
        LocalDateTime end = goal.getEndDate().atTime(LocalTime.MAX);

        return switch (goal.getMetricType()) {
            case DISTANCE -> runningSessionRepository.sumDistanceBetween(memberId, start, end);
            case TIME -> runningSessionRepository.sumDurationBetween(memberId, start, end);
            case COUNT -> runningSessionRepository.countCompletedBetween(memberId, start, end);
        };
    }
}
