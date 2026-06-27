package com.cc.service.engine;

import com.cc.modals.Task;
import com.cc.modals.TaskRule;
import com.cc.repository.TaskRepository;
import com.cc.repository.TaskRuleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

/**
 * Handles configurable, per-department escalation chains.
 * Reads escalation path from TaskRule.escalationChain JSON.
 */
@Service
public class EscalationEngine {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private TaskRuleRepository taskRuleRepository;

    @Autowired
    private TaskOwnerResolver ownerResolver;

    /**
     * Process escalations for all overdue tasks.
     */
    public void processEscalations(List<Task> overdueTasks) {
        for (Task task : overdueTasks) {
            try {
                if (task.getEscalatedTo() != null) continue; // Already escalated once, skip for now

                String nextTarget = getNextEscalationTarget(task);
                if (nextTarget != null) {
                    task.setEscalatedTo(nextTarget);
                    taskRepository.save(task);
                }
            } catch (Exception e) {
                System.err.println("Escalation error for task " + task.getId() + ": " + e.getMessage());
            }
        }
    }

    /**
     * Determines the next escalation target based on the rule's escalation chain.
     */
    public String getNextEscalationTarget(Task task) {
        if (task.getRuleCode() == null) {
            // Manual task — default escalation to ADMISSION_MANAGER
            return "ADMISSION_MANAGER";
        }

        TaskRule rule = taskRuleRepository.findByRuleCode(task.getRuleCode()).orElse(null);
        if (rule == null || rule.getEscalationChain() == null) {
            return "ADMISSION_MANAGER"; // Fallback
        }

        // Parse JSON array: ["ADMISSION_MANAGER","OPERATIONS","ADMIN"]
        String chain = rule.getEscalationChain();
        String[] targets = chain.replace("[", "").replace("]", "").replace("\"", "").split(",");

        String currentEscalation = task.getEscalatedTo();

        if (currentEscalation == null) {
            // First escalation — return first in chain
            return targets.length > 0 ? targets[0].trim() : null;
        }

        // Find current position and return next
        for (int i = 0; i < targets.length; i++) {
            if (targets[i].trim().equalsIgnoreCase(currentEscalation)) {
                if (i + 1 < targets.length) {
                    return targets[i + 1].trim();
                }
                return null; // Fully escalated
            }
        }

        return null;
    }
}
