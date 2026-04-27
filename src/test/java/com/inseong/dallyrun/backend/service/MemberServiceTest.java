package com.inseong.dallyrun.backend.service;

import com.inseong.dallyrun.backend.dto.request.MemberUpdateRequest;
import com.inseong.dallyrun.backend.dto.response.MemberResponse;
import com.inseong.dallyrun.backend.entity.AgeBracket;
import com.inseong.dallyrun.backend.entity.Gender;
import com.inseong.dallyrun.backend.entity.Member;
import com.inseong.dallyrun.backend.exception.BusinessException;
import com.inseong.dallyrun.backend.exception.ErrorCode;
import com.inseong.dallyrun.backend.repository.MemberRepository;
import com.inseong.dallyrun.backend.storage.FileStorage;
import com.inseong.dallyrun.backend.support.TestEntityHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private FileStorage fileStorage;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthService authService;

    @InjectMocks
    private MemberServiceImpl memberService;

    private Member testMember;

    @BeforeEach
    void setUp() {
        testMember = new Member("test@test.com", "encoded-password", "테스터",
                "https://img.test/p.jpg", AgeBracket.THIRTIES, Gender.MALE);
        TestEntityHelper.setId(testMember, 1L);
    }

    @Test
    void getProfile_success() {
        when(memberRepository.findById(1L)).thenReturn(Optional.of(testMember));

        MemberResponse response = memberService.getProfile(1L);

        assertEquals("테스터", response.nickname());
        assertEquals("test@test.com", response.email());
    }

    @Test
    void getProfile_notFound_throwsException() {
        when(memberRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(BusinessException.class, () -> memberService.getProfile(99L));
    }

    @Test
    void updateProfile_success() {
        when(memberRepository.findById(1L)).thenReturn(Optional.of(testMember));

        MemberResponse response = memberService.updateProfile(1L,
                new MemberUpdateRequest("새닉네임", null));

        assertEquals("새닉네임", response.nickname());
    }

    @Test
    void updateProfile_notFound_throwsException() {
        when(memberRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(BusinessException.class,
                () -> memberService.updateProfile(99L, new MemberUpdateRequest("닉", null)));
    }

    @Test
    void deleteMember_success_softDeletesAndLogsOut() {
        when(memberRepository.findById(1L)).thenReturn(Optional.of(testMember));
        when(passwordEncoder.matches("rawPassword", "encoded-password")).thenReturn(true);

        memberService.deleteMember(1L, "rawPassword");

        // soft delete: 행이 살아있고 deletedAt 이 채워진다
        assertNotNull(testMember.getDeletedAt());
        verify(memberRepository, never()).delete(testMember);
        // refresh token 폐기
        verify(authService).logout(1L);
    }

    @Test
    void deleteMember_wrongPassword_throwsInvalidCredentials() {
        when(memberRepository.findById(1L)).thenReturn(Optional.of(testMember));
        when(passwordEncoder.matches("wrongPassword", "encoded-password")).thenReturn(false);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> memberService.deleteMember(1L, "wrongPassword"));
        assertEquals(ErrorCode.INVALID_CREDENTIALS, ex.getErrorCode());
        // 비밀번호 틀리면 soft delete / logout 모두 일어나선 안 됨
        assertNull(testMember.getDeletedAt());
        verify(authService, never()).logout(any());
    }

    @Test
    void deleteMember_notFound_throwsException() {
        when(memberRepository.findById(99L)).thenReturn(Optional.empty());

        BusinessException ex = assertThrows(BusinessException.class,
                () -> memberService.deleteMember(99L, "anything"));
        assertEquals(ErrorCode.MEMBER_NOT_FOUND, ex.getErrorCode());
        verify(authService, never()).logout(any());
    }

    @Test
    void uploadProfileImage_success_storesAndUpdatesMember() {
        MultipartFile file = new MockMultipartFile(
                "file", "avatar.jpg", "image/jpeg", new byte[]{1, 2, 3});
        when(memberRepository.findById(1L)).thenReturn(Optional.of(testMember));
        when(fileStorage.store(eq(file), eq("profile-images")))
                .thenReturn("http://localhost:8080/uploads/profile-images/new.jpg");

        MemberResponse response = memberService.uploadProfileImage(1L, file);

        assertEquals("http://localhost:8080/uploads/profile-images/new.jpg", response.profileImageUrl());
        assertEquals("http://localhost:8080/uploads/profile-images/new.jpg", testMember.getProfileImageUrl());
    }

    @Test
    void uploadProfileImage_replacesOld_deletesPrevious() {
        testMember.updateProfile(null, "http://localhost:8080/uploads/profile-images/old.jpg");
        MultipartFile file = new MockMultipartFile(
                "file", "avatar.png", "image/png", new byte[]{1});
        when(memberRepository.findById(1L)).thenReturn(Optional.of(testMember));
        when(fileStorage.store(eq(file), eq("profile-images")))
                .thenReturn("http://localhost:8080/uploads/profile-images/new.png");

        memberService.uploadProfileImage(1L, file);

        verify(fileStorage).delete("http://localhost:8080/uploads/profile-images/old.jpg");
    }

    @Test
    void uploadProfileImage_invalidMime_throwsInvalidFileType() {
        MultipartFile file = new MockMultipartFile(
                "file", "doc.pdf", "application/pdf", new byte[]{1});

        BusinessException ex = assertThrows(BusinessException.class,
                () -> memberService.uploadProfileImage(1L, file));
        assertEquals(ErrorCode.INVALID_FILE_TYPE, ex.getErrorCode());
        verify(fileStorage, never()).store(any(), any());
        verify(memberRepository, never()).findById(any());
    }

    @Test
    void uploadProfileImage_nullMime_throwsInvalidFileType() {
        MultipartFile file = new MockMultipartFile("file", "x", null, new byte[]{1});

        BusinessException ex = assertThrows(BusinessException.class,
                () -> memberService.uploadProfileImage(1L, file));
        assertEquals(ErrorCode.INVALID_FILE_TYPE, ex.getErrorCode());
    }

    @Test
    void uploadProfileImage_emptyFile_throwsInvalidInput() {
        MultipartFile empty = new MockMultipartFile(
                "file", "x.jpg", "image/jpeg", new byte[]{});

        BusinessException ex = assertThrows(BusinessException.class,
                () -> memberService.uploadProfileImage(1L, empty));
        assertEquals(ErrorCode.INVALID_INPUT, ex.getErrorCode());
    }

    @Test
    void uploadProfileImage_memberNotFound_throwsAndDoesNotStore() {
        MultipartFile file = new MockMultipartFile(
                "file", "avatar.jpg", "image/jpeg", new byte[]{1});
        when(memberRepository.findById(99L)).thenReturn(Optional.empty());

        BusinessException ex = assertThrows(BusinessException.class,
                () -> memberService.uploadProfileImage(99L, file));
        assertEquals(ErrorCode.MEMBER_NOT_FOUND, ex.getErrorCode());
        // 회원이 없으면 스토리지에 쓰지 않아야 한다 (고아 파일 방지)
        verify(fileStorage, never()).store(any(), any());
    }
}
