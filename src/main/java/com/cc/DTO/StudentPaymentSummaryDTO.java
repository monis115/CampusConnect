package com.cc.DTO;

public class StudentPaymentSummaryDTO {
    private Long studentId;
    private String applicationId;
    private String fullName;
    private String email;
    private String contactNo;
    private String applicationStatus;
    private String paymentStatus;
    private Boolean hasPaid;
    private Double paidAmount;
    private String paidTo;
    private String ttCopyPath;
    private String paymentConfirmationPath;
    private Double visaPaymentRequestedAmount;
    private String visaPaymentRequestedCurrency;
    private Double visaManServiceCharge;
    private String visaPaymentTxId;
    private String visaPaymentConfirmationPath;
    private Double visaPaymentReceivedAmount;
    private java.time.LocalDate visaPaymentReceivedDate;
    private String action;
    private String actionOnUser;
    private Double accToVisaManPaidAmount;
    private java.time.LocalDate accToVisaManPaymentDate;
    private String accToVisaManTxId;
    private String accToVisaManMode;
    private String accToVisaManStatus;
    private String agentFullName;
    private String collegeName;
    private String appliedFor;
    private String paymentRejectionRemarks;
    private String visaManFullName;
    private String agentUserFullName;

    public StudentPaymentSummaryDTO() {
    }

    public StudentPaymentSummaryDTO(Long studentId, String applicationId, String fullName, String email,
            String contactNo, String applicationStatus, String paymentStatus, Boolean hasPaid,
            Double paidAmount, String paidTo, String ttCopyPath, String paymentConfirmationPath,
            Double visaPaymentRequestedAmount, String visaPaymentRequestedCurrency,
            Double visaManServiceCharge, String visaPaymentTxId, String visaPaymentConfirmationPath,
            Double visaPaymentReceivedAmount, java.time.LocalDate visaPaymentReceivedDate,
            String action, String actionOnUser, Double accToVisaManPaidAmount,
            java.time.LocalDate accToVisaManPaymentDate, String accToVisaManTxId,
            String accToVisaManMode, String accToVisaManStatus,
            String agentFullName, String collegeName, String appliedFor,
            String paymentRejectionRemarks, String visaManFullName, String agentUserFullName) {
        this.studentId = studentId;
        this.applicationId = applicationId;
        this.fullName = fullName;
        this.email = email;
        this.contactNo = contactNo;
        this.applicationStatus = applicationStatus;
        this.paymentStatus = paymentStatus;
        this.hasPaid = hasPaid;
        this.paidAmount = paidAmount;
        this.paidTo = paidTo;
        this.ttCopyPath = ttCopyPath;
        this.paymentConfirmationPath = paymentConfirmationPath;
        this.visaPaymentRequestedAmount = visaPaymentRequestedAmount;
        this.visaPaymentRequestedCurrency = visaPaymentRequestedCurrency;
        this.visaManServiceCharge = visaManServiceCharge;
        this.visaPaymentTxId = visaPaymentTxId;
        this.visaPaymentConfirmationPath = visaPaymentConfirmationPath;
        this.visaPaymentReceivedAmount = visaPaymentReceivedAmount;
        this.visaPaymentReceivedDate = visaPaymentReceivedDate;
        this.action = action;
        this.actionOnUser = actionOnUser;
        this.accToVisaManPaidAmount = accToVisaManPaidAmount;
        this.accToVisaManPaymentDate = accToVisaManPaymentDate;
        this.accToVisaManTxId = accToVisaManTxId;
        this.accToVisaManMode = accToVisaManMode;
        this.accToVisaManStatus = accToVisaManStatus;
        this.agentFullName = agentFullName;
        this.collegeName = collegeName;
        this.appliedFor = appliedFor;
        this.paymentRejectionRemarks = paymentRejectionRemarks;
        this.visaManFullName = visaManFullName;
        this.agentUserFullName = agentUserFullName;
    }

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public String getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getContactNo() {
        return contactNo;
    }

    public void setContactNo(String contactNo) {
        this.contactNo = contactNo;
    }

    public String getApplicationStatus() {
        return applicationStatus;
    }

