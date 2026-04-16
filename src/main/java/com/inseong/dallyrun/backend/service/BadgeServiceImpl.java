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
import java.util.Set;

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

    /**
     * 러닝 세션 완료 시 배지 수여 조건을 평가한다.
     *
     * <p>전체 배지를 순회하며 아직 미획득인 배지에 대해 conditionType별로 달성 여부를 판단한다:
     * <ul>
     *   <li>TOTAL_COUNT — 누적 완료 세션 수</li>
     *   <li>TOTAL_DISTANCE — 누적 총 거리(m)</li>
     *   <li>SINGLE_DISTANCE — 현재 세션의 단일 거리(m)</li>
     *   <li>STREAK_DAYS — 연속 러닝 일수</li>
     * </ul>
     */
    @Override
    public void checkAndAwardBadges(Long memberId, RunningSession session) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        List<Badge> allBadges = badgeRepository.findAll();
        // N+1 방지: 획득한 배지 ID 집합을 한 번에 조회
        Set<Long> ownedBadgeIds = memberBadgeRepository.findBadgeIdsByMemberId(memberId);

        for (Badge badge : allBadges) {
            // 이미 획득한 배지는 건너뛴다
            if (ownedBadgeIds.contains(badge.getId())) {
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

    /**
     * 최근 러닝 날짜 기준으로 연속 러닝 일수를 계산한다.
     * 날짜 리스트는 최신순(내림차순) 정렬을 전제하며, 하루 간격이 끊기는 시점까지 카운트한다.
     */
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
