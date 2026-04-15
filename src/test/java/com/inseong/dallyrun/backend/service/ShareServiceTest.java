package com.inseong.dallyrun.backend.service;

import com.inseong.dallyrun.backend.config.ShareConfig;
import com.inseong.dallyrun.backend.dto.response.ShareDataResponse;
import com.inseong.dallyrun.backend.dto.response.ShareLinkResponse;
import com.inseong.dallyrun.backend.entity.Member;
import com.inseong.dallyrun.backend.entity.RunningSession;
import com.inseong.dallyrun.backend.entity.enums.OAuthProvider;
import com.inseong.dallyrun.backend.exception.BusinessException;
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
    private StringRedisTemplate redisTemplate;
    @Mock
    private ValueOperations<String, String> valueOperations;

    private ShareServiceImpl shareService;
    private Member testMember;

    @BeforeEach
    void setUp() {
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        ShareConfig shareConfig = new ShareConfig(30);
        shareService = new ShareServiceImpl(runningSessionRepository, redisTemplate, shareConfig);
        testMember = new Member("test@test.com", "테스터", null, OAuthProvider.KAKAO, "kakao-1");
        TestEntityHelper.setId(testMember, 1L);
    }

    @Test
    void getShareData_success() {
        RunningSession session = new RunningSession(testMember);
        TestEntityHelper.setId(session, 1L);
        session.complete(LocalDateTime.now(), 5000.0, 1800L, 6.0);
        when(runningSessionRepository.findById(1L)).thenReturn(Optional.of(session));

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

        ShareDataResponse response = shareService.getSharedData("abc123");

        assertEquals("테스터", response.nickname());
    }

    @Test
    void getSharedData_notFound_throwsException() {
        when(valueOperations.get("share:invalid")).thenReturn(null);

        assertThrows(BusinessException.class, () -> shareService.getSharedData("invalid"));
    }
}
