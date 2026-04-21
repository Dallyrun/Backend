package com.inseong.dallyrun.backend.service;

import com.inseong.dallyrun.backend.dto.request.LoginRequest;
import com.inseong.dallyrun.backend.dto.request.SignupRequest;
import com.inseong.dallyrun.backend.dto.response.TokenResponse;
import com.inseong.dallyrun.backend.entity.Member;
import com.inseong.dallyrun.backend.exception.BusinessException;
import com.inseong.dallyrun.backend.exception.ErrorCode;
import com.inseong.dallyrun.backend.repository.MemberRepository;
import com.inseong.dallyrun.backend.security.JwtTokenProvider;
import com.inseong.dallyrun.backend.support.TestEntityHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.crypto.password.PasswordEncoder;

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
    private PasswordEncoder passwordEncoder;

    private AuthServiceImpl authService;

    @BeforeEach
    void setUp() {
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        authService = new AuthServiceImpl(
                memberRepository, jwtTokenProvider, redisTemplate, passwordEncoder);
    }

    @Test
    void signup_success_encodesPasswordAndIssuesTokens() {
        SignupRequest request = new SignupRequest("test@test.com", "password123", "테스터");
        when(memberRepository.existsByEmail("test@test.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("hashed-password");

        Member savedMember = new Member("test@test.com", "hashed-password", "테스터", null);
        TestEntityHelper.setId(savedMember, 1L);
        when(memberRepository.save(any(Member.class))).thenReturn(savedMember);
        when(jwtTokenProvider.createAccessToken(1L)).thenReturn("access-token");
        when(jwtTokenProvider.createRefreshToken(1L)).thenReturn("refresh-token");
        when(jwtTokenProvider.getRefreshTokenExpiry()).thenReturn(1209600000L);

        TokenResponse response = authService.signup(request);

        assertEquals("access-token", response.accessToken());
        assertEquals("refresh-token", response.refreshToken());

        ArgumentCaptor<Member> captor = ArgumentCaptor.forClass(Member.class);
        verify(memberRepository).save(captor.capture());
        // BCrypt 해시값이 저장되어야 함 (원본 비밀번호가 아닌)
        assertEquals("hashed-password", captor.getValue().getPasswordHash());
        assertEquals("test@test.com", captor.getValue().getEmail());
    }

    @Test
    void signup_duplicateEmail_throwsEmailAlreadyExists() {
        SignupRequest request = new SignupRequest("dup@test.com", "password123", "중복");
        when(memberRepository.existsByEmail("dup@test.com")).thenReturn(true);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> authService.signup(request));
        assertEquals(ErrorCode.EMAIL_ALREADY_EXISTS, ex.getErrorCode());
        verify(memberRepository, never()).save(any());
    }

    @Test
    void login_success_returnsTokens() {
        Member member = new Member("test@test.com", "hashed-password", "테스터", null);
        TestEntityHelper.setId(member, 1L);
        when(memberRepository.findByEmail("test@test.com")).thenReturn(Optional.of(member));
        when(passwordEncoder.matches("password123", "hashed-password")).thenReturn(true);
        when(jwtTokenProvider.createAccessToken(1L)).thenReturn("access-token");
        when(jwtTokenProvider.createRefreshToken(1L)).thenReturn("refresh-token");
        when(jwtTokenProvider.getRefreshTokenExpiry()).thenReturn(1209600000L);

        TokenResponse response = authService.login(new LoginRequest("test@test.com", "password123"));

        assertEquals("access-token", response.accessToken());
        assertEquals("refresh-token", response.refreshToken());
    }

    @Test
    void login_userNotFound_throwsInvalidCredentials() {
        when(memberRepository.findByEmail("none@test.com")).thenReturn(Optional.empty());

        BusinessException ex = assertThrows(BusinessException.class,
                () -> authService.login(new LoginRequest("none@test.com", "password123")));
        assertEquals(ErrorCode.INVALID_CREDENTIALS, ex.getErrorCode());
    }

    @Test
    void login_wrongPassword_throwsInvalidCredentials() {
        Member member = new Member("test@test.com", "hashed-password", "테스터", null);
        TestEntityHelper.setId(member, 1L);
        when(memberRepository.findByEmail("test@test.com")).thenReturn(Optional.of(member));
        when(passwordEncoder.matches("wrong", "hashed-password")).thenReturn(false);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> authService.login(new LoginRequest("test@test.com", "wrong")));
        assertEquals(ErrorCode.INVALID_CREDENTIALS, ex.getErrorCode());
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
