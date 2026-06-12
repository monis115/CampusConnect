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
public class ReportGeneralLedgerGenerator implements ReportGenerator {

    private final SubAccountTransactionRepository transactionRepository;

    public ReportGeneralLedgerGenerator(SubAccountTransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    @Override
    public String getCategory() {
        return "financial-reports";
    }

    @Override
    public String getReportName() {
        return "general-ledger";
    }

    @Override
    public ReportResponseDTO generate(ReportRequest request) {
        ReportResponseDTO response = new ReportResponseDTO();

        // Columns
        List<ColumnDefDTO> columns = new ArrayList<>();
        columns.add(new ColumnDefDTO("date", "Date", "string"));
        columns.add(new ColumnDefDTO("account", "Account", "string"));
        columns.add(new ColumnDefDTO("description", "Description", "string"));
        columns.add(new ColumnDefDTO("debit", "Debit (₹)", "currency"));
        columns.add(new ColumnDefDTO("credit", "Credit (₹)", "currency"));
        columns.add(new ColumnDefDTO("balance", "Running Balance (₹)", "currency"));
        response.setColumns(columns);

        List<Map<String, Object>> data = new ArrayList<>();
        List<SubAccountTransaction> allTx = transactionRepository.findAll();
        allTx = DateFilterUtil.filter(allTx, request, SubAccountTransaction::getTransactionDate);

        double totalDebit = 0;
        double totalCredit = 0;
        double runningBalance = 0;

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        for (SubAccountTransaction tx : allTx) {
            Map<String, Object> row = new HashMap<>();
            row.put("date", tx.getTransactionDate() != null ? tx.getTransactionDate().format(formatter) : "");
            
            String accountName = "Unknown";
            if (tx.getSubAccount() != null) {
                accountName = tx.getSubAccount().getAccountType() + " - " + 
                              (tx.getSubAccount().getBankName() != null ? tx.getSubAccount().getBankName() : tx.getSubAccount().getCashAccountName());
            }
            row.put("account", accountName);
            
            row.put("description", tx.getDescription() != null ? tx.getDescription() : "Ledger Entry");

            double amt = tx.getAmount() != null ? tx.getAmount() : 0.0;
            
            // In accounting context for a cash/bank ledger:
            // Receiving money (Inflow) is a DEBIT to the Cash/Bank account.
            // Spending money (Outflow) is a CREDIT to the Cash/Bank account.
            // But from the software's literal enum "CREDIT" means money in, "DEBIT" means money out.
            // Let's stick to the software's domain mapping for ease of use by the admin:
            
            if ("CREDIT".equalsIgnoreCase(tx.getTransactionType())) { // Inflow
                row.put("debit", String.format("%.2f", amt)); // Debit the asset account
                row.put("credit", "0.00");
                runningBalance += amt;
                totalDebit += amt;
            } else { // Outflow
                row.put("debit", "0.00");
                row.put("credit", String.format("%.2f", amt)); // Credit the asset account
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
        summary.addMetric("Net Balance", String.format("₹%.2f", runningBalance));
        response.setSummary(summary);

        return response;
    }
}
