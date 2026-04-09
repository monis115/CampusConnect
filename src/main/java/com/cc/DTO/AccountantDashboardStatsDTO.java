package com.cc.DTO;

public class AccountantDashboardStatsDTO {
    private Long totalStudentsAssigned;
    private Long pendingPayments;
    private Long paymentsReceived;
    private Long paymentsNotReceived;
    private Double totalAmountReceived;
    private Double totalAmountPending;
    private Double totalVisaRequestedByVisaMan;
    private Double totalAccToVisaManPaidAmount;

    public AccountantDashboardStatsDTO() {
    }

    public AccountantDashboardStatsDTO(Long totalStudentsAssigned, Long pendingPayments, Long paymentsReceived,
            Long paymentsNotReceived, Double totalAmountReceived, Double totalAmountPending,
            Double totalVisaRequestedByVisaMan, Double totalAccToVisaManPaidAmount) {
        this.totalStudentsAssigned = totalStudentsAssigned;
        this.pendingPayments = pendingPayments;
        this.paymentsReceived = paymentsReceived;
        this.paymentsNotReceived = paymentsNotReceived;
        this.totalAmountReceived = totalAmountReceived;
        this.totalAmountPending = totalAmountPending;
        this.totalVisaRequestedByVisaMan = totalVisaRequestedByVisaMan;
        this.totalAccToVisaManPaidAmount = totalAccToVisaManPaidAmount;
    }

    public Long getTotalStudentsAssigned() {
        return totalStudentsAssigned;
    }

    public void setTotalStudentsAssigned(Long totalStudentsAssigned) {
        this.totalStudentsAssigned = totalStudentsAssigned;
    }

    public Long getPendingPayments() {
        return pendingPayments;
    }

    public void setPendingPayments(Long pendingPayments) {
        this.pendingPayments = pendingPayments;
    }

    public Long getPaymentsReceived() {
        return paymentsReceived;
    }

    public void setPaymentsReceived(Long paymentsReceived) {
        this.paymentsReceived = paymentsReceived;
    }

    public Long getPaymentsNotReceived() {
        return paymentsNotReceived;
    }

    public void setPaymentsNotReceived(Long paymentsNotReceived) {
        this.paymentsNotReceived = paymentsNotReceived;
    }

    public Double getTotalAmountReceived() {
        return totalAmountReceived;
    }

    public void setTotalAmountReceived(Double totalAmountReceived) {
        this.totalAmountReceived = totalAmountReceived;
    }

    public Double getTotalAmountPending() {
        return totalAmountPending;
    }

    public void setTotalAmountPending(Double totalAmountPending) {
        this.totalAmountPending = totalAmountPending;
    }

    public Double getTotalVisaRequestedByVisaMan() {
        return totalVisaRequestedByVisaMan;
    }

    public void setTotalVisaRequestedByVisaMan(Double totalVisaRequestedByVisaMan) {
        this.totalVisaRequestedByVisaMan = totalVisaRequestedByVisaMan;
    }

    public Double getTotalAccToVisaManPaidAmount() {
        return totalAccToVisaManPaidAmount;
    }

    public void setTotalAccToVisaManPaidAmount(Double totalAccToVisaManPaidAmount) {
        this.totalAccToVisaManPaidAmount = totalAccToVisaManPaidAmount;
    }
}
