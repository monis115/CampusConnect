package com.cc.DTO;

import java.time.LocalDateTime;

/**
 * DTO for students assigned to a UniversityPerson
 * Contains: applicationId, fullName, applicationStatus,
 * admissionLetterUploaded, lastHistory
 */
public class UniversityPersonStudentDTO {
    private Long id;
    private String applicationId;
    private String fullName;
    private String applicationStatus;
    private String admissionLetterUploaded; // "Yes" or "No"
    private LastHistoryDTO lastHistory;

    public UniversityPersonStudentDTO() {
    }

    public UniversityPersonStudentDTO(Long id, String applicationId, String fullName,
            String applicationStatus, String admissionLetterUploaded,
            LastHistoryDTO lastHistory) {
        this.id = id;
        this.applicationId = applicationId;
        this.fullName = fullName;
        this.applicationStatus = applicationStatus;
        this.admissionLetterUploaded = admissionLetterUploaded;
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

    public String getApplicationStatus() {
        return applicationStatus;
    }

    public void setApplicationStatus(String applicationStatus) {
        this.applicationStatus = applicationStatus;
    }

    public String getAdmissionLetterUploaded() {
        return admissionLetterUploaded;
    }

    public void setAdmissionLetterUploaded(String admissionLetterUploaded) {
        this.admissionLetterUploaded = admissionLetterUploaded;
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
        private String updatedByName;
        private LocalDateTime updatedAt;
        private String remarks;

        public LastHistoryDTO() {
        }

        public LastHistoryDTO(String status, String updatedBy, String updatedByName, LocalDateTime updatedAt, String remarks) {
            this.status = status;
            this.updatedBy = updatedBy;
            this.updatedByName = updatedByName;
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
