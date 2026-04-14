package com.inseong.dallyrun.backend.controller;

import com.inseong.dallyrun.backend.dto.request.MemoUpdateRequest;
import com.inseong.dallyrun.backend.dto.request.RunningSessionEndRequest;
import com.inseong.dallyrun.backend.dto.response.*;
import com.inseong.dallyrun.backend.security.CustomUserDetails;
import com.inseong.dallyrun.backend.service.RunningSessionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Running Session", description = "러닝 세션 API")
@RestController
@RequestMapping("/api/running-sessions")
public class RunningSessionController {

    private final RunningSessionService runningSessionService;

    public RunningSessionController(RunningSessionService runningSessionService) {
        this.runningSessionService = runningSessionService;
    }

    @Operation(summary = "러닝 시작")
    @PostMapping
    public ResponseEntity<ApiResponse<RunningSessionStartResponse>> startSession(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        RunningSessionStartResponse response = runningSessionService.startSession(userDetails.getMemberId());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @Operation(summary = "러닝 종료")
    @PatchMapping("/{id}/end")
    public ResponseEntity<ApiResponse<RunningSessionResponse>> endSession(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id,
            @Valid @RequestBody RunningSessionEndRequest request) {
        RunningSessionResponse response = runningSessionService.endSession(
                userDetails.getMemberId(), id, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "러닝 히스토리 목록")
    @GetMapping
    public ResponseEntity<ApiResponse<Page<RunningSessionResponse>>> getHistory(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<RunningSessionResponse> response = runningSessionService.getHistory(
                userDetails.getMemberId(), pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "러닝 상세 조회")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<RunningSessionDetailResponse>> getDetail(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id) {
        RunningSessionDetailResponse response = runningSessionService.getDetail(
                userDetails.getMemberId(), id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "메모 수정")
    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<RunningSessionResponse>> updateMemo(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id,
            @RequestBody MemoUpdateRequest request) {
        RunningSessionResponse response = runningSessionService.updateMemo(
                userDetails.getMemberId(), id, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "러닝 삭제")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteSession(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id) {
        runningSessionService.deleteSession(userDetails.getMemberId(), id);
        return ResponseEntity.ok(ApiResponse.success());
    }
}
