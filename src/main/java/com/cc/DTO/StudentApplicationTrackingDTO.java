package com.cc.DTO;

import com.cc.modals.StudentStatus;
import java.time.LocalDateTime;
import java.util.List;

public class StudentApplicationTrackingDTO {
    private String fullName;
    private String applicationId;
    private StudentStatus currentStatus;
    private LocalDateTime lastUpdated;
    private List<ApplicationHistoryDTO> history;

    public StudentApplicationTrackingDTO(String fullName, String applicationId, StudentStatus currentStatus,
            LocalDateTime lastUpdated, List<ApplicationHistoryDTO> history) {
        this.fullName = fullName;
        this.applicationId = applicationId;
        this.currentStatus = currentStatus;
        this.lastUpdated = lastUpdated;
        this.history = history;
    }

    // Inner DTO for History
    public static class ApplicationHistoryDTO {
        private StudentStatus status;
        private LocalDateTime updatedAt;
        private String updatedBy;
        private String comments;

        public ApplicationHistoryDTO(StudentStatus status, LocalDateTime updatedAt, String updatedBy, String comments) {
            this.status = status;
            this.updatedAt = updatedAt;
            this.updatedBy = updatedBy;
            this.comments = comments;
        }

        public StudentStatus getStatus() {
            return status;
        }

        public void setStatus(StudentStatus status) {
            this.status = status;
        }

        public LocalDateTime getUpdatedAt() {
            return updatedAt;
        }

        public void setUpdatedAt(LocalDateTime updatedAt) {
            this.updatedAt = updatedAt;
        }

        public String getUpdatedBy() {
            return updatedBy;
        }

        public void setUpdatedBy(String updatedBy) {
            this.updatedBy = updatedBy;
        }

        public String getComments() {
            return comments;
        }

        public void setComments(String comments) {
            this.comments = comments;
        }
    }

    // Getters and Setters
    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }

    public StudentStatus getCurrentStatus() {
        return currentStatus;
    }

    public void setCurrentStatus(StudentStatus currentStatus) {
        this.currentStatus = currentStatus;
    }

    public LocalDateTime getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(LocalDateTime lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public List<ApplicationHistoryDTO> getHistory() {
        return history;
    }

    public void setHistory(List<ApplicationHistoryDTO> history) {
        this.history = history;
    }
}
