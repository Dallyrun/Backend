package com.inseong.dallyrun.backend.service;

import com.inseong.dallyrun.backend.entity.*;
import com.inseong.dallyrun.backend.entity.enums.ConditionType;
import com.inseong.dallyrun.backend.entity.enums.OAuthProvider;
import com.inseong.dallyrun.backend.repository.BadgeRepository;
import com.inseong.dallyrun.backend.repository.MemberBadgeRepository;
import com.inseong.dallyrun.backend.repository.MemberRepository;
import com.inseong.dallyrun.backend.repository.RunningSessionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BadgeServiceTest {

    @Mock
    private BadgeRepository badgeRepository;
    @Mock
    private MemberBadgeRepository memberBadgeRepository;
    @Mock
    private MemberRepository memberRepository;
    @Mock
    private RunningSessionRepository runningSessionRepository;

    @InjectMocks
    private BadgeServiceImpl badgeService;

    private Member testMember;

    @BeforeEach
    void setUp() {
        testMember = new Member("test@test.com", "테스터", null, OAuthProvider.KAKAO, "kakao-1");
    }

    @Test
    void checkAndAwardBadges_totalCount_awards() {
        Badge badge = new Badge("첫 러닝", "설명", null, ConditionType.TOTAL_COUNT, 1.0);
        RunningSession session = new RunningSession(testMember);
        session.complete(LocalDateTime.now(), 5000.0, 1800L, 6.0);

        when(memberRepository.findById(1L)).thenReturn(Optional.of(testMember));
        when(badgeRepository.findAll()).thenReturn(List.of(badge));
        when(memberBadgeRepository.existsByMemberIdAndBadgeId(anyLong(), any())).thenReturn(false);
        when(runningSessionRepository.countCompletedByMemberId(1L)).thenReturn(1L);

        badgeService.checkAndAwardBadges(1L, session);

        ArgumentCaptor<MemberBadge> captor = ArgumentCaptor.forClass(MemberBadge.class);
        verify(memberBadgeRepository).save(captor.capture());
        MemberBadge saved = captor.getValue();
        assertEquals(testMember, saved.getMember());
        assertEquals(badge, saved.getBadge());
        assertNotNull(saved.getAchievedAt());
    }

    @Test
    void checkAndAwardBadges_singleDistance_awards() {
        Badge badge = new Badge("5K 러너", "설명", null, ConditionType.SINGLE_DISTANCE, 5000.0);
        RunningSession session = new RunningSession(testMember);
        session.complete(LocalDateTime.now(), 5500.0, 1800L, 5.5);

        when(memberRepository.findById(1L)).thenReturn(Optional.of(testMember));
        when(badgeRepository.findAll()).thenReturn(List.of(badge));
        when(memberBadgeRepository.existsByMemberIdAndBadgeId(anyLong(), any())).thenReturn(false);

        badgeService.checkAndAwardBadges(1L, session);

        ArgumentCaptor<MemberBadge> captor = ArgumentCaptor.forClass(MemberBadge.class);
        verify(memberBadgeRepository).save(captor.capture());
        MemberBadge saved = captor.getValue();
        assertEquals(testMember, saved.getMember());
        assertEquals(badge, saved.getBadge());
    }

    @Test
    void checkAndAwardBadges_alreadyOwned_skips() {
        Badge badge = new Badge("첫 러닝", "설명", null, ConditionType.TOTAL_COUNT, 1.0);
        RunningSession session = new RunningSession(testMember);

        when(memberRepository.findById(1L)).thenReturn(Optional.of(testMember));
        when(badgeRepository.findAll()).thenReturn(List.of(badge));
        when(memberBadgeRepository.existsByMemberIdAndBadgeId(anyLong(), any())).thenReturn(true);

        badgeService.checkAndAwardBadges(1L, session);

        verify(memberBadgeRepository, never()).save(any(MemberBadge.class));
    }

    @Test
    void checkAndAwardBadges_conditionNotMet_doesNotAward() {
        Badge badge = new Badge("10K 러너", "설명", null, ConditionType.SINGLE_DISTANCE, 10000.0);
        RunningSession session = new RunningSession(testMember);
        session.complete(LocalDateTime.now(), 5000.0, 1800L, 6.0);

        when(memberRepository.findById(1L)).thenReturn(Optional.of(testMember));
        when(badgeRepository.findAll()).thenReturn(List.of(badge));
        when(memberBadgeRepository.existsByMemberIdAndBadgeId(anyLong(), any())).thenReturn(false);

        badgeService.checkAndAwardBadges(1L, session);

        verify(memberBadgeRepository, never()).save(any(MemberBadge.class));
    }
}
