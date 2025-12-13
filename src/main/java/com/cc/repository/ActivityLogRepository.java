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
}
