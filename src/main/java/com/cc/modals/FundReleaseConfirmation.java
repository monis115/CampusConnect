package com.cc.modals;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "fund_release_confirmation")
public class FundReleaseConfirmation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fund_release_id", nullable = false)
    private FundRelease fundRelease;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @Column(name = "visa_manager_username")
    private String visaManagerUsername;

    @Column(name = "confirmation_status")
    private String confirmationStatus; // CONFIRMED or REJECTED

    @Column(name = "confirmed_by")
    private String confirmedBy; // Same as Visa Manager Username

    @Column(name = "confirmed_date")
    private LocalDateTime confirmedDate;

    @Column(name = "remarks", columnDefinition = "TEXT")
    private String remarks;
    
    @Column(name = "rejection_reason", columnDefinition = "TEXT")
    private String rejectionReason;

    @Column(name = "receipt_url", columnDefinition = "TEXT")
    private String receiptUrl; // If the Visa Manager wants to upload something

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public FundReleaseConfirmation() {}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public FundRelease getFundRelease() {
        return fundRelease;
    }

    public void setFundRelease(FundRelease fundRelease) {
        this.fundRelease = fundRelease;
    }

    public Student getStudent() {
        return student;
    }

    public void setStudent(Student student) {
        this.student = student;
    }

    public String getVisaManagerUsername() {
        return visaManagerUsername;
    }

    public void setVisaManagerUsername(String visaManagerUsername) {
        this.visaManagerUsername = visaManagerUsername;
    }

    public String getConfirmationStatus() {
        return confirmationStatus;
    }

    public void setConfirmationStatus(String confirmationStatus) {
        this.confirmationStatus = confirmationStatus;
    }

    public String getConfirmedBy() {
        return confirmedBy;
    }

    public void setConfirmedBy(String confirmedBy) {
        this.confirmedBy = confirmedBy;
    }

    public LocalDateTime getConfirmedDate() {
        return confirmedDate;
    }

    public void setConfirmedDate(LocalDateTime confirmedDate) {
        this.confirmedDate = confirmedDate;
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

    public String getReceiptUrl() {
        return receiptUrl;
    }

    public void setReceiptUrl(String receiptUrl) {
        this.receiptUrl = receiptUrl;
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
