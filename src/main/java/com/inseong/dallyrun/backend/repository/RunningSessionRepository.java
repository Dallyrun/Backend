package com.inseong.dallyrun.backend.repository;

import com.inseong.dallyrun.backend.entity.RunningSession;
import com.inseong.dallyrun.backend.entity.enums.SessionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface RunningSessionRepository extends JpaRepository<RunningSession, Long> {

    Optional<RunningSession> findByMemberIdAndStatus(Long memberId, SessionStatus status);

    Page<RunningSession> findByMemberIdOrderByStartedAtDesc(Long memberId, Pageable pageable);

    @Query("SELECT COUNT(rs) FROM RunningSession rs WHERE rs.member.id = :memberId " +
            "AND rs.status = 'COMPLETED'")
    long countCompletedByMemberId(@Param("memberId") Long memberId);

    @Query("SELECT COALESCE(SUM(rs.distanceMeters), 0) FROM RunningSession rs " +
            "WHERE rs.member.id = :memberId AND rs.status = 'COMPLETED'")
    double sumDistanceByMemberId(@Param("memberId") Long memberId);

    @Query("SELECT COALESCE(SUM(rs.distanceMeters), 0) FROM RunningSession rs " +
            "WHERE rs.member.id = :memberId AND rs.status = 'COMPLETED' " +
            "AND rs.startedAt BETWEEN :start AND :end")
    double sumDistanceBetween(@Param("memberId") Long memberId,
                              @Param("start") LocalDateTime start,
                              @Param("end") LocalDateTime end);

    @Query("SELECT COALESCE(SUM(rs.durationSeconds), 0) FROM RunningSession rs " +
            "WHERE rs.member.id = :memberId AND rs.status = 'COMPLETED' " +
            "AND rs.startedAt BETWEEN :start AND :end")
    long sumDurationBetween(@Param("memberId") Long memberId,
                            @Param("start") LocalDateTime start,
                            @Param("end") LocalDateTime end);

    @Query("SELECT COUNT(rs) FROM RunningSession rs " +
            "WHERE rs.member.id = :memberId AND rs.status = 'COMPLETED' " +
            "AND rs.startedAt BETWEEN :start AND :end")
    long countCompletedBetween(@Param("memberId") Long memberId,
                               @Param("start") LocalDateTime start,
                               @Param("end") LocalDateTime end);

    @Query("SELECT DISTINCT CAST(rs.startedAt AS localdate) FROM RunningSession rs " +
            "WHERE rs.member.id = :memberId AND rs.status = 'COMPLETED' " +
            "ORDER BY CAST(rs.startedAt AS localdate) DESC")
    List<LocalDate> findDistinctRunDates(@Param("memberId") Long memberId);

    /**
     * 시작 시각이 새벽(04:00~06:59) 인 완료 세션 수.
     */
    @Query("SELECT COUNT(rs) FROM RunningSession rs " +
            "WHERE rs.member.id = :memberId AND rs.status = 'COMPLETED' " +
            "AND EXTRACT(HOUR FROM rs.startedAt) BETWEEN 4 AND 6")
    long countCompletedInEarlyMorning(@Param("memberId") Long memberId);

    /**
     * 시작 시각이 심야(22:00~03:59 다음날) 인 완료 세션 수.
     */
    @Query("SELECT COUNT(rs) FROM RunningSession rs " +
            "WHERE rs.member.id = :memberId AND rs.status = 'COMPLETED' " +
            "AND (EXTRACT(HOUR FROM rs.startedAt) >= 22 OR EXTRACT(HOUR FROM rs.startedAt) <= 3)")
    long countCompletedInLateNight(@Param("memberId") Long memberId);
}
