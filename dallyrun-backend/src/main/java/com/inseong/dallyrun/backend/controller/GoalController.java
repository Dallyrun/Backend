package com.inseong.dallyrun.backend.controller;

import com.inseong.dallyrun.backend.dto.request.GoalCreateRequest;
import com.inseong.dallyrun.backend.dto.request.GoalUpdateRequest;
import com.inseong.dallyrun.backend.dto.response.ApiResponse;
import com.inseong.dallyrun.backend.dto.response.GoalProgressResponse;
import com.inseong.dallyrun.backend.dto.response.GoalResponse;
import com.inseong.dallyrun.backend.security.CustomUserDetails;
import com.inseong.dallyrun.backend.service.GoalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Goal", description = "목표 API")
@RestController
@RequestMapping("/api/goals")
public class GoalController {

    private final GoalService goalService;

    public GoalController(GoalService goalService) {
        this.goalService = goalService;
    }

    @Operation(summary = "목표 생성")
    @PostMapping
    public ResponseEntity<ApiResponse<GoalResponse>> createGoal(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody GoalCreateRequest request) {
        GoalResponse response = goalService.createGoal(userDetails.getMemberId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @Operation(summary = "활성 목표 목록")
    @GetMapping
    public ResponseEntity<ApiResponse<List<GoalResponse>>> getActiveGoals(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        List<GoalResponse> response = goalService.getActiveGoals(userDetails.getMemberId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "목표 상세 + 달성률")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<GoalProgressResponse>> getGoalProgress(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id) {
        GoalProgressResponse response = goalService.getGoalProgress(userDetails.getMemberId(), id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "목표 수정")
    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<GoalResponse>> updateGoal(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id,
            @RequestBody GoalUpdateRequest request) {
        GoalResponse response = goalService.updateGoal(userDetails.getMemberId(), id, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "목표 삭제")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteGoal(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id) {
        goalService.deleteGoal(userDetails.getMemberId(), id);
        return ResponseEntity.ok(ApiResponse.success());
    }
}
