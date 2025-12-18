package com.cc.DTO;

import java.time.LocalDateTime;

/**
 * DTO for alerts and bottlenecks in the college.
 */
public class CollegeAlertDTO {

    private String alertType; // STUCK_STUDENT, PENDING_PAYMENT, DOCUMENT_ISSUE, PROCESSING_DELAY
    private String severity; // HIGH, MEDIUM, LOW
    private String title;
    private String message;
    private Long affectedCount;
    private Long referenceId; // studentId or other entity ID
    private String referenceName;
    private LocalDateTime detectedAt;

    public CollegeAlertDTO() {
    }

    public CollegeAlertDTO(String alertType, String severity, String title, String message,
            Long affectedCount, Long referenceId, String referenceName) {
        this.alertType = alertType;
        this.severity = severity;
        this.title = title;
        this.message = message;
        this.affectedCount = affectedCount;
        this.referenceId = referenceId;
        this.referenceName = referenceName;
        this.detectedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public String getAlertType() {
        return alertType;
    }

    public void setAlertType(String alertType) {
        this.alertType = alertType;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Long getAffectedCount() {
        return affectedCount;
    }

    public void setAffectedCount(Long affectedCount) {
        this.affectedCount = affectedCount;
    }

    public Long getReferenceId() {
        return referenceId;
    }

    public void setReferenceId(Long referenceId) {
        this.referenceId = referenceId;
    }

    public String getReferenceName() {
        return referenceName;
    }

    public void setReferenceName(String referenceName) {
        this.referenceName = referenceName;
    }

    public LocalDateTime getDetectedAt() {
        return detectedAt;
    }

    public void setDetectedAt(LocalDateTime detectedAt) {
        this.detectedAt = detectedAt;
    }
}
