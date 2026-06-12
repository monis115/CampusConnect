package com.cc.DTO;

import java.util.List;
import java.util.Map;

public class UserTransactionSummaryDTO {
    private double totalInflow;
    private double totalOutflow;
    private double balance;
    private List<Map<String, Object>> transactions;

    public double getTotalInflow() { return totalInflow; }
    public void setTotalInflow(double totalInflow) { this.totalInflow = totalInflow; }

    public double getTotalOutflow() { return totalOutflow; }
    public void setTotalOutflow(double totalOutflow) { this.totalOutflow = totalOutflow; }

    public double getBalance() { return balance; }
    public void setBalance(double balance) { this.balance = balance; }

    public List<Map<String, Object>> getTransactions() { return transactions; }
    public void setTransactions(List<Map<String, Object>> transactions) { this.transactions = transactions; }
}
