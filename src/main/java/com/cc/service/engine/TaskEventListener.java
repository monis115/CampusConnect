package com.cc.service.engine;

import com.cc.modals.Student;
import com.cc.modals.Task;
import com.cc.modals.TaskHistoryEntry;
import com.cc.modals.TaskRule;
import com.cc.repository.StudentRepository;
import com.cc.repository.TaskHistoryRepository;
import com.cc.repository.TaskRepository;
import com.cc.repository.TaskRuleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Hybrid event listener for instant task creation/closure.
 * Called from existing services when a business action happens,
 * so tasks are updated immediately (not waiting for 15-min scheduler).
 */
@Service
public class TaskEventListener {

    @Autowired
    private TaskRuleRepository taskRuleRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private TaskHistoryRepository taskHistoryRepository;

    @Autowired
    private TaskRuleEvaluator ruleEvaluator;

    @Autowired
    private StudentRepository studentRepository;

    /**
     * Called when a student field is updated in an existing service.
     * Instantly evaluates relevant rules and creates/closes tasks.
     */
    public void onStudentFieldUpdated(Long studentId, String fieldName) {
        try {
            Student student = studentRepository.findById(studentId).orElse(null);
            if (student == null)
                return;

            List<TaskRule> rules = taskRuleRepository.findByEnabledTrue();
            for (TaskRule rule : rules) {
                // Check if this rule's condition or auto-close involves the updated field
                boolean isRelevant = fieldName.equals(rule.getConditionField())
                        || fieldName.equals(rule.getAutoCloseConditionField());

                if (!isRelevant)
                    continue;

                // Check auto-close first
                if (rule.getAutoCloseConditionField() != null
                        && rule.getAutoCloseConditionField().equals(fieldName)) {
                    if (ruleEvaluator.shouldCloseTask(rule, student)) {
                        closeTasksForRule(studentId, rule.getRuleCode());
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Event listener error for student " + studentId + ": " + e.getMessage());
        }
    }

    private void closeTasksForRule(Long studentId, String ruleCode) {
        List<Task> activeTasks = taskRepository.findActiveByStudentAndRuleCode(studentId, ruleCode);
        for (Task task : activeTasks) {
            task.setStatus("COMPLETED");
            task.setClosedAt(LocalDateTime.now());
            taskRepository.save(task);

            TaskHistoryEntry history = new TaskHistoryEntry(
                    task, "COMPLETED", "SYSTEM",
                    "Auto-closed by event: business condition satisfied");
            taskHistoryRepository.save(history);
        }
    }
}
