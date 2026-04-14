package com.inseong.dallyrun.backend.service;

import com.inseong.dallyrun.backend.dto.response.BadgeResponse;
import com.inseong.dallyrun.backend.dto.response.MemberBadgeResponse;
import com.inseong.dallyrun.backend.entity.Badge;
import com.inseong.dallyrun.backend.entity.Member;
import com.inseong.dallyrun.backend.entity.MemberBadge;
import com.inseong.dallyrun.backend.entity.RunningSession;
import com.inseong.dallyrun.backend.exception.BusinessException;
import com.inseong.dallyrun.backend.exception.ErrorCode;
import com.inseong.dallyrun.backend.repository.BadgeRepository;
import com.inseong.dallyrun.backend.repository.MemberBadgeRepository;
import com.inseong.dallyrun.backend.repository.MemberRepository;
import com.inseong.dallyrun.backend.repository.RunningSessionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional
public class BadgeServiceImpl implements BadgeService {

    private final BadgeRepository badgeRepository;
    private final MemberBadgeRepository memberBadgeRepository;
    private final MemberRepository memberRepository;
    private final RunningSessionRepository runningSessionRepository;

    public BadgeServiceImpl(BadgeRepository badgeRepository,
                            MemberBadgeRepository memberBadgeRepository,
                            MemberRepository memberRepository,
                            RunningSessionRepository runningSessionRepository) {
        this.badgeRepository = badgeRepository;
        this.memberBadgeRepository = memberBadgeRepository;
        this.memberRepository = memberRepository;
        this.runningSessionRepository = runningSessionRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<BadgeResponse> getAllBadges() {
        return badgeRepository.findAll().stream()
                .map(BadgeResponse::from)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<MemberBadgeResponse> getMemberBadges(Long memberId) {
        return memberBadgeRepository.findByMemberId(memberId).stream()
                .map(MemberBadgeResponse::from)
                .toList();
    }

    @Override
    public void checkAndAwardBadges(Long memberId, RunningSession session) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        List<Badge> allBadges = badgeRepository.findAll();

        for (Badge badge : allBadges) {
            if (memberBadgeRepository.existsByMemberIdAndBadgeId(memberId, badge.getId())) {
                continue;
            }

            boolean earned = switch (badge.getConditionType()) {
                case TOTAL_COUNT -> {
                    long count = runningSessionRepository.countCompletedByMemberId(memberId);
                    yield count >= badge.getConditionValue();
                }
                case TOTAL_DISTANCE -> {
                    double distance = runningSessionRepository.sumDistanceByMemberId(memberId);
                    yield distance >= badge.getConditionValue();
                }
                case SINGLE_DISTANCE -> {
                    yield session.getDistanceMeters() != null
                            && session.getDistanceMeters() >= badge.getConditionValue();
                }
                case STREAK_DAYS -> {
                    int streak = calculateStreak(memberId);
                    yield streak >= badge.getConditionValue();
                }
            };

            if (earned) {
                memberBadgeRepository.save(new MemberBadge(member, badge));
            }
        }
    }

    private int calculateStreak(Long memberId) {
        List<LocalDate> runDates = runningSessionRepository.findDistinctRunDates(memberId);
        if (runDates.isEmpty()) {
            return 0;
        }

        int streak = 1;
        for (int i = 0; i < runDates.size() - 1; i++) {
            if (runDates.get(i).minusDays(1).equals(runDates.get(i + 1))) {
                streak++;
            } else {
                break;
            }
        }
        return streak;
    }
}
