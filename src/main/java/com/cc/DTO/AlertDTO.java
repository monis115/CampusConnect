package com.cc.DTO;

/**
 * DTO for system alerts in the admin dashboard.
 */
public class AlertDTO {

    private String type; // STUCK_IN_STATUS, PAYMENT_OVERDUE, DOC_PENDING, etc.
    private String severity; // INFO, WARNING, CRITICAL
    private String message;
    private Long count;

    public AlertDTO() {
    }

    public AlertDTO(String type, String severity, String message, Long count) {
        this.type = type;
        this.severity = severity;
        this.message = message;
        this.count = count;
    }

    // Getters and Setters
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Long getCount() {
        return count;
    }

    public void setCount(Long count) {
        this.count = count;
    }
}
