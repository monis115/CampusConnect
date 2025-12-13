package com.cc.controller;

import com.cc.modals.ActivityLog;
import com.cc.service.ActivityLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/activity-logs")
@CrossOrigin(origins = "*")
public class ActivityLogController {

    @Autowired
    private ActivityLogService activityLogService;

    // Get paginated logs
    @GetMapping
    public ResponseEntity<Page<ActivityLog>> getActivityLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<ActivityLog> logs = activityLogService.getLogsPaginated(page, size);
        return ResponseEntity.ok(logs);
    }

    // Get all logs (use carefully)
    @GetMapping("/all")
    public ResponseEntity<List<ActivityLog>> getAllActivityLogs() {
        return ResponseEntity.ok(activityLogService.getAllLogs());
    }
}
