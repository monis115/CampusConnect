package com.cc.DTO;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class FundReleaseDTO {

    private Long releaseId;
    private Long studentId;
    private String studentName;
    private String agentName;
    private String university;
    private String semester;
    private String sourceAccount;
    private String beneficiaryBankName;
    private String beneficiaryAccountNumber;
    private String beneficiarySwiftCode;
    private String beneficiaryAccountType;
    private Double visaFee;
    private Double serviceCharge;
    private Double totalReleasedAmount;
    private String transactionReference;
    private LocalDate transferDate;
    private String receiptUrl;
    private String remarks;
    private String releasedBy;
    private LocalDateTime releasedDate;
    private String releaseStatus;
    private String rejectionReason;

    // Getters and Setters

    public Long getReleaseId() { return releaseId; }
    public void setReleaseId(Long releaseId) { this.releaseId = releaseId; }

    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }

    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }

    public String getAgentName() { return agentName; }
    public void setAgentName(String agentName) { this.agentName = agentName; }

    public String getUniversity() { return university; }
    public void setUniversity(String university) { this.university = university; }

    public String getSemester() { return semester; }
    public void setSemester(String semester) { this.semester = semester; }

    public String getSourceAccount() { return sourceAccount; }
    public void setSourceAccount(String sourceAccount) { this.sourceAccount = sourceAccount; }

    public String getBeneficiaryBankName() { return beneficiaryBankName; }
    public void setBeneficiaryBankName(String beneficiaryBankName) { this.beneficiaryBankName = beneficiaryBankName; }

    public String getBeneficiaryAccountNumber() { return beneficiaryAccountNumber; }
    public void setBeneficiaryAccountNumber(String beneficiaryAccountNumber) { this.beneficiaryAccountNumber = beneficiaryAccountNumber; }

    public String getBeneficiarySwiftCode() { return beneficiarySwiftCode; }
    public void setBeneficiarySwiftCode(String beneficiarySwiftCode) { this.beneficiarySwiftCode = beneficiarySwiftCode; }

    public String getBeneficiaryAccountType() { return beneficiaryAccountType; }
    public void setBeneficiaryAccountType(String beneficiaryAccountType) { this.beneficiaryAccountType = beneficiaryAccountType; }

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
}
