package com.cc.modals;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "passbook_accountant_agent")
public class PassbookForAccountantAgent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "student_name", nullable = false)
    private String studentName;

    @Column(name = "application_id", nullable = false)
    private String applicationId;

    @Column(name = "mode_of_payment")
    private String modeOfPayment;

    @Column(name = "bank_details")
    private String bankDetails;

    @Column(name = "requested_amount")
    private double requestedAmountByAccountantToAgent;

    @Column(name = "amount_paid")
    private double amountPaidByAgent;

    @Column(name = "transaction_id")
    private String transactionIdOrUpiIdGivenByAgent;

    @Column(name = "amount_confirmed")
    private double amountConfirmedByAccountant;

    @Column(name = "request_datetime")
    private LocalDateTime requestDateTimeByAccountant;

    @Column(name = "payment_datetime")
    private LocalDateTime paymentDateTimeByAgent;

    @Column(name = "paid_by")
    private String paidByAgent;

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

    public String getModeOfPayment() {
        return modeOfPayment;
    }

    public void setModeOfPayment(String modeOfPayment) {
        this.modeOfPayment = modeOfPayment;
    }

    public String getBankDetails() {
        return bankDetails;
    }

    public void setBankDetails(String bankDetails) {
        this.bankDetails = bankDetails;
    }

    public double getRequestedAmountByAccountantToAgent() {
        return requestedAmountByAccountantToAgent;
    }

    public void setRequestedAmountByAccountantToAgent(double requestedAmountByAccountantToAgent) {
        this.requestedAmountByAccountantToAgent = requestedAmountByAccountantToAgent;
    }

    public double getAmountPaidByAgent() {
        return amountPaidByAgent;
    }

    public void setAmountPaidByAgent(double amountPaidByAgent) {
        this.amountPaidByAgent = amountPaidByAgent;
    }

    public String getTransactionIdOrUpiIdGivenByAgent() {
        return transactionIdOrUpiIdGivenByAgent;
    }

    public void setTransactionIdOrUpiIdGivenByAgent(String transactionIdOrUpiIdGivenByAgent) {
        this.transactionIdOrUpiIdGivenByAgent = transactionIdOrUpiIdGivenByAgent;
    }

    public double getAmountConfirmedByAccountant() {
        return amountConfirmedByAccountant;
    }

    public void setAmountConfirmedByAccountant(double amountConfirmedByAccountant) {
        this.amountConfirmedByAccountant = amountConfirmedByAccountant;
    }

    public LocalDateTime getRequestDateTimeByAccountant() {
        return requestDateTimeByAccountant;
    }

    public void setRequestDateTimeByAccountant(LocalDateTime requestDateTimeByAccountant) {
        this.requestDateTimeByAccountant = requestDateTimeByAccountant;
    }

    public LocalDateTime getPaymentDateTimeByAgent() {
        return paymentDateTimeByAgent;
    }

    public void setPaymentDateTimeByAgent(LocalDateTime paymentDateTimeByAgent) {
        this.paymentDateTimeByAgent = paymentDateTimeByAgent;
    }

    public String getPaidByAgent() {
        return paidByAgent;
    }

    public void setPaidByAgent(String paidByAgent) {
        this.paidByAgent = paidByAgent;
    }
}