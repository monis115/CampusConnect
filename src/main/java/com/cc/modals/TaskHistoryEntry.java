package com.cc.modals;

import jakarta.persistence.*;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@Table(name = "task_history")
public class TaskHistoryEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", nullable = false)
    @JsonIgnoreProperties({"followUps", "watchers", "student", "childTasks", "parentTask"})
    private Task task;

    @Column(nullable = false)
    private String action; // CREATED, ASSIGNED, VIEWED, STARTED, PAUSED, REMINDER_SENT, 
                           // FOLLOW_UP, ESCALATED, REASSIGNED, COMPLETED, VERIFIED, ARCHIVED

    private String performedBy; // Username or "SYSTEM"

    @Column(columnDefinition = "TEXT")
    private String details; // Human-readable detail

    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public TaskHistoryEntry() {
    }

    public TaskHistoryEntry(Task task, String action, String performedBy, String details) {
        this.task = task;
        this.action = action;
        this.performedBy = performedBy;
        this.details = details;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Task getTask() {
        return task;
    }

    public void setTask(Task task) {
        this.task = task;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getPerformedBy() {
        return performedBy;
    }

    public void setPerformedBy(String performedBy) {
        this.performedBy = performedBy;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
