package com.cc.modals;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "passbook_visaman_accountant")
public class PassbookVisaManAccountant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "student_name", nullable = false)
    private String studentName;

    @Column(name = "application_id", nullable = false)
    private String applicationId;

    @Column(name = "visa_amount_request")
    private double visaAmountRequest;

    @Column(name = "service_charge_requested")
    private double serviceChargeRequested;

    @Column(name = "requested_datetime")
    private LocalDateTime requestedDateTime;

    @Column(name = "total_amount_paid")
    private double totalAmountPaidByAccountant;

    @Column(name = "accountant_payment_datetime")
    private LocalDateTime accountantDateTimePaid;

    @Column(name = "status")
    private String status;

    @Column(name = "mode_of_payment")
    private String modeOfPayment;

    @Column(name = "requester_name")
    private String requesterName;

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }

    public double getVisaAmountRequest() {
        return visaAmountRequest;
    }

    public void setVisaAmountRequest(double visaAmountRequest) {
        this.visaAmountRequest = visaAmountRequest;
    }

    public double getServiceChargeRequested() {
        return serviceChargeRequested;
    }

    public void setServiceChargeRequested(double serviceChargeRequested) {
        this.serviceChargeRequested = serviceChargeRequested;
    }

    public LocalDateTime getRequestedDateTime() {
        return requestedDateTime;
    }

    public void setRequestedDateTime(LocalDateTime requestedDateTime) {
        this.requestedDateTime = requestedDateTime;
    }

    public double getTotalAmountPaidByAccountant() {
        return totalAmountPaidByAccountant;
    }

    public void setTotalAmountPaidByAccountant(double totalAmountPaidByAccountant) {
        this.totalAmountPaidByAccountant = totalAmountPaidByAccountant;
    }

    public LocalDateTime getAccountantDateTimePaid() {
        return accountantDateTimePaid;
    }

    public void setAccountantDateTimePaid(LocalDateTime accountantDateTimePaid) {
        this.accountantDateTimePaid = accountantDateTimePaid;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getModeOfPayment() {
        return modeOfPayment;
    }

    public void setModeOfPayment(String modeOfPayment) {
        this.modeOfPayment = modeOfPayment;
    }

    public String getRequesterName() {
        return requesterName;
    }

    public void setRequesterName(String requesterName) {
        this.requesterName = requesterName;
    }
}