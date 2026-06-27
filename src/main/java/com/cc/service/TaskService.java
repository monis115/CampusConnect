package com.cc.service;

import com.cc.modals.*;
import com.cc.repository.*;
import com.cc.service.engine.TaskAnalyticsEngine;
import com.cc.service.engine.TaskNotificationEngine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class TaskService {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private FollowUpRepository followUpRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private TaskHistoryRepository taskHistoryRepository;

    @Autowired
    private TaskRuleRepository taskRuleRepository;

    @Autowired
    private TaskNotificationEngine notificationEngine;

    @Autowired
    private TaskAnalyticsEngine analyticsEngine;

    private static final List<String> CLOSED_STATUSES = List.of("COMPLETED", "VERIFIED", "ARCHIVED");

    // ====================== EXISTING METHODS (PRESERVED) ======================

    public Task createTask(Task task) {
        // If student is passed only by ID from frontend
        if (task.getStudent() != null && task.getStudent().getId() != null) {
            Student student = studentRepository.findById(task.getStudent().getId())
                .orElseThrow(() -> new RuntimeException("Student not found"));
            task.setStudent(student);
        }
        Task saved = taskRepository.save(task);

        // Record history
        taskHistoryRepository.save(new TaskHistoryEntry(saved, "CREATED",
                task.getCreatedBy() != null ? task.getCreatedBy() : "MANUAL",
                "Task created: " + task.getTitle()));

        return saved;
    }

    public Task updateTaskStatus(Long id, String status) {
        Task task = taskRepository.findById(id).orElseThrow(() -> new RuntimeException("Task not found"));
        String oldStatus = task.getStatus();
        task.setStatus(status);

        if ("COMPLETED".equals(status)) {
            task.setClosedAt(LocalDateTime.now());
        }

        Task saved = taskRepository.save(task);

        // Record history
        taskHistoryRepository.save(new TaskHistoryEntry(saved, status,
                "USER", "Status changed: " + oldStatus + " → " + status));

        return saved;
    }

    public FollowUp addFollowUp(Long taskId, String message, String username) {
        Task task = taskRepository.findById(taskId).orElseThrow(() -> new RuntimeException("Task not found"));
        FollowUp followUp = new FollowUp();
        followUp.setTask(task);
        followUp.setMessage(message);
        followUp.setCreatedBy(username);
        return followUpRepository.save(followUp);
    }

    public Task escalateTask(Long taskId, String target) {
        Task task = taskRepository.findById(taskId).orElseThrow(() -> new RuntimeException("Task not found"));
        task.setEscalatedTo(target);
        Task saved = taskRepository.save(task);

        // Record history
        taskHistoryRepository.save(new TaskHistoryEntry(saved, "ESCALATED",
                "SYSTEM", "Escalated to: " + target));

        notificationEngine.notifyTaskEscalated(saved);
        return saved;
    }
    
    public Task addWatcher(Long taskId, String username) {
        Task task = taskRepository.findById(taskId).orElseThrow(() -> new RuntimeException("Task not found"));
        task.getWatchers().add(username);
        return taskRepository.save(task);
    }

    public Map<String, Object> getDashboardData(String role, String username) {
        if (username == null || username.isEmpty()) {
            return new HashMap<>(); // Fail safe
        }
        
        List<Task> visibleTasks = taskRepository.findEnterpriseDashboardTasks(username, role != null ? role : "");
        
        List<Task> myTasks = visibleTasks.stream()
            .filter(t -> !"COMPLETED".equals(t.getStatus()) && !"AWAITING_APPROVAL".equals(t.getStatus()))
            .toList();
            
        List<Task> pendingApproval = visibleTasks.stream()
            .filter(t -> "AWAITING_APPROVAL".equals(t.getStatus()))
            .toList();
            
        List<Task> overdueTasks = visibleTasks.stream()
            .filter(t -> t.getDueDate() != null && t.getDueDate().isBefore(LocalDate.now()) && !"COMPLETED".equals(t.getStatus()))
            .toList();

        Map<String, Object> data = new HashMap<>();
        data.put("allTasks", visibleTasks);
        data.put("myTasks", myTasks);
        data.put("pendingApproval", pendingApproval);
        data.put("overdueTasks", overdueTasks);

        return data;
    }

    public Map<String, Long> getDashboardCounts(String role, String username, String assignedBy, String assignedTo, String department) {
        if (username == null || username.isEmpty()) return new HashMap<>();
        String actualRole = role != null ? role : "";
        
        Map<String, Long> counts = new HashMap<>();
        counts.put("all", taskRepository.countEnterpriseDashboardTasks(username, actualRole, "all", assignedBy, assignedTo, department));
        counts.put("my", taskRepository.countEnterpriseDashboardTasks(username, actualRole, "my", assignedBy, assignedTo, department));
        counts.put("pendingApproval", taskRepository.countEnterpriseDashboardTasks(username, actualRole, "pending-approval", assignedBy, assignedTo, department));
        counts.put("overdue", taskRepository.countEnterpriseDashboardTasks(username, actualRole, "overdue", assignedBy, assignedTo, department));
        return counts;
    }

    public org.springframework.data.domain.Page<Task> getPaginatedTasks(String role, String username, String filter, int page, int size, String assignedBy, String assignedTo, String department) {
        if (username == null || username.isEmpty()) return org.springframework.data.domain.Page.empty();
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size, org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "createdAt"));
        return taskRepository.findEnterpriseDashboardTasksPaginated(username, role != null ? role : "", filter != null ? filter : "all", assignedBy, assignedTo, department, pageable);
    }

    public List<Map<String, Object>> searchStudentsForTask(String query, String role, String username) {
        if (query == null || query.trim().isEmpty() || role == null || username == null) {
            return List.of();
        }
        
        List<com.cc.modals.Student> students = List.of();
        
        switch (role.toUpperCase()) {
            case "ADMISSION_MANAGER":
                students = studentRepository.searchByAdmissionManager(username, query);
                break;
            case "VISA_MANAGER":
                students = studentRepository.searchByVisaMan(username, query);
                break;
            case "TELEX_MANAGER":
                students = studentRepository.searchByTelexMan(username, query);
                break;
            case "DOCUMENT_MANAGER":
                students = studentRepository.searchByDocPerson(username, query);
                break;
            case "ACCOUNTANT":
                students = studentRepository.searchByAccountant(username, query);
                break;
            case "UNIVERSITY_COORDINATOR":
                students = studentRepository.searchByUniversityPerson(username, query);
                break;
            case "AGENT":
                students = studentRepository.searchByAgent(username, query);
                break;
            default:
                break;
        }
        
        return students.stream()
            .limit(15)
            .map(s -> {
                Map<String, Object> map = new HashMap<>();
                map.put("id", s.getId());
                map.put("fullName", s.getFullName());
                map.put("applicationId", s.getApplicationId());
                return map;
            })
            .toList();
    }

    // ====================== NEW ENTERPRISE METHODS ======================

    /**
     * Intelligent Dashboard (Issue #9) — returns categorized task views.
     */
    public Map<String, Object> getIntelligentDashboard(String username, String role) {
        if (username == null || username.isEmpty()) return new HashMap<>();

        Map<String, Object> dashboard = new HashMap<>();
        LocalDate today = LocalDate.now();
        LocalDate weekEnd = today.plusDays(7);

        // My Tasks (assigned to me, active)
        dashboard.put("myTasks", taskRepository.findByAssignedToUserAndStatusNotIn(username, CLOSED_STATUSES));

        // Watching (I'm a watcher)
        List<Task> allVisible = taskRepository.findEnterpriseDashboardTasks(username, role != null ? role : "");
        dashboard.put("allTasks", allVisible);
        
        List<Task> watching = allVisible.stream()
                .filter(t -> t.getWatchers() != null && t.getWatchers().contains(username))
                .toList();
        dashboard.put("watching", watching);

        // Department Tasks
        if (role != null) {
            String dept = mapRoleToDepartment(role);
            if (dept != null) {
                dashboard.put("departmentTasks", taskRepository.findByDepartmentAndStatusNotIn(dept, CLOSED_STATUSES));
            }
        }

        // Today's Tasks
        dashboard.put("todaysTasks", taskRepository.findByDueDateAndStatusNot(today, "CLOSED"));

        // This Week
        dashboard.put("thisWeekTasks", taskRepository.findByDueDateBetweenAndStatusNot(today, weekEnd, "CLOSED"));

        // Overdue
        dashboard.put("overdueTasks", taskRepository.findOverdueTasks(today));

        // Escalated (tasks that have been escalated to me)
        List<Task> escalated = allVisible.stream()
                .filter(t -> username.equals(t.getEscalatedTo()))
                .toList();
        dashboard.put("escalatedToMe", escalated);

        // Awaiting approval (AM reviews all)
        if ("ADMISSION_MANAGER".equalsIgnoreCase(role)) {
            dashboard.put("awaitingApproval", taskRepository.findByStatus("AWAITING_APPROVAL"));
        } else {
            dashboard.put("awaitingApproval", taskRepository.findByAssignedToUserAndStatus(username, "AWAITING_APPROVAL"));
        }

        // Waiting For Me (My active tasks + rework required)
        List<Task> waitingForMe = taskRepository.findByAssignedToUserAndStatusNotIn(username, CLOSED_STATUSES);
        dashboard.put("waitingForMe", waitingForMe);

        // Completed
        List<Task> completed = allVisible.stream()
                .filter(t -> "CLOSED".equals(t.getStatus()))
                .toList();
        dashboard.put("completedTasks", completed);
        
        // Completed today
        List<Task> completedToday = completed.stream()
                .filter(t -> t.getClosedAt() != null && t.getClosedAt().toLocalDate().equals(today))
                .toList();
        dashboard.put("completedToday", completedToday);

        return dashboard;
    }

    /**
     * Verification-based closure (Issue #11).
     * Task goes COMPLETED → VERIFIED (by a supervisor).
     */
    public Task verifyTask(Long taskId, String verifierUsername) {
        Task task = taskRepository.findById(taskId).orElseThrow(() -> new RuntimeException("Task not found"));
        if (!"COMPLETED".equals(task.getStatus())) {
            throw new RuntimeException("Task must be COMPLETED before verification");
        }
        task.setStatus("VERIFIED");
        task.setVerifiedBy(verifierUsername);
        task.setVerifiedAt(LocalDateTime.now());
        Task saved = taskRepository.save(task);

        taskHistoryRepository.save(new TaskHistoryEntry(saved, "VERIFIED",
                verifierUsername, "Task verified by " + verifierUsername));

        return saved;
    }

    /**
     * Approval decision (Issue #17).
     */
    public Task approveTask(Long taskId, String decision, String comment, String username) {
        Task task = taskRepository.findById(taskId).orElseThrow(() -> new RuntimeException("Task not found"));
        task.setApprovalDecision(decision);
        task.setApprovalComment(comment);

        if ("APPROVED".equalsIgnoreCase(decision)) {
            task.setStatus("CLOSED");
            task.setClosedAt(LocalDateTime.now());
        } else if ("REJECTED".equalsIgnoreCase(decision)) {
            task.setStatus("PENDING");
        } else if ("RETURNED".equalsIgnoreCase(decision)) {
            task.setStatus("REWORK_REQUIRED");
            task.setReworkCount(task.getReworkCount() + 1);
        }

        Task saved = taskRepository.save(task);

        taskHistoryRepository.save(new TaskHistoryEntry(saved, "APPROVAL_" + decision.toUpperCase(),
                username, "Decision: " + decision + (comment != null && !comment.isEmpty() ? " | Comment: " + comment : "")));

        return saved;
    }

    /**
     * Team member marks task as done (Issue #1, #3).
     */
    public Task markCompletedByTeam(Long taskId, String username) {
        Task task = taskRepository.findById(taskId).orElseThrow(() -> new RuntimeException("Task not found"));
        
        task.setCompletedBy(username);
        task.setCompletedAt(LocalDateTime.now());
        
        if (task.getRequiresApproval() != null && task.getRequiresApproval()) {
            task.setStatus("AWAITING_APPROVAL");
            taskHistoryRepository.save(new TaskHistoryEntry(task, "COMPLETED_BY_TEAM",
                    username, "Marked as done, waiting for manager review"));
            notificationEngine.notifyTaskAwaitingApproval(task, username);
        } else {
            task.setStatus("CLOSED");
            task.setClosedAt(LocalDateTime.now());
            taskHistoryRepository.save(new TaskHistoryEntry(task, "CLOSED",
                    username, "Task completed (No approval required)"));
        }
        
        return taskRepository.save(task);
    }

    /**
     * AM returns task for rework (Issue #5).
     */
    public Task returnForRework(Long taskId, String username, String comment) {
        Task task = taskRepository.findById(taskId).orElseThrow(() -> new RuntimeException("Task not found"));
        
        task.setStatus("REWORK_REQUIRED");
        task.setReworkCount(task.getReworkCount() + 1);
        task.setApprovalComment(comment);
        
        Task saved = taskRepository.save(task);
        
        taskHistoryRepository.save(new TaskHistoryEntry(saved, "RETURNED_FOR_REWORK",
                username, "Returned for rework: " + comment));
                
        return saved;
    }

    /**
     * AM requests clarification without returning (Issue #4).
     */
    public Task requestClarification(Long taskId, String username, String message) {
        Task task = taskRepository.findById(taskId).orElseThrow(() -> new RuntimeException("Task not found"));
        
        task.setClarificationMessage(message);
        Task saved = taskRepository.save(task);
        
        addFollowUp(taskId, "Clarification requested: " + message, username);
        
        return saved;
    }

    /**
     * Bulk task creation from template (Issue #15).
     */
    public List<Task> createTasksFromTemplate(String title, String description, String department,
                                               String assignedToRole, String category, String priority,
                                               Integer slaDays, List<Long> studentIds, String createdBy) {
        List<Task> created = new ArrayList<>();
        for (Long studentId : studentIds) {
            try {
                Student student = studentRepository.findById(studentId).orElse(null);
                if (student == null) continue;

                Task task = new Task();
                task.setTitle(title);
                task.setDescription(description + " for " + student.getFullName());
                task.setDepartment(department);
                task.setAssignedToRole(assignedToRole);
                task.setStudent(student);
                task.setCategory(category);
                task.setPriority(priority != null ? priority : "MEDIUM");
                task.setDueDate(LocalDate.now().plusDays(slaDays != null ? slaDays : 3));
                task.setCreatedBy(createdBy);
                task.setType("TASK");
                task.setTemplateName(title);

                Task saved = taskRepository.save(task);
                taskHistoryRepository.save(new TaskHistoryEntry(saved, "CREATED",
                        createdBy, "Bulk created from template: " + title));
                created.add(saved);
            } catch (Exception e) {
                System.err.println("Failed to create template task for student " + studentId + ": " + e.getMessage());
            }
        }
        return created;
    }

    /**
     * Get full task history (Issue #13).
     */
    public List<TaskHistoryEntry> getTaskHistory(Long taskId) {
        return taskHistoryRepository.findByTaskIdOrderByCreatedAtAsc(taskId);
    }

    /**
     * Get analytics (Issue #10).
     */
    public Map<String, Object> getAnalytics() {
        return analyticsEngine.getAnalytics();
    }

    /**
     * Get workload distribution (Issue #14).
     */
    public Map<String, Long> getWorkloadDistribution() {
        return analyticsEngine.getWorkloadDistribution();
    }

    /**
     * Rich follow-up with communication type (Issue #6).
     */
    public FollowUp addRichFollowUp(Long taskId, String message, String username,
                                     String communicationType, String attachmentPath,
                                     String mentionedUsers, Boolean isInternal) {
        Task task = taskRepository.findById(taskId).orElseThrow(() -> new RuntimeException("Task not found"));
        FollowUp followUp = new FollowUp();
        followUp.setTask(task);
        followUp.setMessage(message);
        followUp.setCreatedBy(username);
        followUp.setCommunicationType(communicationType);
        followUp.setAttachmentPath(attachmentPath);
        followUp.setMentionedUsers(mentionedUsers);
        followUp.setIsInternal(isInternal != null ? isInternal : true);

        FollowUp saved = followUpRepository.save(followUp);

        taskHistoryRepository.save(new TaskHistoryEntry(task, "FOLLOW_UP",
                username, communicationType + ": " + message));

        return saved;
    }

    /**
     * Get all task rules (admin).
     */
    public List<TaskRule> getAllRules() {
        return taskRuleRepository.findAll();
    }

    /**
     * Create a new rule (admin).
     */
    public TaskRule createRule(TaskRule rule) {
        return taskRuleRepository.save(rule);
    }

    /**
     * Toggle rule enabled/disabled (admin).
     */
    public TaskRule toggleRule(Long ruleId) {
        TaskRule rule = taskRuleRepository.findById(ruleId)
                .orElseThrow(() -> new RuntimeException("Rule not found"));
        rule.setEnabled(!rule.getEnabled());
        return taskRuleRepository.save(rule);
    }

    /**
     * Get approval queue for a user.
     */
    public List<Task> getApprovalQueue(String username) {
        return taskRepository.findByAssignedToUserAndStatus(username, "AWAITING_APPROVAL");
    }

    // ====================== HELPER ======================

    private String mapRoleToDepartment(String role) {
        if (role == null) return null;
        switch (role.toUpperCase()) {
            case "ADMISSION_MANAGER": return "ADMISSION";
            case "VISA_MANAGER": return "VISA";
            case "TELEX_MANAGER": return "TELEX";
            case "DOCUMENT_MANAGER": return "DOCUMENT";
            case "ACCOUNTANT": return "ACCOUNTANT";
            case "UNIVERSITY_COORDINATOR": return "UNIVERSITY";
            case "AGENT": return "AGENT";
            default: return null;
        }
    }

    public TaskAnalyticsEngine getAnalyticsEngine() {
        return analyticsEngine;
    }

    public TaskRule updateRuleTiming(Long id, Integer creationDelayMinutes, Integer dueDelayMinutes) {
        TaskRule rule = taskRuleRepository.findById(id).orElseThrow(() -> new RuntimeException("Rule not found"));
        if (creationDelayMinutes != null) rule.setCreationDelayMinutes(creationDelayMinutes);
        if (dueDelayMinutes != null) rule.setDueDelayMinutes(dueDelayMinutes);
        return taskRuleRepository.save(rule);
    }

    public TaskRule updateRuleApprovalPolicy(Long id, Boolean requiresApproval) {
        TaskRule rule = taskRuleRepository.findById(id).orElseThrow(() -> new RuntimeException("Rule not found"));
        rule.setRequiresApproval(requiresApproval);
        return taskRuleRepository.save(rule);
    }

    public List<Task> bulkApproveTasks(List<Long> taskIds, String username) {
        List<Task> tasks = taskRepository.findAllById(taskIds);
        for (Task task : tasks) {
            if ("AWAITING_APPROVAL".equals(task.getStatus())) {
                task.setStatus("CLOSED");
                task.setApprovalDecision("APPROVED");
                task.setClosedAt(LocalDateTime.now());
                taskHistoryRepository.save(new TaskHistoryEntry(task, "CLOSED",
                        username, "Task verified and approved"));
            }
        }
        return taskRepository.saveAll(tasks);
    }

    public List<Task> bulkReturnTasks(List<Long> taskIds, String username, String reason) {
        List<Task> tasks = taskRepository.findAllById(taskIds);
        for (Task task : tasks) {
            if ("AWAITING_APPROVAL".equals(task.getStatus())) {
                task.setStatus("PENDING");
                task.setApprovalDecision("RETURNED");
                task.setApprovalComment(reason);
                task.setReworkCount(task.getReworkCount() != null ? task.getReworkCount() + 1 : 1);
                task.setCompletedBy(null);
                task.setCompletedAt(null);
                
                taskHistoryRepository.save(new TaskHistoryEntry(task, "RETURNED",
                        username, "Task returned for rework: " + reason));
            }
        }
        return taskRepository.saveAll(tasks);
    }
}
