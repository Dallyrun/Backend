package com.inseong.dallyrun.backend.service;

import com.inseong.dallyrun.backend.dto.response.TokenResponse;
import com.inseong.dallyrun.backend.entity.Member;
import com.inseong.dallyrun.backend.entity.enums.OAuthProvider;
import com.inseong.dallyrun.backend.exception.BusinessException;
import com.inseong.dallyrun.backend.repository.MemberRepository;
import com.inseong.dallyrun.backend.security.JwtTokenProvider;
import com.inseong.dallyrun.backend.service.oauth.OAuthClient;
import com.inseong.dallyrun.backend.service.oauth.OAuthUserInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private MemberRepository memberRepository;
    @Mock
    private JwtTokenProvider jwtTokenProvider;
    @Mock
    private StringRedisTemplate redisTemplate;
    @Mock
    private ValueOperations<String, String> valueOperations;
    @Mock
    private OAuthClient kakaoClient;

    private AuthServiceImpl authService;

    @BeforeEach
    void setUp() {
        lenient().when(kakaoClient.getProvider()).thenReturn(OAuthProvider.KAKAO);
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        authService = new AuthServiceImpl(
                List.of(kakaoClient), memberRepository, jwtTokenProvider, redisTemplate);
    }

    @Test
    void oauthLogin_newUser_createsAndReturnsTokens() {
        OAuthUserInfo userInfo = new OAuthUserInfo(
                OAuthProvider.KAKAO, "kakao-123", "test@kakao.com", "테스터", null);
        when(kakaoClient.getUserInfo("auth-code")).thenReturn(userInfo);
        when(memberRepository.findByOauthProviderAndOauthProviderId(OAuthProvider.KAKAO, "kakao-123"))
                .thenReturn(Optional.empty());

        Member savedMember = new Member("test@kakao.com", "테스터", null, OAuthProvider.KAKAO, "kakao-123");
        when(memberRepository.save(any(Member.class))).thenReturn(savedMember);
        when(jwtTokenProvider.createAccessToken(any())).thenReturn("access-token");
        when(jwtTokenProvider.createRefreshToken(any())).thenReturn("refresh-token");
        when(jwtTokenProvider.getRefreshTokenExpiry()).thenReturn(1209600000L);

        TokenResponse response = authService.oauthLogin(OAuthProvider.KAKAO, "auth-code");

        assertNotNull(response);
        assertEquals("access-token", response.accessToken());
        assertEquals("refresh-token", response.refreshToken());
        verify(memberRepository).save(any(Member.class));
    }

    @Test
    void oauthLogin_existingUser_returnsTokens() {
        OAuthUserInfo userInfo = new OAuthUserInfo(
                OAuthProvider.KAKAO, "kakao-123", "test@kakao.com", "테스터", null);
        when(kakaoClient.getUserInfo("auth-code")).thenReturn(userInfo);

        Member existingMember = new Member("test@kakao.com", "테스터", null, OAuthProvider.KAKAO, "kakao-123");
        when(memberRepository.findByOauthProviderAndOauthProviderId(OAuthProvider.KAKAO, "kakao-123"))
                .thenReturn(Optional.of(existingMember));
        when(jwtTokenProvider.createAccessToken(any())).thenReturn("access-token");
        when(jwtTokenProvider.createRefreshToken(any())).thenReturn("refresh-token");
        when(jwtTokenProvider.getRefreshTokenExpiry()).thenReturn(1209600000L);

        TokenResponse response = authService.oauthLogin(OAuthProvider.KAKAO, "auth-code");

        assertNotNull(response);
        verify(memberRepository, never()).save(any());
    }

    @Test
    void refreshToken_valid_returnsNewTokens() {
        when(jwtTokenProvider.validateToken("old-refresh")).thenReturn(true);
        when(jwtTokenProvider.getMemberIdFromToken("old-refresh")).thenReturn(1L);
        when(valueOperations.get("refresh:1")).thenReturn("old-refresh");
        when(jwtTokenProvider.createAccessToken(1L)).thenReturn("new-access");
        when(jwtTokenProvider.createRefreshToken(1L)).thenReturn("new-refresh");
        when(jwtTokenProvider.getRefreshTokenExpiry()).thenReturn(1209600000L);

        TokenResponse response = authService.refreshToken("old-refresh");

        assertEquals("new-access", response.accessToken());
        assertEquals("new-refresh", response.refreshToken());
    }

    @Test
    void refreshToken_invalid_throwsException() {
        when(jwtTokenProvider.validateToken("bad-token")).thenReturn(false);

        assertThrows(BusinessException.class, () -> authService.refreshToken("bad-token"));
    }

    @Test
    void refreshToken_mismatch_throwsException() {
        when(jwtTokenProvider.validateToken("refresh-token")).thenReturn(true);
        when(jwtTokenProvider.getMemberIdFromToken("refresh-token")).thenReturn(1L);
        when(valueOperations.get("refresh:1")).thenReturn("different-token");

        assertThrows(BusinessException.class, () -> authService.refreshToken("refresh-token"));
    }

    @Test
    void logout_deletesRefreshToken() {
        authService.logout(1L);

        verify(redisTemplate).delete("refresh:1");
    }
}
