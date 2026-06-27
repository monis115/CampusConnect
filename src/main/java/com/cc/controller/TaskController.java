package com.cc.controller;

import com.cc.modals.FollowUp;
import com.cc.modals.Task;
import com.cc.modals.TaskHistoryEntry;
import com.cc.modals.TaskRule;
import com.cc.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    @Autowired
    private TaskService taskService;

    @Autowired
    private com.cc.service.SystemSettingService systemSettingService;

    // ====================== SETTINGS ENDPOINTS ======================

    @GetMapping("/settings/sync-interval")
    public ResponseEntity<Map<String, Integer>> getSyncInterval() {
        return ResponseEntity.ok(Map.of("minutes", systemSettingService.getTaskSyncIntervalMinutes()));
    }

    @PutMapping("/settings/sync-interval")
    public ResponseEntity<Map<String, String>> updateSyncInterval(@RequestBody Map<String, Integer> payload) {
        int minutes = payload.getOrDefault("minutes", 15);
        systemSettingService.setTaskSyncIntervalMinutes(minutes);
        return ResponseEntity.ok(Map.of("message", "Sync interval updated to " + minutes + " minutes."));
    }

    // ====================== EXISTING ENDPOINTS (PRESERVED) ======================

    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getDashboardData(
            @RequestParam(required = false) String role,
            @RequestParam(required = true) String username) {
        return ResponseEntity.ok(taskService.getDashboardData(role, username));
    }

    @GetMapping("/dashboard/counts")
    public ResponseEntity<Map<String, Long>> getDashboardCounts(
            @RequestParam(required = false) String role,
            @RequestParam(required = true) String username,
            @RequestParam(required = false) String assignedBy,
            @RequestParam(required = false) String assignedTo,
            @RequestParam(required = false) String department) {
        return ResponseEntity.ok(taskService.getDashboardCounts(role, username, assignedBy, assignedTo, department));
    }

    @GetMapping("/dashboard/paginated")
    public ResponseEntity<org.springframework.data.domain.Page<Task>> getPaginatedTasks(
            @RequestParam(required = false) String role,
            @RequestParam(required = true) String username,
            @RequestParam(defaultValue = "all") String filter,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(required = false) String assignedBy,
            @RequestParam(required = false) String assignedTo,
            @RequestParam(required = false) String department) {
        return ResponseEntity.ok(taskService.getPaginatedTasks(role, username, filter, page, size, assignedBy, assignedTo, department));
    }

    @PostMapping
    public ResponseEntity<Task> createTask(@RequestBody Task task) {
        return ResponseEntity.ok(taskService.createTask(task));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<Task> updateTaskStatus(@PathVariable Long id, @RequestParam String status) {
        return ResponseEntity.ok(taskService.updateTaskStatus(id, status));
    }

    // Assignee marks task as done (New Enterprise Workflow)
    @PatchMapping("/{id}/complete")
    public ResponseEntity<Task> markCompletedByTeam(
            @PathVariable Long id,
            @RequestParam String username) {
        return ResponseEntity.ok(taskService.markCompletedByTeam(id, username));
    }

    // Assignee requests approval (Legacy - keep for compatibility)
    @PatchMapping("/{id}/request-approval")
    public ResponseEntity<Task> requestApproval(@PathVariable Long id) {
        return ResponseEntity.ok(taskService.updateTaskStatus(id, "AWAITING_APPROVAL"));
    }

    // Creator/Admission Manager approves the completed task
    @PatchMapping("/{id}/approve")
    public ResponseEntity<Task> approveTask(@PathVariable Long id) {
        return ResponseEntity.ok(taskService.updateTaskStatus(id, "CLOSED"));
    }

    // Creator/Admission Manager rejects (Legacy)
    @PatchMapping("/{id}/reject")
    public ResponseEntity<Task> rejectTask(@PathVariable Long id) {
        return ResponseEntity.ok(taskService.updateTaskStatus(id, "PENDING"));
    }
    
    // AM returns task for rework (Enterprise Workflow)
    @PatchMapping("/{id}/return")
    public ResponseEntity<Task> returnForRework(
            @PathVariable Long id,
            @RequestParam String username,
            @RequestParam String comment) {
        return ResponseEntity.ok(taskService.returnForRework(id, username, comment));
    }
    
    // AM requests clarification (Enterprise Workflow)
    @PatchMapping("/{id}/request-clarification")
    public ResponseEntity<Task> requestClarification(
            @PathVariable Long id,
            @RequestParam String username,
            @RequestParam String message) {
        return ResponseEntity.ok(taskService.requestClarification(id, username, message));
    }

    @GetMapping("/search-students")
    public ResponseEntity<List<Map<String, Object>>> searchStudents(
            @RequestParam String query,
            @RequestParam String role,
            @RequestParam String username) {
        return ResponseEntity.ok(taskService.searchStudentsForTask(query, role, username));
    }
    
    // Follow up logic
    @PostMapping("/{id}/follow-ups")
    public ResponseEntity<FollowUp> addFollowUp(
            @PathVariable Long id, 
            @RequestParam String message, 
            @RequestParam String username) {
        return ResponseEntity.ok(taskService.addFollowUp(id, message, username));
    }
    
    // Escalation logic
    @PatchMapping("/{id}/escalate")
    public ResponseEntity<Task> escalateTask(
            @PathVariable Long id, 
            @RequestParam String target) {
        return ResponseEntity.ok(taskService.escalateTask(id, target));
    }

    // ====================== NEW ENTERPRISE ENDPOINTS ======================

    /**
     * Intelligent Dashboard — returns all categorized task views.
     */
    @GetMapping("/dashboard/intelligent")
    public ResponseEntity<Map<String, Object>> getIntelligentDashboard(
            @RequestParam String username,
            @RequestParam(required = false) String role) {
        return ResponseEntity.ok(taskService.getIntelligentDashboard(username, role));
    }

    /**
     * Full task audit history.
     */
    @GetMapping("/{id}/history")
    public ResponseEntity<List<TaskHistoryEntry>> getTaskHistory(@PathVariable Long id) {
        return ResponseEntity.ok(taskService.getTaskHistory(id));
    }

    /**
     * Operational analytics and metrics.
     */
    @GetMapping("/analytics")
    public ResponseEntity<Map<String, Object>> getAnalytics() {
        return ResponseEntity.ok(taskService.getAnalytics());
    }

    /**
     * Enterprise reports for Admission Manager
     */
    @GetMapping("/analytics/reports")
    public ResponseEntity<Map<String, Object>> getEnterpriseReports() {
        return ResponseEntity.ok(taskService.getAnalyticsEngine().getEnterpriseReports());
    }

    /**
     * Workload distribution across users.
     */
    @GetMapping("/workload")
    public ResponseEntity<Map<String, Long>> getWorkloadDistribution() {
        return ResponseEntity.ok(taskService.getWorkloadDistribution());
    }

    /**
     * Verification step — supervisor verifies a completed task.
     */
    @PostMapping("/{id}/verify")
    public ResponseEntity<Task> verifyTask(
            @PathVariable Long id,
            @RequestParam String username) {
        return ResponseEntity.ok(taskService.verifyTask(id, username));
    }

    /**
     * Approval decision with comment.
     */
    @PostMapping("/{id}/approval-decision")
    public ResponseEntity<Task> approvalDecision(
            @PathVariable Long id,
            @RequestParam String decision,
            @RequestParam(required = false) String comment,
            @RequestParam String username) {
        return ResponseEntity.ok(taskService.approveTask(id, decision, comment, username));
    }

    /**
     * Get pending approvals for a user.
     */
    @GetMapping("/approval-queue")
    public ResponseEntity<List<Task>> getApprovalQueue(@RequestParam String username) {
        return ResponseEntity.ok(taskService.getApprovalQueue(username));
    }

    /**
     * Bulk create tasks from template.
     */
    @PostMapping("/bulk-create")
    public ResponseEntity<List<Task>> bulkCreateTasks(@RequestBody Map<String, Object> request) {
        String title = (String) request.get("title");
        String description = (String) request.get("description");
        String department = (String) request.get("department");
        String assignedToRole = (String) request.get("assignedToRole");
        String category = (String) request.get("category");
        String priority = (String) request.get("priority");
        Integer slaDays = request.get("slaDays") != null ? (Integer) request.get("slaDays") : 3;
        String createdBy = (String) request.get("createdBy");

        @SuppressWarnings("unchecked")
        List<Integer> studentIdInts = (List<Integer>) request.get("studentIds");
        List<Long> studentIds = studentIdInts.stream().map(Integer::longValue).toList();

        return ResponseEntity.ok(taskService.createTasksFromTemplate(
                title, description, department, assignedToRole, category, priority, slaDays, studentIds, createdBy));
    }

    /**
     * Rich follow-up with communication type and attachments.
     */
    @PostMapping("/{id}/rich-follow-up")
    public ResponseEntity<FollowUp> addRichFollowUp(
            @PathVariable Long id,
            @RequestParam String message,
            @RequestParam String username,
            @RequestParam(required = false) String communicationType,
            @RequestParam(required = false) String attachmentPath,
            @RequestParam(required = false) String mentionedUsers,
            @RequestParam(required = false) Boolean isInternal) {
        return ResponseEntity.ok(taskService.addRichFollowUp(
                id, message, username, communicationType, attachmentPath, mentionedUsers, isInternal));
    }

    // ====================== RULE MANAGEMENT ENDPOINTS ======================

    /**
     * List all task rules (admin).
     */
    @GetMapping("/rules")
    public ResponseEntity<List<TaskRule>> getAllRules() {
        return ResponseEntity.ok(taskService.getAllRules());
    }

    /**
     * Create a new rule (admin).
     */
    @PostMapping("/rules")
    public ResponseEntity<TaskRule> createRule(@RequestBody TaskRule rule) {
        return ResponseEntity.ok(taskService.createRule(rule));
    }

    /**
     * Toggle a rule on/off (admin).
     */
    @PutMapping("/rules/{id}/toggle")
    public ResponseEntity<TaskRule> toggleRule(@PathVariable Long id) {
        return ResponseEntity.ok(taskService.toggleRule(id));
    }

    /**
     * Update task creation delay and SLA due delay (Admin/Admission Manager).
     */
    @PatchMapping("/rules/{id}/timing")
    public ResponseEntity<TaskRule> updateRuleTiming(
            @PathVariable Long id,
            @RequestBody Map<String, Integer> payload) {
        return ResponseEntity.ok(taskService.updateRuleTiming(
                id, 
                payload.get("creationDelayMinutes"), 
                payload.get("dueDelayMinutes")));
    }

    /**
     * Update task approval policy (Admission Manager).
     */
    @PatchMapping("/rules/{id}/approval")
    public ResponseEntity<TaskRule> updateRuleApprovalPolicy(
            @PathVariable Long id,
            @RequestBody Map<String, Boolean> payload) {
        return ResponseEntity.ok(taskService.updateRuleApprovalPolicy(
                id, 
                payload.get("requiresApproval")));
    }

    /**
     * Bulk approve tasks (Admission Manager).
     */
    @PostMapping("/bulk-approve")
    public ResponseEntity<List<Task>> bulkApproveTasks(
            @RequestParam String username,
            @RequestBody List<Long> taskIds) {
        return ResponseEntity.ok(taskService.bulkApproveTasks(taskIds, username));
    }

    /**
     * Bulk return tasks for rework (Admission Manager).
     */
    @PostMapping("/bulk-return")
    public ResponseEntity<List<Task>> bulkReturnTasks(
            @RequestParam String username,
            @RequestBody Map<String, Object> payload) {
        List<Long> taskIds = (List<Long>) payload.get("taskIds");
        String reason = (String) payload.get("reason");
        return ResponseEntity.ok(taskService.bulkReturnTasks(taskIds, username, reason));
    }
}
