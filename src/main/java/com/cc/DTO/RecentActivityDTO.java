package com.cc.DTO;

import java.time.LocalDateTime;

/**
 * DTO for recent activity items in the admin dashboard.
 */
public class RecentActivityDTO {

    private Long studentId;
    private String applicationId;
    private String studentName;
    private String action; // Status change or action performed
    private String previousStatus;
    private String newStatus;
    private LocalDateTime timestamp;
    private String actor; // Username who performed the action

    public RecentActivityDTO() {
    }

    public RecentActivityDTO(Long studentId, String applicationId, String studentName,
            String action, String previousStatus, String newStatus,
            LocalDateTime timestamp, String actor) {
        this.studentId = studentId;
        this.applicationId = applicationId;
        this.studentName = studentName;
        this.action = action;
        this.previousStatus = previousStatus;
        this.newStatus = newStatus;
        this.timestamp = timestamp;
        this.actor = actor;
    }

    // Getters and Setters
    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public String getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getPreviousStatus() {
        return previousStatus;
    }

    public void setPreviousStatus(String previousStatus) {
        this.previousStatus = previousStatus;
    }

    public String getNewStatus() {
        return newStatus;
    }

    public void setNewStatus(String newStatus) {
        this.newStatus = newStatus;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getActor() {
        return actor;
    }

    public void setActor(String actor) {
        this.actor = actor;
    }
}
