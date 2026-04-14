package com.inseong.dallyrun.backend.service;

import com.inseong.dallyrun.backend.dto.request.GoalCreateRequest;
import com.inseong.dallyrun.backend.dto.request.GoalUpdateRequest;
import com.inseong.dallyrun.backend.dto.response.GoalProgressResponse;
import com.inseong.dallyrun.backend.dto.response.GoalResponse;

import java.util.List;

public interface GoalService {

    GoalResponse createGoal(Long memberId, GoalCreateRequest request);

    List<GoalResponse> getActiveGoals(Long memberId);

    GoalProgressResponse getGoalProgress(Long memberId, Long goalId);

    GoalResponse updateGoal(Long memberId, Long goalId, GoalUpdateRequest request);

    void deleteGoal(Long memberId, Long goalId);
}
