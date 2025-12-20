package com.cc.repository;

import com.cc.modals.ActivityLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ActivityLogRepository extends JpaRepository<ActivityLog, Long> {
    // Basic finding methods if needed
    List<ActivityLog> findByUsername(String username);

    List<ActivityLog> findByAction(String action);

    @org.springframework.data.jpa.repository.Query("SELECT new com.cc.DTO.DailyActivityDTO(CAST(a.timestamp AS LocalDate), COUNT(a)) "
            +
            "FROM ActivityLog a " +
            "WHERE a.username = :username " +
            "AND a.timestamp BETWEEN :startDate AND :endDate " +
            "GROUP BY CAST(a.timestamp AS LocalDate) " +
            "ORDER BY CAST(a.timestamp AS LocalDate) ASC")
    List<com.cc.DTO.DailyActivityDTO> findDailyActivityCountByUsernameAndDateRange(
            @org.springframework.data.repository.query.Param("username") String username,
            @org.springframework.data.repository.query.Param("startDate") java.time.LocalDateTime startDate,
            @org.springframework.data.repository.query.Param("endDate") java.time.LocalDateTime endDate);
}
