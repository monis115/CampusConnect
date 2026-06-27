package com.cc.service.engine.generators;

import com.cc.modals.Student;
import com.cc.modals.Task;
import com.cc.modals.TaskHistoryEntry;
import com.cc.modals.TaskRule;
import com.cc.repository.TaskHistoryRepository;
import com.cc.repository.TaskRepository;
import com.cc.service.engine.TaskNotificationEngine;
import com.cc.service.engine.TaskOwnerResolver;
import com.cc.service.engine.TaskRuleEvaluator;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

/**
 * Base class for all department task generators.
 * Contains shared logic for rule evaluation, task creation, and auto-closure.
 */
public abstract class BaseTaskGenerator implements TaskGenerator {

    @Autowired
    protected TaskRepository taskRepository;

    @Autowired
    protected TaskHistoryRepository taskHistoryRepository;

    @Autowired
    protected TaskRuleEvaluator ruleEvaluator;

    @Autowired
    protected TaskOwnerResolver ownerResolver;

    @Autowired
    protected TaskNotificationEngine notificationEngine;

    @Override
    public void evaluate(Student student, List<TaskRule> departmentRules) {
        for (TaskRule rule : departmentRules) {
            try {
                processRule(rule, student);
            } catch (Exception e) {
                System.err.println("[" + getDepartment() + "] Rule " + rule.getRuleCode()
                        + " failed for student " + student.getId() + ": " + e.getMessage());
            }
        }
    }

    /**
     * Workflow Stage Gate: Returns true if the student has reached the operational stage
     * where this department should begin working. Subclasses should override this.
     */
    protected boolean isStageActive(Student student) {
        return true; // Default to always active
    }

    protected void processRule(TaskRule rule, Student student) {
        // Check auto-close first
        if (ruleEvaluator.shouldCloseTask(rule, student)) {
            closeTasksForRule(student.getId(), rule.getRuleCode());
            return;
        }

        // Check if task should be created
        if (ruleEvaluator.shouldCreateTask(rule, student)) {
            // Critical Architecture Rule: Stage Progression
            if (!isStageActive(student)) {
                return;
            }

            // Check prerequisite
            if (rule.getPrerequisiteRuleCode() != null && !rule.getPrerequisiteRuleCode().isEmpty()) {
                List<Task> prereqTasks = taskRepository.findActiveByStudentAndRuleCode(
                        student.getId(), rule.getPrerequisiteRuleCode());
                if (!prereqTasks.isEmpty()) {
                    return; // Prerequisite task still active — blocked
                }
            }

            createTaskIfNotExists(rule, student);
        }
    }

    protected void createTaskIfNotExists(TaskRule rule, Student student) {
        // Duplicate prevention
        List<Task> existing = taskRepository.findActiveByStudentAndRuleCode(student.getId(), rule.getRuleCode());
        if (!existing.isEmpty()) return;

        // Resolve exact owner
        String ownerUsername = ownerResolver.resolveOwner(student, rule.getOwnerRole());
        
        // Critical Architecture Rule: Workflow-Aware Generation
        // Do not generate tasks if there is no valid owner. 
        // This ensures tasks only appear when the student has reached that operational stage.
        if (ownerUsername == null || ownerUsername.trim().isEmpty() || ownerUsername.equals("-")) {
            return;
        }

        // Resolve watchers
        Set<String> watchers = ownerResolver.resolveWatchers(student, rule.getWatcherRoles());

        // Create task
        Task task = new Task();
        task.setTitle(rule.getTaskTitle());
        task.setDescription(rule.getDescription() != null
                ? rule.getDescription().replace("{studentName}", student.getFullName())
                : rule.getTaskTitle() + " for " + student.getFullName());
        task.setDepartment(rule.getDepartment());
        task.setAssignedToRole(rule.getOwnerRole());
        task.setAssignedToUser(ownerUsername);
        task.setStudent(student);
        task.setCategory(rule.getCategory());
        task.setPriority(rule.getPriority() != null ? rule.getPriority() : "MEDIUM");
        task.setSystemGenerated(true);
        task.setCreatedBy("SYSTEM");
        task.setType("TASK");
        
        Integer delay = rule.getCreationDelayMinutes() != null ? rule.getCreationDelayMinutes() : 5;
        if (delay > 0) {
            task.setStatus("SCHEDULED");
            task.setScheduledFor(LocalDateTime.now().plusMinutes(delay));
        } else {
            task.setStatus("PENDING");
            Integer dueDelay = rule.getDueDelayMinutes() != null ? rule.getDueDelayMinutes() : 4320;
            task.setDueDate(LocalDate.now().plusDays(dueDelay / 1440));
        }
        
        task.setRuleCode(rule.getRuleCode());
        task.setWatchers(watchers);
        task.setRequiresApproval(rule.getRequiresApproval() != null ? rule.getRequiresApproval() : true);

        Task saved = taskRepository.save(task);

        // Create history entry
        TaskHistoryEntry history = new TaskHistoryEntry(
                saved, "CREATED", "SYSTEM",
                "Auto-generated by rule: " + rule.getRuleCode()
                        + (ownerUsername != null ? " | Assigned to: " + ownerUsername : " | No owner resolved")
        );
        taskHistoryRepository.save(history);

        // Notify
        notificationEngine.notifyTaskCreated(saved);
    }

    protected void closeTasksForRule(Long studentId, String ruleCode) {
        List<Task> activeTasks = taskRepository.findActiveByStudentAndRuleCode(studentId, ruleCode);
        for (Task task : activeTasks) {
            task.setStatus("COMPLETED");
            task.setClosedAt(LocalDateTime.now());
            taskRepository.save(task);

            TaskHistoryEntry history = new TaskHistoryEntry(
                    task, "COMPLETED", "SYSTEM",
                    "Auto-closed: business condition satisfied for rule " + ruleCode
            );
            taskHistoryRepository.save(history);

            notificationEngine.notifyTaskCompleted(task);
        }
    }
}
