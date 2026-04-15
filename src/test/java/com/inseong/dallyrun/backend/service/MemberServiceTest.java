package com.inseong.dallyrun.backend.service;

import com.inseong.dallyrun.backend.dto.request.MemberUpdateRequest;
import com.inseong.dallyrun.backend.dto.response.MemberResponse;
import com.inseong.dallyrun.backend.entity.Member;
import com.inseong.dallyrun.backend.entity.enums.OAuthProvider;
import com.inseong.dallyrun.backend.exception.BusinessException;
import com.inseong.dallyrun.backend.repository.MemberRepository;
import com.inseong.dallyrun.backend.support.TestEntityHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @InjectMocks
    private MemberServiceImpl memberService;

    private Member testMember;

    @BeforeEach
    void setUp() {
        testMember = new Member("test@test.com", "테스터", null, OAuthProvider.KAKAO, "kakao-1");
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
}
