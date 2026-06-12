package com.cc.modals;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
public class LoanLedger {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "credit_id")
    private AccountantCredit credit;

    private String transactionId;

    // CREDIT, DEBIT, PARTIAL_PAYMENT, FORECLOSURE, PENALTY, ACCRUAL
    private String transactionType;

    @Column(precision = 19, scale = 4)
    private BigDecimal principalComponent = BigDecimal.ZERO;

    @Column(precision = 19, scale = 4)
    private BigDecimal interestComponent = BigDecimal.ZERO;

    @Column(precision = 19, scale = 4)
    private BigDecimal penaltyComponent = BigDecimal.ZERO;

    @Column(precision = 19, scale = 4)
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Column(precision = 19, scale = 4)
    private BigDecimal balanceBefore = BigDecimal.ZERO;

    @Column(precision = 19, scale = 4)
    private BigDecimal balanceAfter = BigDecimal.ZERO;

    private String recordedBy;
    private LocalDateTime timestamp;

    @PrePersist
    protected void onCreate() {
        timestamp = LocalDateTime.now();
        if (transactionId == null) {
            transactionId = UUID.randomUUID().toString();
        }
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public AccountantCredit getCredit() {
        return credit;
    }

    public void setCredit(AccountantCredit credit) {
        this.credit = credit;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(String transactionType) {
        this.transactionType = transactionType;
    }

    public BigDecimal getPrincipalComponent() {
        return principalComponent;
    }

    public void setPrincipalComponent(BigDecimal principalComponent) {
        this.principalComponent = principalComponent;
    }

    public BigDecimal getInterestComponent() {
        return interestComponent;
    }

    public void setInterestComponent(BigDecimal interestComponent) {
        this.interestComponent = interestComponent;
    }

    public BigDecimal getPenaltyComponent() {
        return penaltyComponent;
    }

    public void setPenaltyComponent(BigDecimal penaltyComponent) {
        this.penaltyComponent = penaltyComponent;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public BigDecimal getBalanceBefore() {
        return balanceBefore;
    }

    public void setBalanceBefore(BigDecimal balanceBefore) {
        this.balanceBefore = balanceBefore;
    }

    public BigDecimal getBalanceAfter() {
        return balanceAfter;
    }

    public void setBalanceAfter(BigDecimal balanceAfter) {
        this.balanceAfter = balanceAfter;
    }

    public String getRecordedBy() {
        return recordedBy;
    }

    public void setRecordedBy(String recordedBy) {
        this.recordedBy = recordedBy;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
