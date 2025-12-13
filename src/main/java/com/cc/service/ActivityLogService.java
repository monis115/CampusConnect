package com.cc.service;

import com.cc.modals.ActivityLog;
import com.cc.repository.ActivityLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ActivityLogService {

    @Autowired
    private ActivityLogRepository activityLogRepository;

    public void logActivity(String action, String username, String role, String entityName, String details,
            String ipAddress) {
        try {
            ActivityLog log = new ActivityLog(action, username, role, entityName, details, ipAddress);
            activityLogRepository.save(log);
        } catch (Exception e) {
            // Silently fail logging to avoid disrupting business logic
            System.err.println("Failed to save activity log: " + e.getMessage());
        }
    }

    public List<ActivityLog> getAllLogs() {
        return activityLogRepository.findAll(Sort.by(Sort.Direction.DESC, "timestamp"));
    }

    public Page<ActivityLog> getLogsPaginated(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "timestamp"));
        return activityLogRepository.findAll(pageable);
    }
}
