package com.inseong.dallyrun.backend.repository;

import com.inseong.dallyrun.backend.entity.GpsPoint;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GpsPointRepository extends JpaRepository<GpsPoint, Long> {

    List<GpsPoint> findByRunningSessionIdOrderBySequenceIndexAsc(Long runningSessionId);

    void deleteByRunningSessionId(Long runningSessionId);
}
