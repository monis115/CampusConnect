package com.cc.modals;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class CreditRepayment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "credit_id")
    private AccountantCredit credit;

    @ManyToOne
    @JoinColumn(name = "sub_account_id")
    private SubAccount subAccount;

    private Double amount;
    private LocalDateTime paymentDate;
    
    @Column(length = 1000)
    private String notes;

    @PrePersist
    protected void onCreate() {
        if (paymentDate == null) {
            paymentDate = LocalDateTime.now();
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

    public SubAccount getSubAccount() {
        return subAccount;
    }

    public void setSubAccount(SubAccount subAccount) {
        this.subAccount = subAccount;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public LocalDateTime getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(LocalDateTime paymentDate) {
        this.paymentDate = paymentDate;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
