package com.cc.service.engine;

import com.cc.DTO.NotificationDTO;
import com.cc.modals.Task;
import com.cc.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Set;

/**
 * Dispatches notifications for task lifecycle events.
 * Leverages existing NotificationService infrastructure.
 */
@Service
public class TaskNotificationEngine {

    @Autowired
    private NotificationService notificationService;

    public void notifyTaskCreated(Task task) {
        // Notify the assigned owner
        if (task.getAssignedToUser() != null) {
            sendNotification(
                "New Task Assigned: " + task.getTitle(),
                "You have been assigned a new task for student " +
                    (task.getStudent() != null ? task.getStudent().getFullName() : "Unknown") +
                    ". Priority: " + task.getPriority() + ". Due: " + task.getDueDate(),
                "TASK_ASSIGNED",
                "SYSTEM",
                task.getAssignedToUser()
            );
        }

        // Notify watchers
        notifyWatchers(task, "New Task Created: " + task.getTitle(),
            "A task you're watching has been created: " + task.getDescription());
    }

    public void notifyTaskEscalated(Task task) {
        if (task.getEscalatedTo() != null) {
            sendNotification(
                "Task Escalated: " + task.getTitle(),
                "A task has been escalated to you. Student: " +
                    (task.getStudent() != null ? task.getStudent().getFullName() : "Unknown"),
                "TASK_ESCALATED",
                "SYSTEM",
                task.getEscalatedTo()
            );
        }
    }

    public void notifyReminderSent(Task task) {
        if (task.getAssignedToUser() != null) {
            sendNotification(
                "Reminder: " + task.getTitle(),
                "This task is due on " + task.getDueDate() + ". Reminder #" + task.getReminderCount(),
                "TASK_REMINDER",
                "SYSTEM",
                task.getAssignedToUser()
            );
        }
    }

    public void notifyTaskAwaitingApproval(Task task, String completedBy) {
        // Notify Admission Manager that the task is waiting for review
        if (task.getStudent() != null && task.getStudent().getAdmissionManager() != null) {
            sendNotification(
                "Task Waiting for Review: " + task.getTitle(),
                completedBy + " has marked the task as done for student " + task.getStudent().getFullName() + ". Please review and approve.",
                "TASK_AWAITING_APPROVAL",
                completedBy,
                task.getStudent().getAdmissionManager().getUsername()
            );
        }
        
        // Notify watchers
        notifyWatchers(task, "Task Waiting for Review: " + task.getTitle(),
            completedBy + " has marked a task you're watching as done.");
    }

    public void notifyTaskCompleted(Task task) {
        // Notify creator
        if (task.getCreatedBy() != null && !"SYSTEM".equals(task.getCreatedBy())) {
            sendNotification(
                "Task Completed: " + task.getTitle(),
                "A task you created has been marked as completed.",
                "TASK_COMPLETED",
                "SYSTEM",
                task.getCreatedBy()
            );
        }
        notifyWatchers(task, "Task Completed: " + task.getTitle(),
            "A task you're watching has been completed.");
    }

    public void notifyApprovalNeeded(Task task) {
        // Notify the creator (typically the approver for system tasks)
        if (task.getCreatedBy() != null) {
            sendNotification(
                "Approval Required: " + task.getTitle(),
                "A task requires your approval for student " +
                    (task.getStudent() != null ? task.getStudent().getFullName() : "Unknown"),
                "TASK_APPROVAL",
                task.getAssignedToUser() != null ? task.getAssignedToUser() : "SYSTEM",
                task.getCreatedBy()
            );
        }
    }

    private void notifyWatchers(Task task, String heading, String content) {
        Set<String> watchers = task.getWatchers();
        if (watchers != null) {
            for (String watcher : watchers) {
                sendNotification(heading, content, "TASK_WATCHER", "SYSTEM", watcher);
            }
        }
    }

    private void sendNotification(String heading, String content, String type,
                                  String sender, String target) {
        try {
            NotificationDTO dto = new NotificationDTO();
            dto.setHeading(heading);
            dto.setContent(content);
            dto.setType(type);
            dto.setSenderUsername(sender);
            dto.setTargetUsername(target);
            notificationService.createNotification(dto);
        } catch (Exception e) {
            System.err.println("Failed to send notification to " + target + ": " + e.getMessage());
        }
    }
}
