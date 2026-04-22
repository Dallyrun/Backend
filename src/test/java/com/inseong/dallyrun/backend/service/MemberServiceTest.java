package com.inseong.dallyrun.backend.service;

import com.inseong.dallyrun.backend.dto.request.MemberUpdateRequest;
import com.inseong.dallyrun.backend.dto.response.MemberResponse;
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

    @InjectMocks
    private MemberServiceImpl memberService;

    private Member testMember;

    @BeforeEach
    void setUp() {
        testMember = new Member("test@test.com", "encoded-password", "테스터", null);
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
    void deleteMember_success() {
        when(memberRepository.findById(1L)).thenReturn(Optional.of(testMember));

        memberService.deleteMember(1L);

        verify(memberRepository).delete(testMember);
    }

    @Test
    void deleteMember_notFound_throwsException() {
        when(memberRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(BusinessException.class, () -> memberService.deleteMember(99L));
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
        // 이전 URL이 없었으므로 delete는 호출되지 않아야 함
        verify(fileStorage, never()).delete(any());
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
