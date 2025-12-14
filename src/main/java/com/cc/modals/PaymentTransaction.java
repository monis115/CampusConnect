package com.cc.modals;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.LocalDateTime;

@Entity
@Table(name = "payment_transaction")
public class PaymentTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Double amount;

    @Column(nullable = false)
    private String paymentMode; // Cash, UPI, Bank Transfer

    private String transactionId; // Optional for Cash

    @Column(nullable = false)
    private LocalDateTime paymentDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "request_money_id", nullable = false)
    @JsonIgnoreProperties("transactions")
    private RequestMoney requestMoney;

    public PaymentTransaction() {
        this.paymentDate = LocalDateTime.now();
    }

    public PaymentTransaction(Double amount, String paymentMode, String transactionId, RequestMoney requestMoney) {
        this.amount = amount;
        this.paymentMode = paymentMode;
        this.transactionId = transactionId;
        this.requestMoney = requestMoney;
        this.paymentDate = LocalDateTime.now();
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public String getPaymentMode() {
        return paymentMode;
    }

    public void setPaymentMode(String paymentMode) {
        this.paymentMode = paymentMode;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public LocalDateTime getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(LocalDateTime paymentDate) {
        this.paymentDate = paymentDate;
    }

    public RequestMoney getRequestMoney() {
        return requestMoney;
    }

    public void setRequestMoney(RequestMoney requestMoney) {
        this.requestMoney = requestMoney;
    }
}
