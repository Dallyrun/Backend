package com.inseong.dallyrun.backend.controller;

import com.inseong.dallyrun.backend.dto.request.MemoUpdateRequest;
import com.inseong.dallyrun.backend.dto.request.RunningSessionEndRequest;
import com.inseong.dallyrun.backend.dto.response.*;
import com.inseong.dallyrun.backend.security.CustomUserDetails;
import com.inseong.dallyrun.backend.service.RunningSessionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Running Session", description = "러닝 세션 API — 러닝 시작/종료, GPS 데이터 저장, 히스토리 조회. "
        + "러닝 중에는 서버 통신 없이 앱이 GPS를 로컬에 수집하고, 종료 시 일괄 전송합니다.")
@RestController
@RequestMapping("/api/running-sessions")
public class RunningSessionController {

    private final RunningSessionService runningSessionService;

    public RunningSessionController(RunningSessionService runningSessionService) {
        this.runningSessionService = runningSessionService;
    }

    @Operation(
            summary = "러닝 시작",
            description = "새 러닝 세션을 생성합니다. 반환된 sessionId를 앱에 저장해두고, "
                    + "러닝 종료 시 이 ID로 종료 API를 호출합니다. "
                    + "이미 진행 중인 세션이 있으면 409 Conflict를 반환합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "세션 생성 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "이미 진행 중인 세션 존재")
    })
    @PostMapping
    public ResponseEntity<ApiResponse<RunningSessionStartResponse>> startSession(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        RunningSessionStartResponse response = runningSessionService.startSession(userDetails.getMemberId());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.of(response));
    }

    @Operation(
            summary = "러닝 종료 + GPS 데이터 전송",
            description = "러닝을 종료하고, 앱이 수집한 GPS 좌표 배열을 일괄 전송합니다. "
                    + "서버가 GPS 데이터를 기반으로 총 거리(Haversine), 소요 시간, 평균 페이스를 계산합니다. "
                    + "동시에 뱃지 조건도 자동으로 판정됩니다.\n\n"
                    + "**GPS 수집 가이드**: 1~3초 간격으로 수집 권장. sequenceIndex는 0부터 순서대로 부여."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "종료 성공, 계산된 통계 반환"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "이미 종료된 세션")
    })
    @PatchMapping("/{id}/end")
    public ResponseEntity<ApiResponse<RunningSessionResponse>> endSession(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id,
            @Valid @RequestBody RunningSessionEndRequest request) {
        RunningSessionResponse response = runningSessionService.endSession(
                userDetails.getMemberId(), id, request);
        return ResponseEntity.ok(ApiResponse.of(response));
    }

    @Operation(
            summary = "러닝 히스토리 목록",
            description = "완료/진행 중인 러닝 기록을 최신순으로 페이징 조회합니다. "
                    + "GPS 경로 데이터는 포함되지 않습니다 (상세 API에서 조회). "
                    + "기본 페이지 크기는 20입니다."
    )
    @GetMapping
    public ResponseEntity<ApiResponse<Page<RunningSessionResponse>>> getHistory(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<RunningSessionResponse> response = runningSessionService.getHistory(
                userDetails.getMemberId(), pageable);
        return ResponseEntity.ok(ApiResponse.of(response));
    }

    @Operation(
            summary = "러닝 상세 조회 (GPS 경로 포함)",
            description = "러닝 기록의 상세 정보를 조회합니다. GPS 좌표 배열이 포함되어 있어 "
                    + "지도에 러닝 경로를 그릴 수 있습니다."
    )
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<RunningSessionDetailResponse>> getDetail(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id) {
        RunningSessionDetailResponse response = runningSessionService.getDetail(
                userDetails.getMemberId(), id);
        return ResponseEntity.ok(ApiResponse.of(response));
    }

    @Operation(
            summary = "러닝 메모 수정",
            description = "러닝 기록에 메모를 추가하거나 수정합니다."
    )
    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<RunningSessionResponse>> updateMemo(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id,
            @Valid @RequestBody MemoUpdateRequest request) {
        RunningSessionResponse response = runningSessionService.updateMemo(
                userDetails.getMemberId(), id, request);
        return ResponseEntity.ok(ApiResponse.of(response));
    }

    @Operation(
            summary = "러닝 기록 삭제",
            description = "러닝 기록과 관련 GPS 데이터를 모두 삭제합니다."
    )
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSession(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id) {
        runningSessionService.deleteSession(userDetails.getMemberId(), id);
        return ResponseEntity.ok().build();
    }
}
