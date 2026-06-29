package com.cc.modals;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(name = "additional_doc_requests")
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
public class AdditionalDocRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    @JsonIgnoreProperties({ "studentStatusHistory", "hibernateLazyInitializer", "handler", "tasks" })
    private Student student;

    @Column(name = "document_name", nullable = false, length = 255)
    private String documentName;

    @Column(name = "status", nullable = false, length = 50)
    private String status; // PENDING, UPLOADED, VERIFIED, REJECTED

    @Column(name = "requested_by", nullable = false, length = 100)
    private String requestedBy; // University Coordinator username

    @Column(name = "requested_by_name", length = 255)
    private String requestedByName;

    @Column(name = "assigned_to_agent", nullable = false, length = 100)
    private String assignedToAgent; // Agent username

    @Column(name = "uploaded_by", length = 100)
    private String uploadedBy; // Agent username who uploaded

    @Column(name = "uploaded_doc_path", columnDefinition = "TEXT")
    private String uploadedDocPath;

    @Column(name = "remarks", columnDefinition = "TEXT")
    private String remarks;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public AdditionalDocRequest() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Student getStudent() {
        return student;
    }

    public void setStudent(Student student) {
        this.student = student;
    }

    public String getDocumentName() {
        return documentName;
    }

    public void setDocumentName(String documentName) {
        this.documentName = documentName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getRequestedBy() {
        return requestedBy;
    }

    public void setRequestedBy(String requestedBy) {
        this.requestedBy = requestedBy;
    }

    public String getRequestedByName() {
        return requestedByName;
    }

    public void setRequestedByName(String requestedByName) {
        this.requestedByName = requestedByName;
    }

    public String getAssignedToAgent() {
        return assignedToAgent;
    }

    public void setAssignedToAgent(String assignedToAgent) {
        this.assignedToAgent = assignedToAgent;
    }

    public String getUploadedBy() {
        return uploadedBy;
    }

    public void setUploadedBy(String uploadedBy) {
        this.uploadedBy = uploadedBy;
    }

    public String getUploadedDocPath() {
        return uploadedDocPath;
    }

    public void setUploadedDocPath(String uploadedDocPath) {
        this.uploadedDocPath = uploadedDocPath;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
