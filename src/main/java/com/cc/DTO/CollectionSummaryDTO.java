package com.cc.DTO;

import java.time.LocalDate;

public class CollectionSummaryDTO {
    private String period; // "TODAY", "THIS_WEEK", "THIS_MONTH", "THIS_YEAR"
    private LocalDate startDate;
    private LocalDate endDate;
    private Long transactionCount;
    private Double totalAmount;

    public CollectionSummaryDTO() {
    }

    public CollectionSummaryDTO(String period, LocalDate startDate, LocalDate endDate, Long transactionCount,
            Double totalAmount) {
        this.period = period;
        this.startDate = startDate;
        this.endDate = endDate;
        this.transactionCount = transactionCount;
        this.totalAmount = totalAmount;
    }

    public String getPeriod() {
        return period;
    }

    public void setPeriod(String period) {
        this.period = period;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public Long getTransactionCount() {
        return transactionCount;
    }

    public void setTransactionCount(Long transactionCount) {
        this.transactionCount = transactionCount;
    }

    public Double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(Double totalAmount) {
        this.totalAmount = totalAmount;
    }
}
