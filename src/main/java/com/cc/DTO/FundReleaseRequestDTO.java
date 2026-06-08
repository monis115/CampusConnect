package com.cc.DTO;

import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

public class FundReleaseRequestDTO {

    private Long studentId;
    private Long sourceAccountId;
    private Long beneficiaryAccountId;
    private String transactionReference;
    private LocalDate transferDate;
    private Long collectionRequestId;
    private String remarks;

    // Getters and Setters
    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }

    public Long getSourceAccountId() { return sourceAccountId; }
    public void setSourceAccountId(Long sourceAccountId) { this.sourceAccountId = sourceAccountId; }

    public Long getBeneficiaryAccountId() { return beneficiaryAccountId; }
    public void setBeneficiaryAccountId(Long beneficiaryAccountId) { this.beneficiaryAccountId = beneficiaryAccountId; }

    public String getTransactionReference() { return transactionReference; }
    public void setTransactionReference(String transactionReference) { this.transactionReference = transactionReference; }

    public LocalDate getTransferDate() { return transferDate; }
    public void setTransferDate(LocalDate transferDate) { this.transferDate = transferDate; }

    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }

    public Long getCollectionRequestId() { return collectionRequestId; }
    public void setCollectionRequestId(Long collectionRequestId) { this.collectionRequestId = collectionRequestId; }
}
