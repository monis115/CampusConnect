package com.cc.scheduler;

import com.cc.modals.Student;
import com.cc.modals.Task;
import com.cc.modals.TaskRule;
import com.cc.repository.StudentRepository;
import com.cc.repository.TaskRepository;
import com.cc.repository.TaskRuleRepository;
import com.cc.service.engine.EscalationEngine;
import com.cc.service.engine.ReminderEngine;
import com.cc.service.engine.generators.TaskGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Thin coordinator — delegates all business logic to modular generators and
 * engines.
 * The scheduler is only ONE component of the hybrid architecture.
 * It catches anything the event listeners miss.
 */
@Service
public class TaskSyncService {

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private TaskRuleRepository taskRuleRepository;

    @Autowired
    private List<TaskGenerator> taskGenerators; // Spring auto-injects all implementations

    @Autowired
    private ReminderEngine reminderEngine;

    @Autowired
    private EscalationEngine escalationEngine;

    @Autowired
    private com.cc.service.engine.TaskRuleEvaluator ruleEvaluator;

    /**
     * Main scheduled sync — runs every 15 minutes for evaluation.
     * Catches anything the event listeners miss.
     */
    public void runScheduledSync() {
        System.out.println("Running Enterprise Task Engine...");

        // Clean up any existing orphaned tasks (no valid owner assigned).
        // We mark them CANCELLED instead of deleting, to avoid FK constraint
        // violations from task_history records that reference these tasks.
        try {
            List<Task> orphans = taskRepository.findOrphanedSystemTasks();
            for (Task orphan : orphans) {
                orphan.setStatus("CANCELLED");
                taskRepository.save(orphan);
            }
            if (!orphans.isEmpty()) {
                System.out.println("Cancelled " + orphans.size() + " orphaned system tasks.");
            }
        } catch (Exception e) {
            System.err.println("Failed to clean up orphaned tasks: " + e.getMessage());
        }

        List<TaskRule> allRules = taskRuleRepository.findByEnabledTrue();
        if (allRules.isEmpty()) {
            System.out.println("No enabled rules found. Skipping task generation.");
            return;
        }

        List<Student> students = studentRepository.findAll();

        for (Student student : students) {
            try {
                for (TaskGenerator generator : taskGenerators) {
                    // Filter rules for this generator's department
                    List<TaskRule> deptRules = allRules.stream()
                            .filter(r -> generator.getDepartment().equalsIgnoreCase(r.getDepartment()))
                            .collect(Collectors.toList());

                    if (!deptRules.isEmpty()) {
                        generator.evaluate(student, deptRules);
                    }
                }
            } catch (Exception e) {
                System.err.println("Failed to process student ID " + student.getId() + ": " + e.getMessage());
            }
        }

        // Process reminders
        try {
            reminderEngine.processReminders();
        } catch (Exception e) {
            System.err.println("Reminder engine error: " + e.getMessage());
        }

        // Process escalations
        try {
            List<Task> overdueTasks = taskRepository.findOverdueTasks(LocalDate.now());
            escalationEngine.processEscalations(overdueTasks);
        } catch (Exception e) {
            System.err.println("Escalation engine error: " + e.getMessage());
        }

        System.out.println("Enterprise Task Engine cycle complete.");
    }

    /**
     * Runs every 15 minutes to activate delayed tasks.
     * If the condition is resolved during the delay, the task silently disappears.
     */
    @Scheduled(fixedRate = 900000)
    public void activateScheduledTasks() {
        List<Task> readyTasks = taskRepository.findByStatusAndScheduledForLessThanEqual("SCHEDULED",
                java.time.LocalDateTime.now());
        if (readyTasks.isEmpty())
            return;

        List<TaskRule> allRules = taskRuleRepository.findByEnabledTrue();

        for (Task task : readyTasks) {
            TaskRule rule = allRules.stream().filter(r -> r.getRuleCode().equals(task.getRuleCode())).findFirst()
                    .orElse(null);

            if (rule != null && ruleEvaluator.shouldCreateTask(rule, task.getStudent())
                    && !ruleEvaluator.shouldCloseTask(rule, task.getStudent())) {
                task.setStatus("PENDING");
                Integer dueDelay = rule.getDueDelayMinutes() != null ? rule.getDueDelayMinutes() : 4320;
                task.setDueDate(java.time.LocalDate.now().plusDays(dueDelay / 1440));
                task.setCreatedAt(java.time.LocalDateTime.now());
                taskRepository.save(task);
            } else {
                taskRepository.delete(task);
            }
        }
    }
}
