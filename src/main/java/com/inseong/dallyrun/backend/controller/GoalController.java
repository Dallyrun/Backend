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

@Tag(name = "Goal", description = "목표 API — 주간/월간 러닝 목표 설정 및 달성률 조회. "
        + "거리(미터), 시간(초), 횟수 기준으로 목표를 설정할 수 있습니다.")
@RestController
@RequestMapping("/api/goals")
public class GoalController {

    private final GoalService goalService;

    public GoalController(GoalService goalService) {
        this.goalService = goalService;
    }

    @Operation(
            summary = "목표 생성",
            description = "새로운 러닝 목표를 생성합니다.\n\n"
                    + "- **goalType**: WEEKLY(주간) / MONTHLY(월간)\n"
                    + "- **metricType**: DISTANCE(거리, 미터) / TIME(시간, 초) / COUNT(횟수)\n"
                    + "- **targetValue**: 목표값. 예) DISTANCE=30000 → 30km, COUNT=10 → 10회\n"
                    + "- **startDate / endDate**: 목표 기간 (yyyy-MM-dd)"
    )
    @PostMapping
    public ResponseEntity<ApiResponse<GoalResponse>> createGoal(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody GoalCreateRequest request) {
        GoalResponse response = goalService.createGoal(userDetails.getMemberId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.of(response));
    }

    @Operation(
            summary = "활성 목표 목록",
            description = "현재 활성 상태인 목표 목록을 조회합니다. 삭제된 목표는 포함되지 않습니다."
    )
    @GetMapping
    public ResponseEntity<ApiResponse<List<GoalResponse>>> getActiveGoals(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        List<GoalResponse> response = goalService.getActiveGoals(userDetails.getMemberId());
        return ResponseEntity.ok(ApiResponse.of(response));
    }

    @Operation(
            summary = "목표 상세 + 달성률 조회",
            description = "특정 목표의 상세 정보와 현재 달성률을 조회합니다. "
                    + "서버가 해당 기간의 러닝 기록을 집계하여 달성률(%)을 계산합니다. "
                    + "progressRate는 최대 100.0으로 제한됩니다."
    )
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<GoalProgressResponse>> getGoalProgress(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id) {
        GoalProgressResponse response = goalService.getGoalProgress(userDetails.getMemberId(), id);
        return ResponseEntity.ok(ApiResponse.of(response));
    }

    @Operation(
            summary = "목표 수정",
            description = "목표를 수정합니다. 변경할 필드만 보내면 되며, null인 필드는 기존 값을 유지합니다."
    )
    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<GoalResponse>> updateGoal(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id,
            @RequestBody GoalUpdateRequest request) {
        GoalResponse response = goalService.updateGoal(userDetails.getMemberId(), id, request);
        return ResponseEntity.ok(ApiResponse.of(response));
    }

    @Operation(
            summary = "목표 삭제",
            description = "목표를 비활성 처리합니다 (소프트 삭제). 이후 활성 목표 목록에서 제외됩니다."
    )
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteGoal(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id) {
        goalService.deleteGoal(userDetails.getMemberId(), id);
        return ResponseEntity.ok().build();
    }
}
