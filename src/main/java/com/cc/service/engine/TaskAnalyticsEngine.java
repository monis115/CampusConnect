package com.cc.service.engine;

import com.cc.repository.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Provides operational metrics and analytics for the command center dashboard.
 */
@Service
public class TaskAnalyticsEngine {

    @Autowired
    private TaskRepository taskRepository;

    /**
     * Returns comprehensive analytics data for the operations dashboard.
     */
    public Map<String, Object> getAnalytics() {
        Map<String, Object> analytics = new HashMap<>();

        // Open tasks by department
        List<Object[]> deptCounts = taskRepository.countOpenTasksByDepartment();
        Map<String, Long> openByDept = new HashMap<>();
        for (Object[] row : deptCounts) {
            openByDept.put(row[0] != null ? row[0].toString() : "UNKNOWN", (Long) row[1]);
        }
        analytics.put("openTasksByDepartment", openByDept);

        // Workload distribution
        List<Object[]> workload = taskRepository.findWorkloadDistribution();
        Map<String, Long> workloadMap = new HashMap<>();
        for (Object[] row : workload) {
            workloadMap.put(row[0] != null ? row[0].toString() : "UNASSIGNED", (Long) row[1]);
        }
        analytics.put("workloadDistribution", workloadMap);

        // Overdue count
        long overdueCount = taskRepository.findOverdueTasks(java.time.LocalDate.now()).size();
        analytics.put("overdueCount", overdueCount);

        // Total active tasks
        long activeCount = taskRepository.findAllActiveTasks().size();
        analytics.put("activeTaskCount", activeCount);

        return analytics;
    }

    /**
     * Returns workload distribution data for workload balancing visibility.
     */
    public Map<String, Long> getWorkloadDistribution() {
        List<Object[]> workload = taskRepository.findWorkloadDistribution();
        Map<String, Long> workloadMap = new HashMap<>();
        for (Object[] row : workload) {
            workloadMap.put(row[0] != null ? row[0].toString() : "UNASSIGNED", (Long) row[1]);
        }
        return workloadMap;
    }

    public Map<String, Object> getEnterpriseReports() {
        Map<String, Object> reports = new HashMap<>();
        
        java.time.LocalDateTime thirtyDaysAgo = java.time.LocalDateTime.now().minusDays(30);
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        
        List<com.cc.modals.Task> recentCompleted = taskRepository.findClosedTasksInRange(thirtyDaysAgo, now);
        List<com.cc.modals.Task> allActive = taskRepository.findAllActiveTasks();
        
        // 1. Average Resolution Time (in hours)
        double avgResolutionHours = 0;
        if (!recentCompleted.isEmpty()) {
            long totalMinutes = 0;
            for (com.cc.modals.Task t : recentCompleted) {
                if (t.getCreatedAt() != null && t.getClosedAt() != null) {
                    totalMinutes += java.time.Duration.between(t.getCreatedAt(), t.getClosedAt()).toMinutes();
                }
            }
            avgResolutionHours = (double) totalMinutes / 60.0 / recentCompleted.size();
        }
        reports.put("averageResolutionHours", Math.round(avgResolutionHours * 10.0) / 10.0);
        
        // 2. SLA Compliance (%)
        int withinSlaCount = 0;
        for (com.cc.modals.Task t : recentCompleted) {
            if (t.getDueDate() != null && t.getClosedAt() != null) {
                if (!t.getClosedAt().toLocalDate().isAfter(t.getDueDate())) {
                    withinSlaCount++;
                }
            }
        }
        double slaCompliance = recentCompleted.isEmpty() ? 100.0 : ((double) withinSlaCount / recentCompleted.size()) * 100.0;
        reports.put("slaCompliancePercent", Math.round(slaCompliance));
        
        // 3. Escalation Rate (%)
        long escalatedCount = allActive.stream().filter(t -> t.getEscalatedTo() != null).count();
        double escalationRate = allActive.isEmpty() ? 0.0 : ((double) escalatedCount / allActive.size()) * 100.0;
        reports.put("escalationRatePercent", Math.round(escalationRate));
        
        // 4. Rework Rate (%)
        long reworkCount = allActive.stream().filter(t -> t.getReworkCount() != null && t.getReworkCount() > 0).count();
        double reworkRate = allActive.isEmpty() ? 0.0 : ((double) reworkCount / allActive.size()) * 100.0;
        reports.put("reworkRatePercent", Math.round(reworkRate));
        
        long pendingApprovals = allActive.stream().filter(t -> "AWAITING_APPROVAL".equals(t.getStatus())).count();
        reports.put("pendingApprovals", pendingApprovals);
        
        // Include basic analytics as well
        reports.putAll(getAnalytics());
        
        // 6. Tasks closed in last 30 days
        reports.put("completedLast30Days", recentCompleted.size());
        
        return reports;
    }
}
