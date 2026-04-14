package com.inseong.dallyrun.backend.repository;

import com.inseong.dallyrun.backend.entity.MemberBadge;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MemberBadgeRepository extends JpaRepository<MemberBadge, Long> {

    List<MemberBadge> findByMemberId(Long memberId);

    boolean existsByMemberIdAndBadgeId(Long memberId, Long badgeId);
}
