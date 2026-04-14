package com.inseong.dallyrun.backend.service;

import com.inseong.dallyrun.backend.dto.request.MemoUpdateRequest;
import com.inseong.dallyrun.backend.dto.request.RunningSessionEndRequest;
import com.inseong.dallyrun.backend.dto.response.RunningSessionDetailResponse;
import com.inseong.dallyrun.backend.dto.response.RunningSessionResponse;
import com.inseong.dallyrun.backend.dto.response.RunningSessionStartResponse;
import com.inseong.dallyrun.backend.entity.GpsPoint;
import com.inseong.dallyrun.backend.entity.Member;
import com.inseong.dallyrun.backend.entity.RunningSession;
import com.inseong.dallyrun.backend.entity.enums.SessionStatus;
import com.inseong.dallyrun.backend.exception.BusinessException;
import com.inseong.dallyrun.backend.exception.ErrorCode;
import com.inseong.dallyrun.backend.repository.GpsPointRepository;
import com.inseong.dallyrun.backend.repository.MemberRepository;
import com.inseong.dallyrun.backend.repository.RunningSessionRepository;
import com.inseong.dallyrun.backend.util.GeoUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class RunningSessionServiceImpl implements RunningSessionService {

    private final RunningSessionRepository runningSessionRepository;
    private final GpsPointRepository gpsPointRepository;
    private final MemberRepository memberRepository;
    private final BadgeService badgeService;

    public RunningSessionServiceImpl(RunningSessionRepository runningSessionRepository,
                                     GpsPointRepository gpsPointRepository,
                                     MemberRepository memberRepository,
                                     BadgeService badgeService) {
        this.runningSessionRepository = runningSessionRepository;
        this.gpsPointRepository = gpsPointRepository;
        this.memberRepository = memberRepository;
        this.badgeService = badgeService;
    }

    @Override
    public RunningSessionStartResponse startSession(Long memberId) {
        runningSessionRepository.findByMemberIdAndStatus(memberId, SessionStatus.IN_PROGRESS)
                .ifPresent(s -> { throw new BusinessException(ErrorCode.RUNNING_SESSION_ALREADY_ACTIVE); });

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        RunningSession session = runningSessionRepository.save(new RunningSession(member));
        return RunningSessionStartResponse.from(session);
    }

    @Override
    public RunningSessionResponse endSession(Long memberId, Long sessionId, RunningSessionEndRequest request) {
        RunningSession session = getOwnedSession(memberId, sessionId);

        if (session.getStatus() == SessionStatus.COMPLETED) {
            throw new BusinessException(ErrorCode.RUNNING_SESSION_ALREADY_COMPLETED);
        }

        List<GpsPoint> gpsPoints = new ArrayList<>();
        List<double[]> coordinates = new ArrayList<>();

        for (RunningSessionEndRequest.GpsPointRequest gps : request.gpsPoints()) {
            GpsPoint point = new GpsPoint(session, gps.latitude(), gps.longitude(),
                    gps.altitude(), gps.recordedAt(), gps.sequenceIndex());
            gpsPoints.add(point);
            coordinates.add(new double[]{gps.latitude(), gps.longitude()});
        }

        gpsPointRepository.saveAll(gpsPoints);

        double distanceMeters = GeoUtils.calculateTotalDistance(coordinates);
        long durationSeconds = Duration.between(session.getStartedAt(), java.time.LocalDateTime.now()).getSeconds();
        Double avgPace = null;
        if (distanceMeters > 0) {
            avgPace = (durationSeconds / 60.0) / (distanceMeters / 1000.0);
        }

        session.complete(distanceMeters, durationSeconds, avgPace);

        badgeService.checkAndAwardBadges(memberId, session);

        return RunningSessionResponse.from(session);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<RunningSessionResponse> getHistory(Long memberId, Pageable pageable) {
        return runningSessionRepository.findByMemberIdOrderByStartedAtDesc(memberId, pageable)
                .map(RunningSessionResponse::from);
    }

    @Override
    @Transactional(readOnly = true)
    public RunningSessionDetailResponse getDetail(Long memberId, Long sessionId) {
        RunningSession session = getOwnedSession(memberId, sessionId);
        return RunningSessionDetailResponse.from(session);
    }

    @Override
    public RunningSessionResponse updateMemo(Long memberId, Long sessionId, MemoUpdateRequest request) {
        RunningSession session = getOwnedSession(memberId, sessionId);
        session.updateMemo(request.memo());
        return RunningSessionResponse.from(session);
    }

    @Override
    public void deleteSession(Long memberId, Long sessionId) {
        RunningSession session = getOwnedSession(memberId, sessionId);
        runningSessionRepository.delete(session);
    }

    private RunningSession getOwnedSession(Long memberId, Long sessionId) {
        RunningSession session = runningSessionRepository.findById(sessionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RUNNING_SESSION_NOT_FOUND));
        if (!session.getMember().getId().equals(memberId)) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }
        return session;
    }
}
