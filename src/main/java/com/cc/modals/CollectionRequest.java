package com.cc.modals;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "collection_request")
public class CollectionRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "request_id", unique = true)
    private String requestId; // e.g., COL-20231015-001

    @Column(name = "student_id", nullable = false)
    private Long studentId;

    @Column(name = "student_name")
    private String studentName;

    @Column(name = "application_id")
    private String applicationId;

    @Column(name = "agent_username")
    private String agentUsername;

    @Column(name = "agent_name")
    private String agentName;

    @Column(name = "university_name")
    private String universityName;

    @Column(name = "requested_amount", nullable = false)
    private Double requestedAmount = 0.0;

    @Column(name = "collected_amount")
    private Double collectedAmount = 0.0;

    @Column(name = "pending_amount")
    private Double pendingAmount = 0.0;

    @Column(name = "due_date")
    private LocalDate dueDate;

    @Column(name = "remarks", length = 500)
    private String remarks;

    @Column(name = "status", nullable = false)
    private String status = "PENDING"; // PENDING, PARTIAL, PAID, CANCELLED

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.pendingAmount == null || this.pendingAmount == 0.0) {
            this.pendingAmount = this.requestedAmount;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
        if (this.collectedAmount != null && this.requestedAmount != null) {
            this.pendingAmount = this.requestedAmount - this.collectedAmount;
        }
        
        if (this.pendingAmount <= 0) {
            this.status = "PAID";
        } else if (this.collectedAmount > 0) {
            this.status = "PARTIAL";
        } else {
            this.status = "PENDING";
        }
    }

    // Getters and Setters

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getRequestId() { return requestId; }
    public void setRequestId(String requestId) { this.requestId = requestId; }

    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }

    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }

    public String getApplicationId() { return applicationId; }
    public void setApplicationId(String applicationId) { this.applicationId = applicationId; }

    public String getAgentUsername() { return agentUsername; }
    public void setAgentUsername(String agentUsername) { this.agentUsername = agentUsername; }

    public String getAgentName() { return agentName; }
    public void setAgentName(String agentName) { this.agentName = agentName; }

    public String getUniversityName() { return universityName; }
    public void setUniversityName(String universityName) { this.universityName = universityName; }

    public Double getRequestedAmount() { return requestedAmount; }
    public void setRequestedAmount(Double requestedAmount) { this.requestedAmount = requestedAmount; }

    public Double getCollectedAmount() { return collectedAmount; }
    public void setCollectedAmount(Double collectedAmount) { this.collectedAmount = collectedAmount; }

    public Double getPendingAmount() { return pendingAmount; }
    public void setPendingAmount(Double pendingAmount) { this.pendingAmount = pendingAmount; }

    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }

    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
