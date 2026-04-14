package com.inseong.dallyrun.backend.service;

import com.inseong.dallyrun.backend.dto.request.MemoUpdateRequest;
import com.inseong.dallyrun.backend.dto.request.RunningSessionEndRequest;
import com.inseong.dallyrun.backend.dto.response.RunningSessionDetailResponse;
import com.inseong.dallyrun.backend.dto.response.RunningSessionResponse;
import com.inseong.dallyrun.backend.dto.response.RunningSessionStartResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface RunningSessionService {

    RunningSessionStartResponse startSession(Long memberId);

    RunningSessionResponse endSession(Long memberId, Long sessionId, RunningSessionEndRequest request);

    Page<RunningSessionResponse> getHistory(Long memberId, Pageable pageable);

    RunningSessionDetailResponse getDetail(Long memberId, Long sessionId);

    RunningSessionResponse updateMemo(Long memberId, Long sessionId, MemoUpdateRequest request);

    void deleteSession(Long memberId, Long sessionId);
}
