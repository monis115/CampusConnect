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
public class ReportOutflowReportGenerator implements ReportGenerator {

    private final SubAccountTransactionRepository transactionRepository;

    public ReportOutflowReportGenerator(SubAccountTransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    @Override
    public String getCategory() {
        return "transaction-reports";
    }

    @Override
    public String getReportName() {
        return "outflow-report";
    }

    @Override
    public ReportResponseDTO generate(ReportRequest request) {
        ReportResponseDTO response = new ReportResponseDTO();

        List<ColumnDefDTO> columns = new ArrayList<>();
        columns.add(new ColumnDefDTO("date", "Date", "string"));
        columns.add(new ColumnDefDTO("destination", "Destination Account", "string"));
        columns.add(new ColumnDefDTO("amount", "Amount (₹)", "currency"));
        columns.add(new ColumnDefDTO("method", "Method", "string"));
        columns.add(new ColumnDefDTO("reference", "Reference ID", "string"));
        response.setColumns(columns);

        List<SubAccountTransaction> allTx = transactionRepository.findAll();
        allTx = DateFilterUtil.filter(allTx, request, SubAccountTransaction::getTransactionDate);
        List<Map<String, Object>> data = new ArrayList<>();

        double totalOutflow = 0;
        int transactionCount = 0;

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        for (SubAccountTransaction tx : allTx) {
            if ("DEBIT".equalsIgnoreCase(tx.getTransactionType())) {
                Map<String, Object> row = new HashMap<>();
                row.put("date", tx.getTransactionDate() != null ? tx.getTransactionDate().format(formatter) : "");
                
                String destination = "Unknown Destination";
                if (tx.getSubAccount() != null) {
                    destination = tx.getSubAccount().getBankName() != null ? tx.getSubAccount().getBankName() : tx.getSubAccount().getCashAccountName();
                }
                row.put("destination", destination);
                
                double amt = tx.getAmount() != null ? tx.getAmount() : 0.0;
                row.put("amount", String.format("%.2f", amt));
                row.put("method", tx.getPaymentMethod() != null ? tx.getPaymentMethod() : "-");
                row.put("reference", tx.getReferenceId() != null ? tx.getReferenceId() : "-");
                
                data.add(row);
                totalOutflow += amt;
                transactionCount++;
            }
        }

        response.setData(data);
        response.setTotalRecords(data.size());

        ReportSummaryDTO summary = new ReportSummaryDTO();
        summary.addMetric("Total Outflow Transactions", String.valueOf(transactionCount));
        summary.addMetric("Total Outflow Amount", String.format("₹%.2f", totalOutflow));
        response.setSummary(summary);

        return response;
    }
}
