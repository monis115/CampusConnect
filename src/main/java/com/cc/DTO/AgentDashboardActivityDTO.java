package com.cc.DTO;

import java.time.LocalDateTime;

public class AgentDashboardActivityDTO {
    private String studentName;
    private String status;
    private LocalDateTime updatedAt;
    private String remarks;

    public AgentDashboardActivityDTO() {
    }

    public AgentDashboardActivityDTO(String studentName, String status, LocalDateTime updatedAt, String remarks) {
        this.studentName = studentName;
        this.status = status;
        this.updatedAt = updatedAt;
        this.remarks = remarks;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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
