package com.inseong.dallyrun.backend.repository;

import com.inseong.dallyrun.backend.entity.Goal;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GoalRepository extends JpaRepository<Goal, Long> {

    List<Goal> findByMemberIdAndActiveTrue(Long memberId);
}
