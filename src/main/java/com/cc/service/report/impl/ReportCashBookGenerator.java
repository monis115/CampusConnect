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
public class ReportCashBookGenerator implements ReportGenerator {

    private final SubAccountTransactionRepository transactionRepository;

    public ReportCashBookGenerator(SubAccountTransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    @Override
    public String getCategory() {
        return "financial-reports";
    }

    @Override
    public String getReportName() {
        return "cash-book";
    }

    @Override
    public ReportResponseDTO generate(ReportRequest request) {
        ReportResponseDTO response = new ReportResponseDTO();

        // Columns
        List<ColumnDefDTO> columns = new ArrayList<>();
        columns.add(new ColumnDefDTO("date", "Date", "string"));
        columns.add(new ColumnDefDTO("particulars", "Particulars", "string"));
        columns.add(new ColumnDefDTO("vchType", "Vch Type", "string"));
        columns.add(new ColumnDefDTO("inflow", "Inflow (₹)", "currency"));
        columns.add(new ColumnDefDTO("outflow", "Outflow (₹)", "currency"));
        columns.add(new ColumnDefDTO("balance", "Balance (₹)", "currency"));
        response.setColumns(columns);

        List<Map<String, Object>> data = new ArrayList<>();
        List<SubAccountTransaction> allTx = transactionRepository.findAll();
        allTx = DateFilterUtil.filter(allTx, request, SubAccountTransaction::getTransactionDate);

        double totalInflow = 0;
        double totalOutflow = 0;
        double runningBalance = 0;

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        for (SubAccountTransaction tx : allTx) {
            boolean isCash = false;
            if (tx.getSubAccount() != null) {
                String type = tx.getSubAccount().getAccountType();
                String name = tx.getSubAccount().getBankName();
                if ("CASH".equalsIgnoreCase(type)) {
                    isCash = true;
                } else if (type == null && "CASH".equalsIgnoreCase(name)) {
                    isCash = true;
                }
            }

            if (isCash) {
                Map<String, Object> row = new HashMap<>();
                row.put("date", tx.getTransactionDate() != null ? tx.getTransactionDate().format(formatter) : "");
                row.put("particulars", tx.getDescription() != null ? tx.getDescription() : "Cash Transaction");
                row.put("vchType", tx.getPaymentMethod());

                double amt = tx.getAmount() != null ? tx.getAmount() : 0.0;
                if ("CREDIT".equalsIgnoreCase(tx.getTransactionType())) {
                    row.put("inflow", String.format("%.2f", amt));
                    row.put("outflow", "0.00");
                    runningBalance += amt;
                    totalInflow += amt;
                } else {
                    row.put("inflow", "0.00");
                    row.put("outflow", String.format("%.2f", amt));
                    runningBalance -= amt;
                    totalOutflow += amt;
                }
                
                row.put("balance", String.format("%.2f", runningBalance));
                data.add(row);
            }
        }

        response.setData(data);
        response.setTotalRecords(data.size());

        ReportSummaryDTO summary = new ReportSummaryDTO();
        summary.addMetric("Total Cash Inflow", String.format("₹%.2f", totalInflow));
        summary.addMetric("Total Cash Outflow", String.format("₹%.2f", totalOutflow));
        summary.addMetric("Closing Balance", String.format("₹%.2f", runningBalance));
        response.setSummary(summary);

        return response;
    }
}
