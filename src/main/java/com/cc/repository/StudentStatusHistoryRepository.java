package com.cc.repository;

import com.cc.modals.StudentStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface StudentStatusHistoryRepository extends JpaRepository<StudentStatusHistory, Long> {
    List<StudentStatusHistory> findByStudentId(Long studentId);

    @org.springframework.data.jpa.repository.Query("SELECT h FROM StudentStatusHistory h WHERE h.student.agent.id = :agentId ORDER BY h.updatedAt DESC")
    List<StudentStatusHistory> findRecentActivitiesByAgentId(
            @org.springframework.data.repository.query.Param("agentId") Long agentId,
            org.springframework.data.domain.Pageable pageable);

    @org.springframework.data.jpa.repository.Query("SELECT h FROM StudentStatusHistory h WHERE h.student.agent.userId = :username ORDER BY h.updatedAt DESC")
    List<StudentStatusHistory> findRecentActivitiesByAgentUsername(
            @org.springframework.data.repository.query.Param("username") String username,
            org.springframework.data.domain.Pageable pageable);
}
