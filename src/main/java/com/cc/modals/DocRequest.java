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
@Table(name = "doc_requests")
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
public class DocRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    @JsonIgnoreProperties({ "studentStatusHistory", "hibernateLazyInitializer", "handler" })
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "college_id")
    @JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
    private College college;

    @Column(name = "doc_type", nullable = false, length = 100)
    private String docType; // LOAN_LETTER, BONAFIDE_CERTIFICATE, FEE_STRUCTURE, OTHER etc.

    @Column(name = "custom_doc_name", length = 255)
    private String customDocName; // Used when docType = OTHER

    @Column(name = "status", nullable = false, length = 50)
    private String status; // PENDING, FORWARDED, COMPLETED, REJECTED

    @Column(name = "requested_by", nullable = false, length = 100)
    private String requestedBy; // Agent username

    @Column(name = "requested_by_name", length = 255)
    private String requestedByName; // Agent full name

    @Column(name = "forwarded_by", length = 100)
    private String forwardedBy; // Admission Manager username

    @Column(name = "forwarded_by_name", length = 255)
    private String forwardedByName;

    @Column(name = "uploaded_by", length = 100)
    private String uploadedBy; // University Coordinator username

    @Column(name = "uploaded_doc_path", columnDefinition = "TEXT")
    private String uploadedDocPath;

    @Column(name = "remarks", columnDefinition = "TEXT")
    private String remarks;

    @Column(name = "rejection_reason", columnDefinition = "TEXT")
    private String rejectionReason;

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

    public DocRequest() {
    }

    // Getters and Setters

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

    public College getCollege() {
        return college;
    }

    public void setCollege(College college) {
        this.college = college;
    }

    public String getDocType() {
        return docType;
    }

    public void setDocType(String docType) {
        this.docType = docType;
    }

    public String getCustomDocName() {
        return customDocName;
    }

    public void setCustomDocName(String customDocName) {
        this.customDocName = customDocName;
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

    public String getForwardedBy() {
        return forwardedBy;
    }

    public void setForwardedBy(String forwardedBy) {
        this.forwardedBy = forwardedBy;
    }

    public String getForwardedByName() {
        return forwardedByName;
    }

    public void setForwardedByName(String forwardedByName) {
        this.forwardedByName = forwardedByName;
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

    public String getRejectionReason() {
        return rejectionReason;
    }

    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
