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
    private String action;
    private String actionOnUser;

    public StudentPaymentSummaryDTO() {
    }

    public StudentPaymentSummaryDTO(Long studentId, String applicationId, String fullName, String email,
            String contactNo, String applicationStatus, String paymentStatus, Boolean hasPaid,
            Double paidAmount, String paidTo, String ttCopyPath, String paymentConfirmationPath,
            Double visaPaymentRequestedAmount, String visaPaymentRequestedCurrency,
            Double visaManServiceCharge, String visaPaymentTxId, String visaPaymentConfirmationPath,
            String action, String actionOnUser) {
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
        this.action = action;
        this.actionOnUser = actionOnUser;
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
}
