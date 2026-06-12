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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ReportTransactionSummaryGenerator implements ReportGenerator {

    private final SubAccountTransactionRepository transactionRepository;

    public ReportTransactionSummaryGenerator(SubAccountTransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    @Override
    public String getCategory() {
        return "transaction-reports";
    }

    @Override
    public String getReportName() {
        return "transaction-summary";
    }

    @Override
    public ReportResponseDTO generate(ReportRequest request) {
        ReportResponseDTO response = new ReportResponseDTO();

        List<ColumnDefDTO> columns = new ArrayList<>();
        columns.add(new ColumnDefDTO("method", "Payment Method", "string"));
        columns.add(new ColumnDefDTO("inflowCount", "Inflow Count", "string"));
        columns.add(new ColumnDefDTO("inflowAmount", "Inflow Amount (₹)", "currency"));
        columns.add(new ColumnDefDTO("outflowCount", "Outflow Count", "string"));
        columns.add(new ColumnDefDTO("outflowAmount", "Outflow Amount (₹)", "currency"));
        response.setColumns(columns);

        List<SubAccountTransaction> allTx = transactionRepository.findAll();
        allTx = DateFilterUtil.filter(allTx, request, SubAccountTransaction::getTransactionDate);
        
        // Group by Payment Method
        Map<String, double[]> methodAggs = new HashMap<>(); // [inCount, inAmt, outCount, outAmt]

        double totalInflow = 0;
        double totalOutflow = 0;

        for (SubAccountTransaction tx : allTx) {
            String method = tx.getPaymentMethod() != null ? tx.getPaymentMethod().toUpperCase() : "UNKNOWN";
            double amt = tx.getAmount() != null ? tx.getAmount() : 0.0;
            
            methodAggs.putIfAbsent(method, new double[]{0, 0, 0, 0});
            double[] agg = methodAggs.get(method);

            if ("CREDIT".equalsIgnoreCase(tx.getTransactionType())) {
                agg[0]++;
                agg[1] += amt;
                totalInflow += amt;
            } else {
                agg[2]++;
                agg[3] += amt;
                totalOutflow += amt;
            }
        }

        List<Map<String, Object>> data = new ArrayList<>();
        for (Map.Entry<String, double[]> entry : methodAggs.entrySet()) {
            Map<String, Object> row = new HashMap<>();
            row.put("method", entry.getKey());
            row.put("inflowCount", String.valueOf((int) entry.getValue()[0]));
            row.put("inflowAmount", String.format("%.2f", entry.getValue()[1]));
            row.put("outflowCount", String.valueOf((int) entry.getValue()[2]));
            row.put("outflowAmount", String.format("%.2f", entry.getValue()[3]));
            data.add(row);
        }

        response.setData(data);
        response.setTotalRecords(data.size());

        ReportSummaryDTO summary = new ReportSummaryDTO();
        summary.addMetric("Total Inflow", String.format("₹%.2f", totalInflow));
        summary.addMetric("Total Outflow", String.format("₹%.2f", totalOutflow));
        summary.addMetric("Net Flow", String.format("₹%.2f", totalInflow - totalOutflow));
        response.setSummary(summary);

        return response;
    }
}
