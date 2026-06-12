package com.cc.modals;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
public class CreditInstallment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "credit_id")
    private AccountantCredit credit;

    private Integer installmentNum;
    private LocalDate dueDate;
    
    @Column(precision = 19, scale = 4)
    private BigDecimal installmentAmount;
    
    @Column(precision = 19, scale = 4)
    private BigDecimal principalPortion;
    
    @Column(precision = 19, scale = 4)
    private BigDecimal interestPortion;
    
    @Column(precision = 19, scale = 4)
    private BigDecimal remainingPrincipal;
    
    @Column(precision = 19, scale = 4)
    private BigDecimal remainingInterest;
    
    @Column(precision = 19, scale = 4)
    private BigDecimal paidAmount = BigDecimal.ZERO;
    
    // PENDING, PAID, OVERDUE
    private String status = "PENDING";
    private LocalDateTime paidDate;

    @Column(precision = 19, scale = 4)
    private BigDecimal penaltyAmount = BigDecimal.ZERO;

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

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public BigDecimal getInstallmentAmount() {
        return installmentAmount;
    }

    public void setInstallmentAmount(BigDecimal installmentAmount) {
        this.installmentAmount = installmentAmount;
    }

    public BigDecimal getPrincipalPortion() {
        return principalPortion;
    }

    public void setPrincipalPortion(BigDecimal principalPortion) {
        this.principalPortion = principalPortion;
    }

    public BigDecimal getInterestPortion() {
        return interestPortion;
    }

    public void setInterestPortion(BigDecimal interestPortion) {
        this.interestPortion = interestPortion;
    }

    public BigDecimal getRemainingPrincipal() {
        return remainingPrincipal;
    }

    public void setRemainingPrincipal(BigDecimal remainingPrincipal) {
        this.remainingPrincipal = remainingPrincipal;
    }

    public BigDecimal getRemainingInterest() {
        return remainingInterest;
    }

    public void setRemainingInterest(BigDecimal remainingInterest) {
        this.remainingInterest = remainingInterest;
    }

    public BigDecimal getPaidAmount() {
        return paidAmount;
    }

    public void setPaidAmount(BigDecimal paidAmount) {
        this.paidAmount = paidAmount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getPaidDate() {
        return paidDate;
    }

    public void setPaidDate(LocalDateTime paidDate) {
        this.paidDate = paidDate;
    }

    public Integer getInstallmentNum() {
        return installmentNum;
    }

    public void setInstallmentNum(Integer installmentNum) {
        this.installmentNum = installmentNum;
    }

    public BigDecimal getPenaltyAmount() {
        return penaltyAmount;
    }

    public void setPenaltyAmount(BigDecimal penaltyAmount) {
        this.penaltyAmount = penaltyAmount;
    }
}
