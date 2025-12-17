package com.cc.DTO;

import java.time.LocalDateTime;

/**
 * DTO for students assigned to a TelexMan
 * Contains limited fields: applicationId, fullName, collegeName, agentName,
 * course, applicationStatus, lastHistory
 */
public class TelexManStudentDTO {
    private Long id;
    private String applicationId;
    private String fullName;
    private String collegeName;
    private String agentName;
    private String course;
    private String applicationStatus;
    private LastHistoryDTO lastHistory;

    public TelexManStudentDTO() {
    }

    public TelexManStudentDTO(Long id, String applicationId, String fullName, String collegeName,
            String agentName, String course, String applicationStatus,
            LastHistoryDTO lastHistory) {
        this.id = id;
        this.applicationId = applicationId;
        this.fullName = fullName;
        this.collegeName = collegeName;
        this.agentName = agentName;
        this.course = course;
        this.applicationStatus = applicationStatus;
        this.lastHistory = lastHistory;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getCollegeName() {
        return collegeName;
    }

    public void setCollegeName(String collegeName) {
        this.collegeName = collegeName;
    }

    public String getAgentName() {
        return agentName;
    }

    public void setAgentName(String agentName) {
        this.agentName = agentName;
    }

    public String getCourse() {
        return course;
    }

    public void setCourse(String course) {
        this.course = course;
    }

    public String getApplicationStatus() {
        return applicationStatus;
    }

    public void setApplicationStatus(String applicationStatus) {
        this.applicationStatus = applicationStatus;
    }

    public LastHistoryDTO getLastHistory() {
        return lastHistory;
    }

    public void setLastHistory(LastHistoryDTO lastHistory) {
        this.lastHistory = lastHistory;
    }

    /**
     * Inner class for last history entry
     */
    public static class LastHistoryDTO {
        private String status;
        private String updatedBy;
        private LocalDateTime updatedAt;
        private String remarks;

        public LastHistoryDTO() {
        }

        public LastHistoryDTO(String status, String updatedBy, LocalDateTime updatedAt, String remarks) {
            this.status = status;
            this.updatedBy = updatedBy;
            this.updatedAt = updatedAt;
            this.remarks = remarks;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
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
}
