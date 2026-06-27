package com.cc.service.engine;

import com.cc.modals.Task;
import com.cc.modals.TaskHistoryEntry;
import com.cc.repository.TaskHistoryRepository;
import com.cc.repository.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Processes reminders for tasks approaching or past their SLA.
 * Sends notifications and tracks reminder history.
 */
@Service
public class ReminderEngine {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private TaskHistoryRepository taskHistoryRepository;

    @Autowired
    private TaskNotificationEngine notificationEngine;

    /**
     * Process all reminders — called by the scheduler.
     */
    public void processReminders() {
        LocalDate today = LocalDate.now();
        LocalDate tomorrow = today.plusDays(1);

        // 1 day before due
        List<Task> tasksDueTomorrow = taskRepository.findTasksDueOn(tomorrow);
        for (Task task : tasksDueTomorrow) {
            sendReminder(task, "Reminder: Task due tomorrow — " + task.getTitle());
        }

        // Due today
        List<Task> tasksDueToday = taskRepository.findTasksDueOn(today);
        for (Task task : tasksDueToday) {
            sendReminder(task, "Reminder: Task due TODAY — " + task.getTitle());
        }

        // Overdue
        List<Task> overdueTasks = taskRepository.findOverdueTasks(today);
        for (Task task : overdueTasks) {
            sendReminder(task, "OVERDUE: Task past due date — " + task.getTitle());
        }
    }

    private void sendReminder(Task task, String message) {
        try {
            // Update reminder tracking
            task.setReminderCount(task.getReminderCount() != null ? task.getReminderCount() + 1 : 1);
            task.setLastReminderAt(LocalDateTime.now());
            taskRepository.save(task);

            // Create history entry
            TaskHistoryEntry history = new TaskHistoryEntry(
                    task, "REMINDER_SENT", "SYSTEM",
                    message + " (Reminder #" + task.getReminderCount() + ")");
            taskHistoryRepository.save(history);

            // Send notification
            notificationEngine.notifyReminderSent(task);
        } catch (Exception e) {
            System.err.println("Reminder error for task " + task.getId() + ": " + e.getMessage());
        }
    }
}
