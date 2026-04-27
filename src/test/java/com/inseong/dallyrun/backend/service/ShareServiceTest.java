package com.inseong.dallyrun.backend.service;

import com.inseong.dallyrun.backend.config.ShareConfig;
import com.inseong.dallyrun.backend.dto.response.ShareDataResponse;
import com.inseong.dallyrun.backend.dto.response.ShareLinkResponse;
import com.inseong.dallyrun.backend.entity.AgeBracket;
import com.inseong.dallyrun.backend.entity.Gender;
import com.inseong.dallyrun.backend.entity.Member;
import com.inseong.dallyrun.backend.entity.RunningSession;
import com.inseong.dallyrun.backend.exception.BusinessException;
import com.inseong.dallyrun.backend.exception.ErrorCode;
import com.inseong.dallyrun.backend.repository.MemberRepository;
import com.inseong.dallyrun.backend.repository.RunningSessionRepository;
import com.inseong.dallyrun.backend.support.TestEntityHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ShareServiceTest {

    @Mock
    private RunningSessionRepository runningSessionRepository;
    @Mock
    private MemberRepository memberRepository;
    @Mock
    private StringRedisTemplate redisTemplate;
    @Mock
    private ValueOperations<String, String> valueOperations;

    private ShareServiceImpl shareService;
    private Member testMember;

    @BeforeEach
    void setUp() {
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        ShareConfig shareConfig = new ShareConfig(30);
        shareService = new ShareServiceImpl(runningSessionRepository, memberRepository,
                redisTemplate, shareConfig);
        testMember = new Member("test@test.com", "encoded-password", "테스터",
                "https://img.test/p.jpg", AgeBracket.THIRTIES, Gender.MALE);
        TestEntityHelper.setId(testMember, 1L);
    }

    @Test
    void getShareData_success() {
        RunningSession session = new RunningSession(testMember);
        TestEntityHelper.setId(session, 1L);
        session.complete(LocalDateTime.now(), 5000.0, 1800L, 6.0);
        when(runningSessionRepository.findById(1L)).thenReturn(Optional.of(session));
        when(memberRepository.findById(1L)).thenReturn(Optional.of(testMember));

        ShareDataResponse response = shareService.getShareData(1L, 1L);

        assertEquals("테스터", response.nickname());
        assertEquals(5000.0, response.distanceMeters());
    }

    @Test
    void createShareLink_success() {
        RunningSession session = new RunningSession(testMember);
        TestEntityHelper.setId(session, 1L);
        when(runningSessionRepository.findById(1L)).thenReturn(Optional.of(session));

        ShareLinkResponse response = shareService.createShareLink(1L, 1L);

        assertNotNull(response.shareCode());
        assertEquals(16, response.shareCode().length());
        assertTrue(response.shareUrl().contains(response.shareCode()));
        verify(valueOperations).set(anyString(), anyString(), anyLong(), any());
    }

    @Test
    void getSharedData_success() {
        RunningSession session = new RunningSession(testMember);
        TestEntityHelper.setId(session, 1L);
        session.complete(LocalDateTime.now(), 5000.0, 1800L, 6.0);
        when(valueOperations.get("share:abc123")).thenReturn("1");
        when(runningSessionRepository.findById(1L)).thenReturn(Optional.of(session));
        when(memberRepository.findById(1L)).thenReturn(Optional.of(testMember));

        ShareDataResponse response = shareService.getSharedData("abc123");

        assertEquals("테스터", response.nickname());
    }

    @Test
    void getSharedData_notFound_throwsException() {
        when(valueOperations.get("share:invalid")).thenReturn(null);

        assertThrows(BusinessException.class, () -> shareService.getSharedData("invalid"));
    }

    @Test
    void getSharedData_malformedRedisValue_throwsShareNotFound() {
        // Redis 값이 손상되어 숫자로 파싱 불가능한 경우에도 404 응답이 되어야 한다.
        when(valueOperations.get("share:corrupted")).thenReturn("not-a-number");

        assertThrows(BusinessException.class, () -> shareService.getSharedData("corrupted"));
        verify(runningSessionRepository, never()).findById(any());
    }

    @Test
    void getSharedData_memberSoftDeleted_throwsShareNotFound() {
        // 세션 소유자가 탈퇴(soft delete) 한 경우, MemberRepository.findById 가 비어
        // SHARE_NOT_FOUND 로 응답하여 외부에 데이터가 노출되지 않아야 한다.
        RunningSession session = new RunningSession(testMember);
        TestEntityHelper.setId(session, 1L);
        session.complete(LocalDateTime.now(), 5000.0, 1800L, 6.0);
        when(valueOperations.get("share:abc123")).thenReturn("1");
        when(runningSessionRepository.findById(1L)).thenReturn(Optional.of(session));
        when(memberRepository.findById(1L)).thenReturn(Optional.empty());

        BusinessException ex = assertThrows(BusinessException.class,
                () -> shareService.getSharedData("abc123"));
        assertEquals(ErrorCode.SHARE_NOT_FOUND, ex.getErrorCode());
    }

    @Test
    void getSharedData_sessionNotFound_throwsShareNotFound() {
        // 세션 자체가 사라진 경우에도 RUNNING_SESSION_NOT_FOUND 가 아닌 SHARE_NOT_FOUND 로
        // 통일하여 외부에 노출되는 에러 코드를 일관되게 유지한다.
        when(valueOperations.get("share:abc123")).thenReturn("999");
        when(runningSessionRepository.findById(999L)).thenReturn(Optional.empty());

        BusinessException ex = assertThrows(BusinessException.class,
                () -> shareService.getSharedData("abc123"));
        assertEquals(ErrorCode.SHARE_NOT_FOUND, ex.getErrorCode());
        verify(memberRepository, never()).findById(any());
    }
}
