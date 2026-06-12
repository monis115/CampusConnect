package com.cc.modals;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
public class AccountantCredit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(precision = 19, scale = 4)
    private BigDecimal amount;
    
    private String method; // FRIEND, LOAN, CREDIT_CARD
    private LocalDate creditDate;
    private LocalDate dueDate;
    
    @Column(precision = 19, scale = 4)
    private BigDecimal interestRate;

    // Friend specific
    private String friendName;
    private String friendPhone;

    // Loan specific
    private String bankName;
    private Integer loanTenureMonths;

    // Credit Card specific
    private String cardInfo;
    private String emiDetails;
    
    @Column(precision = 19, scale = 4)
    private BigDecimal cardLimit;
    private LocalDate statementDate;

    @Column(precision = 19, scale = 4)
    private BigDecimal amountRepaid = BigDecimal.ZERO;
    
    private String status; // ACTIVE, CLOSED, FORECLOSED
    private String interestModel; // FLAT, REDUCING
    private String healthScore; // HEALTHY, ATTENTION, CRITICAL

    private String accountantUsername;
    private Long subAccountId;

    @Column(precision = 19, scale = 4)
    private BigDecimal outstandingAmount;
    
    private String repaymentStructure;
    
    @Column(precision = 19, scale = 4)
    private BigDecimal totalRepaymentAmount;
    
    @Column(precision = 19, scale = 4)
    private BigDecimal installmentAmount;
    
    @Column(length = 1000)
    private String purpose;

    private String lateFeeType; // PERCENTAGE, FIXED_AMOUNT

    @Column(precision = 19, scale = 4)
    private BigDecimal lateFeeValue;

    @Column(precision = 19, scale = 4)
    private BigDecimal fixedLateFine;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) {
            status = "ACTIVE";
        }
        if (healthScore == null) {
            healthScore = "HEALTHY";
        }
        if (interestModel == null) {
            interestModel = "REDUCING";
        }
        if (amountRepaid == null) {
            amountRepaid = BigDecimal.ZERO;
        }
        if (outstandingAmount == null && totalRepaymentAmount != null) {
            outstandingAmount = totalRepaymentAmount;
        } else if (outstandingAmount == null && amount != null) {
            outstandingAmount = amount;
        } else if (outstandingAmount == null) {
            outstandingAmount = BigDecimal.ZERO;
        }
        if (lateFeeType == null) {
            lateFeeType = "PERCENTAGE";
        }
        if (lateFeeValue == null) {
            lateFeeValue = BigDecimal.ZERO;
        }
        if (fixedLateFine == null) {
            fixedLateFine = BigDecimal.ZERO;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public LocalDate getCreditDate() {
        return creditDate;
    }

    public void setCreditDate(LocalDate creditDate) {
        this.creditDate = creditDate;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public BigDecimal getInterestRate() {
        return interestRate;
    }

    public void setInterestRate(BigDecimal interestRate) {
        this.interestRate = interestRate;
    }

    public String getFriendName() {
        return friendName;
    }

    public void setFriendName(String friendName) {
        this.friendName = friendName;
    }

    public String getFriendPhone() {
        return friendPhone;
    }

    public void setFriendPhone(String friendPhone) {
        this.friendPhone = friendPhone;
    }

    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    public Integer getLoanTenureMonths() {
        return loanTenureMonths;
    }

    public void setLoanTenureMonths(Integer loanTenureMonths) {
        this.loanTenureMonths = loanTenureMonths;
    }

    public String getCardInfo() {
        return cardInfo;
    }

    public void setCardInfo(String cardInfo) {
        this.cardInfo = cardInfo;
    }

    public String getEmiDetails() {
        return emiDetails;
    }

    public void setEmiDetails(String emiDetails) {
        this.emiDetails = emiDetails;
    }

    public BigDecimal getCardLimit() {
        return cardLimit;
    }

    public void setCardLimit(BigDecimal cardLimit) {
        this.cardLimit = cardLimit;
    }

    public LocalDate getStatementDate() {
        return statementDate;
    }

    public void setStatementDate(LocalDate statementDate) {
        this.statementDate = statementDate;
    }

    public BigDecimal getAmountRepaid() {
        return amountRepaid;
    }

    public void setAmountRepaid(BigDecimal amountRepaid) {
        this.amountRepaid = amountRepaid;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getInterestModel() {
        return interestModel;
    }

    public void setInterestModel(String interestModel) {
        this.interestModel = interestModel;
    }

    public String getHealthScore() {
        return healthScore;
    }

    public void setHealthScore(String healthScore) {
        this.healthScore = healthScore;
    }

    public String getAccountantUsername() {
        return accountantUsername;
    }

    public void setAccountantUsername(String accountantUsername) {
        this.accountantUsername = accountantUsername;
    }

    public Long getSubAccountId() {
        return subAccountId;
    }

    public void setSubAccountId(Long subAccountId) {
        this.subAccountId = subAccountId;
    }

    public BigDecimal getOutstandingAmount() {
        return outstandingAmount;
    }

    public void setOutstandingAmount(BigDecimal outstandingAmount) {
        this.outstandingAmount = outstandingAmount;
    }

    public String getRepaymentStructure() {
        return repaymentStructure;
    }

    public void setRepaymentStructure(String repaymentStructure) {
        this.repaymentStructure = repaymentStructure;
    }

    public BigDecimal getTotalRepaymentAmount() {
        return totalRepaymentAmount;
    }

    public void setTotalRepaymentAmount(BigDecimal totalRepaymentAmount) {
        this.totalRepaymentAmount = totalRepaymentAmount;
    }

    public BigDecimal getInstallmentAmount() {
        return installmentAmount;
    }

    public void setInstallmentAmount(BigDecimal installmentAmount) {
        this.installmentAmount = installmentAmount;
    }

    public String getPurpose() {
        return purpose;
    }

    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getLateFeeType() {
        return lateFeeType;
    }

    public void setLateFeeType(String lateFeeType) {
        this.lateFeeType = lateFeeType;
    }

    public BigDecimal getLateFeeValue() {
        return lateFeeValue;
    }

    public void setLateFeeValue(BigDecimal lateFeeValue) {
        this.lateFeeValue = lateFeeValue;
    }

    public BigDecimal getFixedLateFine() {
        return fixedLateFine;
    }

    public void setFixedLateFine(BigDecimal fixedLateFine) {
        this.fixedLateFine = fixedLateFine;
    }
}
