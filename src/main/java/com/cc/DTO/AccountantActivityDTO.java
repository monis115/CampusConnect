package com.cc.DTO;

import java.time.LocalDateTime;

public class AccountantActivityDTO {
    private String studentName;
    private String applicationId;
    private String action; // "PAYMENT_RECEIVED", "PAYMENT_NOT_RECEIVED"
    private Double amount;
    private LocalDateTime timestamp;
    private String remarks;

    public AccountantActivityDTO() {
    }

    public AccountantActivityDTO(String studentName, String applicationId, String action, Double amount,
            LocalDateTime timestamp, String remarks) {
        this.studentName = studentName;
        this.applicationId = applicationId;
        this.action = action;
        this.amount = amount;
        this.timestamp = timestamp;
        this.remarks = remarks;
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

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }
}
