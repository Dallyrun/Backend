package com.inseong.dallyrun.backend.repository;

import com.inseong.dallyrun.backend.entity.MemberBadge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Set;

public interface MemberBadgeRepository extends JpaRepository<MemberBadge, Long> {

    List<MemberBadge> findByMemberId(Long memberId);

    boolean existsByMemberIdAndBadgeId(Long memberId, Long badgeId);

    /**
     * 배지 판정 시 N+1 방지를 위해 특정 회원이 이미 획득한 배지 ID 집합을 한 번에 조회한다.
     */
    @Query("SELECT mb.badge.id FROM MemberBadge mb WHERE mb.member.id = :memberId")
    Set<Long> findBadgeIdsByMemberId(@Param("memberId") Long memberId);
}
