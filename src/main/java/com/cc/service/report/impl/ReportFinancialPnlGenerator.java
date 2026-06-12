package com.cc.service.report.impl;

import com.cc.DTO.report.ColumnDefDTO;
import com.cc.DTO.report.ReportRequest;
import com.cc.service.report.impl.DateFilterUtil;
import com.cc.DTO.report.ReportResponseDTO;
import com.cc.DTO.report.ReportSummaryDTO;
import com.cc.modals.SubAccountTransaction;
import com.cc.repository.SubAccountTransactionRepository;
import com.cc.service.report.ReportGenerator;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ReportFinancialPnlGenerator implements ReportGenerator {

    private final SubAccountTransactionRepository transactionRepository;

    public ReportFinancialPnlGenerator(SubAccountTransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    @Override
    public String getCategory() {
        return "financial-reports";
    }

    @Override
    public String getReportName() {
        return "profit-loss-statement";
    }

    @Override
    public ReportResponseDTO generate(ReportRequest request) {
        ReportResponseDTO response = new ReportResponseDTO();

        // 1. Columns
        List<ColumnDefDTO> columns = new ArrayList<>();
        columns.add(new ColumnDefDTO("month", "Month", "string"));
        columns.add(new ColumnDefDTO("revenue", "Gross Revenue (₹)", "currency"));
        columns.add(new ColumnDefDTO("expense", "Total Expense (₹)", "currency"));
        columns.add(new ColumnDefDTO("netProfit", "Net Profit (₹)", "currency"));
        response.setColumns(columns);

        // 2. Data
        List<SubAccountTransaction> allTransactions = transactionRepository.findAll();
        allTransactions = DateFilterUtil.filter(allTransactions, request, SubAccountTransaction::getTransactionDate);
        
        // Group by Month-Year (e.g., "Jan 2026")
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM yyyy", Locale.ENGLISH);
        
        Map<String, double[]> monthlyData = new LinkedHashMap<>();
        
        java.time.LocalDate start = request.getStartDate() != null && !request.getStartDate().isEmpty() 
            ? java.time.LocalDate.parse(request.getStartDate()) : null;
        java.time.LocalDate end = request.getEndDate() != null && !request.getEndDate().isEmpty() 
            ? java.time.LocalDate.parse(request.getEndDate()) : null;
            
        for (SubAccountTransaction tx : allTransactions) {
            if (tx.getTransactionDate() == null) continue;
            
            java.time.LocalDate txDate = tx.getTransactionDate().toLocalDate();
            if (start != null && txDate.isBefore(start)) continue;
            if (end != null && txDate.isAfter(end)) continue;
            
            String monthKey = tx.getTransactionDate().format(formatter);
            double amount = tx.getAmount() != null ? tx.getAmount() : 0.0;
            
            monthlyData.putIfAbsent(monthKey, new double[]{0.0, 0.0}); // [Revenue, Expense]
            
            if ("CREDIT".equalsIgnoreCase(tx.getTransactionType())) {
                monthlyData.get(monthKey)[0] += amount;
            } else if ("DEBIT".equalsIgnoreCase(tx.getTransactionType())) {
                monthlyData.get(monthKey)[1] += amount;
            }
        }

        List<Map<String, Object>> data = new ArrayList<>();
        double totalRev = 0;
        double totalExp = 0;

        for (Map.Entry<String, double[]> entry : monthlyData.entrySet()) {
            Map<String, Object> row = new HashMap<>();
            row.put("month", entry.getKey());
            
            double rev = entry.getValue()[0];
            double exp = entry.getValue()[1];
            
            row.put("revenue", String.format("%.2f", rev));
            row.put("expense", String.format("%.2f", exp));
            row.put("netProfit", String.format("%.2f", rev - exp));
            data.add(row);
            
            totalRev += rev;
            totalExp += exp;
        }
        
        // If there's no data, let's at least show the current month with 0
        if (data.isEmpty()) {
            Map<String, Object> row = new HashMap<>();
            row.put("month", java.time.LocalDate.now().format(formatter));
            row.put("revenue", "0.00");
            row.put("expense", "0.00");
            row.put("netProfit", "0.00");
            data.add(row);
        }

        response.setData(data);
        response.setTotalRecords(data.size());

        // 3. Summary
        ReportSummaryDTO summary = new ReportSummaryDTO();
        summary.addMetric("Total Revenue", String.format("₹%.2f", totalRev));
        summary.addMetric("Total Expenses", String.format("₹%.2f", totalExp));
        summary.addMetric("Net Profit", String.format("₹%.2f", totalRev - totalExp));
        response.setSummary(summary);

        return response;
    }
}
