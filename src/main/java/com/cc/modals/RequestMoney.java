package com.cc.modals;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.LocalDateTime;

@Entity
@Table(name = "request_money")
public class RequestMoney {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String requesterUsername;

    @Column(nullable = false)
    private String requesterFullName;

    @Column(nullable = false)
    private String requesterRole; // e.g., "DocPerson", "VisaMan"

    @Column(nullable = false)
    private String purpose;

    @Column(nullable = false)
    private Double requestedAmount;

    @Column(nullable = false)
    private String requesterPaymentMode; // e.g., "UPI", "Bank Transfer"

    // Duplicate field to handle legacy DB column schema drift
    @Column(name = "payment_mode", nullable = false)
    private String legacyPaymentMode;

    @Column(nullable = false, length = 1000)
    private String bankDetails; // JSON or formatted string with details

    private String status = "PENDING"; // PENDING, PAID, PARTIAL

    private Double amountPaid = 0.0;

    @OneToMany(mappedBy = "requestMoney", cascade = CascadeType.ALL)
    @JsonIgnoreProperties("requestMoney")
    @OrderBy("id DESC")
    private java.util.List<PaymentTransaction> transactions = new java.util.ArrayList<>();

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String qrCode; // Base64 or URL

    public RequestMoney() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public RequestMoney(String requesterUsername, String requesterFullName, String requesterRole,
            String purpose, Double requestedAmount, String requesterPaymentMode, String bankDetails, String qrCode) {
        this.requesterUsername = requesterUsername;
        this.requesterFullName = requesterFullName;
        this.requesterRole = requesterRole;
        this.purpose = purpose;
        this.requestedAmount = requestedAmount;
        this.requesterPaymentMode = requesterPaymentMode;
        this.legacyPaymentMode = requesterPaymentMode; // Sync legacy field
        this.bankDetails = bankDetails;
        this.qrCode = qrCode;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void setLastUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRequesterUsername() {
        return requesterUsername;
    }

    public void setRequesterUsername(String requesterUsername) {
        this.requesterUsername = requesterUsername;
    }

    public String getRequesterFullName() {
        return requesterFullName;
    }

    public void setRequesterFullName(String requesterFullName) {
        this.requesterFullName = requesterFullName;
    }

    public String getRequesterRole() {
        return requesterRole;
    }

    public void setRequesterRole(String requesterRole) {
        this.requesterRole = requesterRole;
    }

    public String getPurpose() {
        return purpose;
    }

    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }

    public Double getRequestedAmount() {
        return requestedAmount;
    }

    public void setRequestedAmount(Double requestedAmount) {
        this.requestedAmount = requestedAmount;
    }

    public String getRequesterPaymentMode() {
        return requesterPaymentMode;
    }

    public void setRequesterPaymentMode(String requesterPaymentMode) {
        this.requesterPaymentMode = requesterPaymentMode;
        this.legacyPaymentMode = requesterPaymentMode; // Keep synced
    }

    public String getBankDetails() {
        return bankDetails;
    }

    public void setBankDetails(String bankDetails) {
        this.bankDetails = bankDetails;
    }

    public String getQrCode() {
        return qrCode;
    }

    public void setQrCode(String qrCode) {
        this.qrCode = qrCode;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Double getAmountPaid() {
        return amountPaid;
    }

    public void setAmountPaid(Double amountPaid) {
        this.amountPaid = amountPaid;
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

    public java.util.List<PaymentTransaction> getTransactions() {
        return transactions;
    }

    public void setTransactions(java.util.List<PaymentTransaction> transactions) {
        this.transactions = transactions;
    }

    // Computed Getter for Frontend convenience
    public Double getRemainingAmount() {
        return (requestedAmount != null ? requestedAmount : 0.0) - (amountPaid != null ? amountPaid : 0.0);
    }
}
