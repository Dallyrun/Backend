package com.inseong.dallyrun.backend.service;

import com.inseong.dallyrun.backend.dto.request.LoginRequest;
import com.inseong.dallyrun.backend.dto.request.SignupRequest;
import com.inseong.dallyrun.backend.dto.response.TokenResponse;
import com.inseong.dallyrun.backend.entity.AgeBracket;
import com.inseong.dallyrun.backend.entity.Gender;
import com.inseong.dallyrun.backend.entity.Member;
import com.inseong.dallyrun.backend.exception.BusinessException;
import com.inseong.dallyrun.backend.exception.ErrorCode;
import com.inseong.dallyrun.backend.repository.MemberRepository;
import com.inseong.dallyrun.backend.security.JwtTokenProvider;
import com.inseong.dallyrun.backend.storage.FileStorage;
import com.inseong.dallyrun.backend.support.TestEntityHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    private static final String PROFILE_IMAGE_URL = "https://img.test/p.jpg";

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
    @Mock
    private FileStorage fileStorage;

    private AuthServiceImpl authService;

    @BeforeEach
    void setUp() {
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        authService = new AuthServiceImpl(
                memberRepository, jwtTokenProvider, redisTemplate, passwordEncoder, fileStorage);
    }

    private SignupRequest validRequest() {
        return new SignupRequest("test@test.com", "Password123!", "테스터123",
                AgeBracket.THIRTIES, Gender.MALE);
    }

    private MultipartFile validImage() {
        return new MockMultipartFile("image", "p.jpg", "image/jpeg", new byte[]{1, 2, 3});
    }

    @Test
    void signup_success_encodesPasswordAndIssuesTokens() {
        SignupRequest request = validRequest();
        MultipartFile image = validImage();
        when(memberRepository.existsByEmail("test@test.com")).thenReturn(false);
        when(memberRepository.existsByNickname("테스터123")).thenReturn(false);
        when(fileStorage.store(image, "profile-images")).thenReturn(PROFILE_IMAGE_URL);
        when(passwordEncoder.encode("Password123!")).thenReturn("hashed-password");

        Member savedMember = new Member("test@test.com", "hashed-password", "테스터123",
                PROFILE_IMAGE_URL, AgeBracket.THIRTIES, Gender.MALE);
        TestEntityHelper.setId(savedMember, 1L);
        when(memberRepository.save(any(Member.class))).thenReturn(savedMember);
        when(jwtTokenProvider.createAccessToken(1L)).thenReturn("access-token");
        when(jwtTokenProvider.createRefreshToken(1L)).thenReturn("refresh-token");
        when(jwtTokenProvider.getRefreshTokenExpiry()).thenReturn(1209600000L);

        TokenResponse response = authService.signup(request, image);

        assertEquals("access-token", response.accessToken());
        assertEquals("refresh-token", response.refreshToken());

        ArgumentCaptor<Member> captor = ArgumentCaptor.forClass(Member.class);
        verify(memberRepository).save(captor.capture());
        Member saved = captor.getValue();
        assertEquals("hashed-password", saved.getPasswordHash());
        assertEquals("test@test.com", saved.getEmail());
        assertEquals(PROFILE_IMAGE_URL, saved.getProfileImageUrl());
        assertEquals(AgeBracket.THIRTIES, saved.getAgeBracket());
        assertEquals(Gender.MALE, saved.getGender());
    }

    @Test
    void signup_duplicateEmail_throwsEmailAlreadyExists() {
        SignupRequest request = validRequest();
        when(memberRepository.existsByEmail("test@test.com")).thenReturn(true);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> authService.signup(request, validImage()));
        assertEquals(ErrorCode.EMAIL_ALREADY_EXISTS, ex.getErrorCode());
        verify(memberRepository, never()).save(any());
        verify(fileStorage, never()).store(any(), any());
    }

    @Test
    void signup_duplicateNickname_throwsNicknameAlreadyExists() {
        SignupRequest request = validRequest();
        when(memberRepository.existsByEmail("test@test.com")).thenReturn(false);
        when(memberRepository.existsByNickname("테스터123")).thenReturn(true);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> authService.signup(request, validImage()));
        assertEquals(ErrorCode.NICKNAME_ALREADY_EXISTS, ex.getErrorCode());
        verify(memberRepository, never()).save(any());
        verify(fileStorage, never()).store(any(), any());
    }

    @Test
    void signup_missingImage_throwsProfileImageRequired() {
        SignupRequest request = validRequest();

        BusinessException ex = assertThrows(BusinessException.class,
                () -> authService.signup(request, null));
        assertEquals(ErrorCode.PROFILE_IMAGE_REQUIRED, ex.getErrorCode());
    }

    @Test
    void signup_invalidImageMime_throwsInvalidFileType() {
        SignupRequest request = validRequest();
        MultipartFile badImage = new MockMultipartFile("image", "p.gif", "image/gif", new byte[]{1});

        BusinessException ex = assertThrows(BusinessException.class,
                () -> authService.signup(request, badImage));
        assertEquals(ErrorCode.INVALID_FILE_TYPE, ex.getErrorCode());
    }

    @Test
    void signup_dbSaveFails_compensatesByDeletingUploadedImage() {
        SignupRequest request = validRequest();
        MultipartFile image = validImage();
        when(memberRepository.existsByEmail(any())).thenReturn(false);
        when(memberRepository.existsByNickname(any())).thenReturn(false);
        when(fileStorage.store(image, "profile-images")).thenReturn(PROFILE_IMAGE_URL);
        when(passwordEncoder.encode(any())).thenReturn("hashed");
        when(memberRepository.save(any())).thenThrow(new RuntimeException("db down"));

        assertThrows(RuntimeException.class, () -> authService.signup(request, image));
        verify(fileStorage).delete(PROFILE_IMAGE_URL);
    }

    @Test
    void login_success_returnsTokens() {
        Member member = new Member("test@test.com", "hashed-password", "테스터123",
                PROFILE_IMAGE_URL, AgeBracket.THIRTIES, Gender.MALE);
        TestEntityHelper.setId(member, 1L);
        when(memberRepository.findByEmail("test@test.com")).thenReturn(Optional.of(member));
        when(passwordEncoder.matches("Password123!", "hashed-password")).thenReturn(true);
        when(jwtTokenProvider.createAccessToken(1L)).thenReturn("access-token");
        when(jwtTokenProvider.createRefreshToken(1L)).thenReturn("refresh-token");
        when(jwtTokenProvider.getRefreshTokenExpiry()).thenReturn(1209600000L);

        TokenResponse response = authService.login(new LoginRequest("test@test.com", "Password123!"));

        assertEquals("access-token", response.accessToken());
        assertEquals("refresh-token", response.refreshToken());
    }

    @Test
    void login_userNotFound_throwsInvalidCredentials() {
        when(memberRepository.findByEmail("none@test.com")).thenReturn(Optional.empty());

        BusinessException ex = assertThrows(BusinessException.class,
                () -> authService.login(new LoginRequest("none@test.com", "Password123!")));
        assertEquals(ErrorCode.INVALID_CREDENTIALS, ex.getErrorCode());
    }

    @Test
    void login_wrongPassword_throwsInvalidCredentials() {
        Member member = new Member("test@test.com", "hashed-password", "테스터123",
                PROFILE_IMAGE_URL, AgeBracket.THIRTIES, Gender.MALE);
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