    public void setApplicationStatus(String applicationStatus) {
        this.applicationStatus = applicationStatus;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public Boolean getHasPaid() {
        return hasPaid;
    }

    public void setHasPaid(Boolean hasPaid) {
        this.hasPaid = hasPaid;
    }

    public Double getPaidAmount() {
        return paidAmount;
    }

    public void setPaidAmount(Double paidAmount) {
        this.paidAmount = paidAmount;
    }

    public String getPaidTo() {
        return paidTo;
    }

    public void setPaidTo(String paidTo) {
        this.paidTo = paidTo;
    }

    public String getTtCopyPath() {
        return ttCopyPath;
    }

    public void setTtCopyPath(String ttCopyPath) {
        this.ttCopyPath = ttCopyPath;
    }

    public String getPaymentConfirmationPath() {
        return paymentConfirmationPath;
    }

    public void setPaymentConfirmationPath(String paymentConfirmationPath) {
        this.paymentConfirmationPath = paymentConfirmationPath;
    }

    public Double getVisaPaymentRequestedAmount() {
        return visaPaymentRequestedAmount;
    }

    public void setVisaPaymentRequestedAmount(Double visaPaymentRequestedAmount) {
        this.visaPaymentRequestedAmount = visaPaymentRequestedAmount;
    }

    public String getVisaPaymentRequestedCurrency() {
        return visaPaymentRequestedCurrency;
    }

    public void setVisaPaymentRequestedCurrency(String visaPaymentRequestedCurrency) {
        this.visaPaymentRequestedCurrency = visaPaymentRequestedCurrency;
    }

    public Double getVisaManServiceCharge() {
        return visaManServiceCharge;
    }

    public void setVisaManServiceCharge(Double visaManServiceCharge) {
        this.visaManServiceCharge = visaManServiceCharge;
    }

    public String getVisaPaymentTxId() {
        return visaPaymentTxId;
    }

    public void setVisaPaymentTxId(String visaPaymentTxId) {
        this.visaPaymentTxId = visaPaymentTxId;
    }

    public String getVisaPaymentConfirmationPath() {
        return visaPaymentConfirmationPath;
    }

    public void setVisaPaymentConfirmationPath(String visaPaymentConfirmationPath) {
        this.visaPaymentConfirmationPath = visaPaymentConfirmationPath;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getActionOnUser() {
        return actionOnUser;
    }

    public void setActionOnUser(String actionOnUser) {
        this.actionOnUser = actionOnUser;
    }

    public Double getVisaPaymentReceivedAmount() {
        return visaPaymentReceivedAmount;
    }

    public void setVisaPaymentReceivedAmount(Double visaPaymentReceivedAmount) {
        this.visaPaymentReceivedAmount = visaPaymentReceivedAmount;
    }

    public java.time.LocalDate getVisaPaymentReceivedDate() {
        return visaPaymentReceivedDate;
    }

    public void setVisaPaymentReceivedDate(java.time.LocalDate visaPaymentReceivedDate) {
        this.visaPaymentReceivedDate = visaPaymentReceivedDate;
    }

    public Double getAccToVisaManPaidAmount() {
        return accToVisaManPaidAmount;
    }

    public void setAccToVisaManPaidAmount(Double accToVisaManPaidAmount) {
        this.accToVisaManPaidAmount = accToVisaManPaidAmount;
    }

    public java.time.LocalDate getAccToVisaManPaymentDate() {
        return accToVisaManPaymentDate;
    }

    public void setAccToVisaManPaymentDate(java.time.LocalDate accToVisaManPaymentDate) {
        this.accToVisaManPaymentDate = accToVisaManPaymentDate;
    }

    public String getAccToVisaManTxId() {
        return accToVisaManTxId;
    }

    public void setAccToVisaManTxId(String accToVisaManTxId) {
        this.accToVisaManTxId = accToVisaManTxId;
    }

    public String getAccToVisaManMode() {
        return accToVisaManMode;
    }

    public void setAccToVisaManMode(String accToVisaManMode) {
        this.accToVisaManMode = accToVisaManMode;
    }

    public String getAccToVisaManStatus() {
        return accToVisaManStatus;
    }

    public void setAccToVisaManStatus(String accToVisaManStatus) {
        this.accToVisaManStatus = accToVisaManStatus;
    }

    public String getAgentFullName() { return agentFullName; }
    public void setAgentFullName(String agentFullName) { this.agentFullName = agentFullName; }

    public String getCollegeName() { return collegeName; }
    public void setCollegeName(String collegeName) { this.collegeName = collegeName; }

    public String getAppliedFor() { return appliedFor; }
    public void setAppliedFor(String appliedFor) { this.appliedFor = appliedFor; }

    public String getPaymentRejectionRemarks() { return paymentRejectionRemarks; }
    public void setPaymentRejectionRemarks(String paymentRejectionRemarks) { this.paymentRejectionRemarks = paymentRejectionRemarks; }

    public String getVisaManFullName() { return visaManFullName; }
    public void setVisaManFullName(String visaManFullName) { this.visaManFullName = visaManFullName; }

    public String getAgentUserFullName() { return agentUserFullName; }
    public void setAgentUserFullName(String agentUserFullName) { this.agentUserFullName = agentUserFullName; }
}
