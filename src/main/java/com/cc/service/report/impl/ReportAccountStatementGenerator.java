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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ReportAccountStatementGenerator implements ReportGenerator {

    private final SubAccountTransactionRepository transactionRepository;

    public ReportAccountStatementGenerator(SubAccountTransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    @Override
    public String getCategory() {
        return "account-reports";
    }

    @Override
    public String getReportName() {
        return "account-statement";
    }

    @Override
    public ReportResponseDTO generate(ReportRequest request) {
        ReportResponseDTO response = new ReportResponseDTO();

        List<ColumnDefDTO> columns = new ArrayList<>();
        columns.add(new ColumnDefDTO("date", "Date", "string"));
        columns.add(new ColumnDefDTO("accountName", "Account", "string"));
        columns.add(new ColumnDefDTO("description", "Description", "string"));
        columns.add(new ColumnDefDTO("debit", "Debit (₹)", "currency"));
        columns.add(new ColumnDefDTO("credit", "Credit (₹)", "currency"));
        columns.add(new ColumnDefDTO("balance", "Balance (₹)", "currency"));
        response.setColumns(columns);

        List<SubAccountTransaction> allTx = transactionRepository.findAll();
        allTx = DateFilterUtil.filter(allTx, request, SubAccountTransaction::getTransactionDate);
        List<Map<String, Object>> data = new ArrayList<>();

        double runningBalance = 0;
        double totalDebit = 0;
        double totalCredit = 0;

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        for (SubAccountTransaction tx : allTx) {
            Map<String, Object> row = new HashMap<>();
            row.put("date", tx.getTransactionDate() != null ? tx.getTransactionDate().format(formatter) : "");
            
            String accountName = "Unknown";
            if (tx.getSubAccount() != null) {
                accountName = tx.getSubAccount().getAccountType() + " - " + 
                              (tx.getSubAccount().getBankName() != null ? tx.getSubAccount().getBankName() : tx.getSubAccount().getCashAccountName());
            }
            row.put("accountName", accountName);
            
            row.put("description", tx.getDescription() != null ? tx.getDescription() : "-");

            double amt = tx.getAmount() != null ? tx.getAmount() : 0.0;
            
            if ("CREDIT".equalsIgnoreCase(tx.getTransactionType())) { // Inflow -> Debit asset
                row.put("debit", String.format("%.2f", amt));
                row.put("credit", "0.00");
                runningBalance += amt;
                totalDebit += amt;
            } else { // Outflow -> Credit asset
                row.put("debit", "0.00");
                row.put("credit", String.format("%.2f", amt));
                runningBalance -= amt;
                totalCredit += amt;
            }
            
            row.put("balance", String.format("%.2f", runningBalance));
            data.add(row);
        }

        response.setData(data);
        response.setTotalRecords(data.size());

        ReportSummaryDTO summary = new ReportSummaryDTO();
        summary.addMetric("Total Debits", String.format("₹%.2f", totalDebit));
        summary.addMetric("Total Credits", String.format("₹%.2f", totalCredit));
        summary.addMetric("Closing Balance", String.format("₹%.2f", runningBalance));
        response.setSummary(summary);

        return response;
    }
}
