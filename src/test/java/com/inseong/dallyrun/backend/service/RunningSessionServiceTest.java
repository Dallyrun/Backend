package com.inseong.dallyrun.backend.service;

import com.inseong.dallyrun.backend.dto.request.MemoUpdateRequest;
import com.inseong.dallyrun.backend.dto.request.RunningSessionEndRequest;
import com.inseong.dallyrun.backend.dto.response.RunningSessionResponse;
import com.inseong.dallyrun.backend.dto.response.RunningSessionStartResponse;
import com.inseong.dallyrun.backend.entity.Member;
import com.inseong.dallyrun.backend.entity.RunningSession;
import com.inseong.dallyrun.backend.entity.enums.SessionStatus;
import com.inseong.dallyrun.backend.exception.BusinessException;
import com.inseong.dallyrun.backend.repository.GpsPointRepository;
import com.inseong.dallyrun.backend.repository.MemberRepository;
import com.inseong.dallyrun.backend.repository.RunningSessionRepository;
import com.inseong.dallyrun.backend.support.TestEntityHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RunningSessionServiceTest {

    @Mock
    private RunningSessionRepository runningSessionRepository;
    @Mock
    private GpsPointRepository gpsPointRepository;
    @Mock
    private MemberRepository memberRepository;
    @Mock
    private BadgeService badgeService;

    @InjectMocks
    private RunningSessionServiceImpl runningSessionService;

    private Member testMember;

    @BeforeEach
    void setUp() {
        testMember = new Member("test@test.com", "encoded-password", "테스터", null);
        TestEntityHelper.setId(testMember, 1L);
    }

    @Test
    void startSession_success() {
        when(runningSessionRepository.findByMemberIdAndStatus(1L, SessionStatus.IN_PROGRESS))
                .thenReturn(Optional.empty());
        when(memberRepository.findById(1L)).thenReturn(Optional.of(testMember));
        when(runningSessionRepository.save(any(RunningSession.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        RunningSessionStartResponse response = runningSessionService.startSession(1L);

        assertNotNull(response);
        verify(runningSessionRepository).save(any(RunningSession.class));
    }

    @Test
    void startSession_alreadyActive_throwsException() {
        when(runningSessionRepository.findByMemberIdAndStatus(1L, SessionStatus.IN_PROGRESS))
                .thenReturn(Optional.of(new RunningSession(testMember)));

        assertThrows(BusinessException.class, () -> runningSessionService.startSession(1L));
    }

    @Test
    void endSession_calculatesDistanceAndPace() {
        RunningSession session = new RunningSession(testMember);
        TestEntityHelper.setId(session, 1L);
        when(runningSessionRepository.findById(1L)).thenReturn(Optional.of(session));
        when(gpsPointRepository.saveAll(any())).thenReturn(List.of());

        List<RunningSessionEndRequest.GpsPointRequest> gpsPoints = List.of(
                new RunningSessionEndRequest.GpsPointRequest(37.5666, 126.9784, 10.0, LocalDateTime.now(), 0),
                new RunningSessionEndRequest.GpsPointRequest(37.5700, 126.9800, 10.0, LocalDateTime.now(), 1),
                new RunningSessionEndRequest.GpsPointRequest(37.5750, 126.9850, 10.0, LocalDateTime.now(), 2)
        );
        RunningSessionEndRequest request = new RunningSessionEndRequest(gpsPoints);

        RunningSessionResponse response = runningSessionService.endSession(1L, 1L, request);

        assertNotNull(response);
        assertEquals(SessionStatus.COMPLETED, response.status());
        assertNotNull(response.distanceMeters());
        assertTrue(response.distanceMeters() > 0);
        verify(badgeService).checkAndAwardBadges(eq(1L), any(RunningSession.class));
    }

    @Test
    void updateMemo_success() {
        RunningSession session = new RunningSession(testMember);
        TestEntityHelper.setId(session, 1L);
        when(runningSessionRepository.findById(1L)).thenReturn(Optional.of(session));

        RunningSessionResponse response = runningSessionService.updateMemo(1L, 1L,
                new MemoUpdateRequest("좋은 러닝이었다!"));

        assertEquals("좋은 러닝이었다!", response.memo());
    }

    @Test
    void deleteSession_success() {
        RunningSession session = new RunningSession(testMember);
        TestEntityHelper.setId(session, 1L);
        when(runningSessionRepository.findById(1L)).thenReturn(Optional.of(session));

        runningSessionService.deleteSession(1L, 1L);

        verify(runningSessionRepository).delete(session);
    }

    @Test
    void getDetail_notFound_throwsException() {
        when(runningSessionRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(BusinessException.class,
                () -> runningSessionService.getDetail(1L, 99L));
    }
}
