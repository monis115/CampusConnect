package com.cc.DTO;

import java.time.LocalDateTime;

/**
 * DTO for recent activities within a college.
 */
public class CollegeRecentActivityDTO {

    private Long activityId;
    private Long studentId;
    private String studentName;
    private String applicationId;
    private String previousStatus;
    private String newStatus;
    private String updatedBy;
    private LocalDateTime updatedAt;
    private String remarks;

    public CollegeRecentActivityDTO() {
    }

    public CollegeRecentActivityDTO(Long activityId, Long studentId, String studentName,
            String applicationId, String previousStatus, String newStatus,
            String updatedBy, LocalDateTime updatedAt, String remarks) {
        this.activityId = activityId;
        this.studentId = studentId;
        this.studentName = studentName;
        this.applicationId = applicationId;
        this.previousStatus = previousStatus;
        this.newStatus = newStatus;
        this.updatedBy = updatedBy;
        this.updatedAt = updatedAt;
        this.remarks = remarks;
    }

    // Getters and Setters
    public Long getActivityId() {
        return activityId;
    }

    public void setActivityId(Long activityId) {
        this.activityId = activityId;
    }

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
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

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }
}
