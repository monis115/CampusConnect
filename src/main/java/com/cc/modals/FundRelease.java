package com.cc.modals;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "fund_release")
public class FundRelease {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long releaseId;

    @Column(name = "collection_request_id")
    private Long collectionRequestId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @Column(name = "source_account_id")
    private Long sourceAccountId; // Could link to SubAccount

    @Column(name = "beneficiary_bank_name")
    private String beneficiaryBankName;

    @Column(name = "beneficiary_account_number")
    private String beneficiaryAccountNumber;

    @Column(name = "beneficiary_swift_code")
    private String beneficiarySwiftCode;

    @Column(name = "beneficiary_account_type")
    private String beneficiaryAccountType;

    @Column(name = "beneficiary_account_id")
    private Long beneficiaryAccountId;

    @Column(name = "visa_fee")
    private Double visaFee;

    @Column(name = "service_charge")
    private Double serviceCharge;

    @Column(name = "total_released_amount")
    private Double totalReleasedAmount;

    @Column(name = "transaction_reference")
    private String transactionReference;

    @Column(name = "transfer_date")
    private LocalDate transferDate;

    @Column(name = "receipt_url", columnDefinition = "TEXT")
    private String receiptUrl;

    @Column(name = "remarks", columnDefinition = "TEXT")
    private String remarks;

    @Column(name = "released_by")
    private String releasedBy; // Accountant username

    @Column(name = "released_date")
    private LocalDateTime releasedDate;

    @Column(name = "release_status")
    private String releaseStatus = "PENDING_CONFIRMATION";

    @Column(name = "rejection_reason", columnDefinition = "TEXT")
    private String rejectionReason;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public FundRelease() {}

    // Getters and Setters
    public Long getReleaseId() { return releaseId; }
    public void setReleaseId(Long releaseId) { this.releaseId = releaseId; }

    public Long getCollectionRequestId() { return collectionRequestId; }
    public void setCollectionRequestId(Long collectionRequestId) { this.collectionRequestId = collectionRequestId; }

    public Student getStudent() { return student; }
    public void setStudent(Student student) { this.student = student; }

    public Long getSourceAccountId() { return sourceAccountId; }
    public void setSourceAccountId(Long sourceAccountId) { this.sourceAccountId = sourceAccountId; }

    public String getBeneficiaryBankName() { return beneficiaryBankName; }
    public void setBeneficiaryBankName(String beneficiaryBankName) { this.beneficiaryBankName = beneficiaryBankName; }

    public String getBeneficiaryAccountNumber() { return beneficiaryAccountNumber; }
    public void setBeneficiaryAccountNumber(String beneficiaryAccountNumber) { this.beneficiaryAccountNumber = beneficiaryAccountNumber; }

    public String getBeneficiarySwiftCode() { return beneficiarySwiftCode; }
    public void setBeneficiarySwiftCode(String beneficiarySwiftCode) { this.beneficiarySwiftCode = beneficiarySwiftCode; }

    public String getBeneficiaryAccountType() { return beneficiaryAccountType; }
    public void setBeneficiaryAccountType(String beneficiaryAccountType) { this.beneficiaryAccountType = beneficiaryAccountType; }

    public Long getBeneficiaryAccountId() { return beneficiaryAccountId; }
    public void setBeneficiaryAccountId(Long beneficiaryAccountId) { this.beneficiaryAccountId = beneficiaryAccountId; }

    public Double getVisaFee() { return visaFee; }
    public void setVisaFee(Double visaFee) { this.visaFee = visaFee; }

    public Double getServiceCharge() { return serviceCharge; }
    public void setServiceCharge(Double serviceCharge) { this.serviceCharge = serviceCharge; }

    public Double getTotalReleasedAmount() { return totalReleasedAmount; }
    public void setTotalReleasedAmount(Double totalReleasedAmount) { this.totalReleasedAmount = totalReleasedAmount; }

    public String getTransactionReference() { return transactionReference; }
    public void setTransactionReference(String transactionReference) { this.transactionReference = transactionReference; }

    public LocalDate getTransferDate() { return transferDate; }
    public void setTransferDate(LocalDate transferDate) { this.transferDate = transferDate; }

    public String getReceiptUrl() { return receiptUrl; }
    public void setReceiptUrl(String receiptUrl) { this.receiptUrl = receiptUrl; }

    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }

    public String getReleasedBy() { return releasedBy; }
    public void setReleasedBy(String releasedBy) { this.releasedBy = releasedBy; }

    public LocalDateTime getReleasedDate() { return releasedDate; }
    public void setReleasedDate(LocalDateTime releasedDate) { this.releasedDate = releasedDate; }

    public String getReleaseStatus() { return releaseStatus; }
    public void setReleaseStatus(String releaseStatus) { this.releaseStatus = releaseStatus; }

    public String getRejectionReason() { return rejectionReason; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
