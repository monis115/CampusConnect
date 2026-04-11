package com.cc.modals;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
public class AccountantCredit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Double amount;
    private String method; // FRIEND, LOAN, CREDIT_CARD
    private LocalDate creditDate;
    private LocalDate dueDate;
    private Double interestRate;
    
    // Friend specific
    private String friendName;
    private String friendPhone;
    
    // Loan specific
    private String bankName;
    private Integer loanTenureMonths;
    
    // Credit Card specific
    private String cardInfo;
    private String emiDetails;

    private Double amountRepaid = 0.0;
    private String status; // ACTIVE, REPAID
    
    private String accountantUsername;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) {
            status = "ACTIVE";
        }
        if (amountRepaid == null) {
            amountRepaid = 0.0;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Double getAmount() { return amount; }
    public void setAmount(Double amount) { this.amount = amount; }

    public String getMethod() { return method; }
    public void setMethod(String method) { this.method = method; }

    public LocalDate getCreditDate() { return creditDate; }
    public void setCreditDate(LocalDate creditDate) { this.creditDate = creditDate; }

    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }

    public Double getInterestRate() { return interestRate; }
    public void setInterestRate(Double interestRate) { this.interestRate = interestRate; }

    public String getFriendName() { return friendName; }
    public void setFriendName(String friendName) { this.friendName = friendName; }

    public String getFriendPhone() { return friendPhone; }
    public void setFriendPhone(String friendPhone) { this.friendPhone = friendPhone; }

    public String getBankName() { return bankName; }
    public void setBankName(String bankName) { this.bankName = bankName; }

    public Integer getLoanTenureMonths() { return loanTenureMonths; }
    public void setLoanTenureMonths(Integer loanTenureMonths) { this.loanTenureMonths = loanTenureMonths; }

    public String getCardInfo() { return cardInfo; }
    public void setCardInfo(String cardInfo) { this.cardInfo = cardInfo; }

    public String getEmiDetails() { return emiDetails; }
    public void setEmiDetails(String emiDetails) { this.emiDetails = emiDetails; }

    public Double getAmountRepaid() { return amountRepaid; }
    public void setAmountRepaid(Double amountRepaid) { this.amountRepaid = amountRepaid; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getAccountantUsername() { return accountantUsername; }
    public void setAccountantUsername(String accountantUsername) { this.accountantUsername = accountantUsername; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
