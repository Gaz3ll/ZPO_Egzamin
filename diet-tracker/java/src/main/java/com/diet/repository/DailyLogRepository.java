package com.diet.repository;

import com.diet.model.DailyLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DailyLogRepository extends JpaRepository<DailyLog, Long> {
    List<DailyLog> findByUserIdAndDate(String userId, String date);
}
