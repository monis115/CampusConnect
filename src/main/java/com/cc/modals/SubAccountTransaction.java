package com.cc.modals;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class SubAccountTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "sub_account_id")
    private SubAccount subAccount;

    private String transactionType; // CREDIT or DEBIT
    private Double amount;
    private LocalDateTime transactionDate;
    private String description;
    private String referenceId; // e.g., txId or applicationId
    private String paymentMethod;

    public SubAccountTransaction() {
    }

    public SubAccountTransaction(SubAccount subAccount, String transactionType, Double amount, String description,
            String referenceId, String paymentMethod) {
        this.subAccount = subAccount;
        this.transactionType = transactionType;
        this.amount = amount;
        this.description = description;
        this.referenceId = referenceId;
        this.paymentMethod = paymentMethod;
        this.transactionDate = LocalDateTime.now();
    }

    public SubAccountTransaction(SubAccount subAccount, String transactionType, Double amount, String description,
            String referenceId) {
        this(subAccount, transactionType, amount, description, referenceId, "UNKNOWN");
    }


    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public SubAccount getSubAccount() {
        return subAccount;
    }

    public void setSubAccount(SubAccount subAccount) {
        this.subAccount = subAccount;
    }

    public String getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(String transactionType) {
        this.transactionType = transactionType;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public LocalDateTime getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(LocalDateTime transactionDate) {
        this.transactionDate = transactionDate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getReferenceId() {
        return referenceId;
    }

    public void setReferenceId(String referenceId) {
        this.referenceId = referenceId;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }
}
