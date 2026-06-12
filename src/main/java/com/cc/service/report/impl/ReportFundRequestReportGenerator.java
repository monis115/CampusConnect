package com.cc.service.report.impl;

import com.cc.DTO.report.ColumnDefDTO;
import com.cc.DTO.report.ReportRequest;
import com.cc.service.report.impl.DateFilterUtil;
import com.cc.DTO.report.ReportResponseDTO;
import com.cc.DTO.report.ReportSummaryDTO;
import com.cc.modals.RequestMoney;
import com.cc.repository.RequestMoneyRepository;
import com.cc.service.report.ReportGenerator;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ReportFundRequestReportGenerator implements ReportGenerator {

    private final RequestMoneyRepository requestMoneyRepository;

    public ReportFundRequestReportGenerator(RequestMoneyRepository requestMoneyRepository) {
        this.requestMoneyRepository = requestMoneyRepository;
    }

    @Override
    public String getCategory() {
        return "account-reports";
    }

    @Override
    public String getReportName() {
        return "fund-request-report";
    }

    @Override
    public ReportResponseDTO generate(ReportRequest request) {
        ReportResponseDTO response = new ReportResponseDTO();

        List<ColumnDefDTO> columns = new ArrayList<>();
        columns.add(new ColumnDefDTO("date", "Date", "string"));
        columns.add(new ColumnDefDTO("requestedBy", "Requested By", "string"));
        columns.add(new ColumnDefDTO("purpose", "Purpose", "string"));
        columns.add(new ColumnDefDTO("amount", "Amount (₹)", "currency"));
        columns.add(new ColumnDefDTO("status", "Status", "status"));
        response.setColumns(columns);

        List<RequestMoney> requests = requestMoneyRepository.findAll();
        requests = DateFilterUtil.filter(requests, request, RequestMoney::getCreatedAt);
        List<Map<String, Object>> data = new ArrayList<>();

        double totalAmount = 0;
        int approvedCount = 0;
        int pendingCount = 0;

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        for (RequestMoney req : requests) {
            Map<String, Object> row = new HashMap<>();
            row.put("date", req.getCreatedAt() != null ? req.getCreatedAt().format(formatter) : "");
            row.put("requestedBy", req.getRequesterFullName() + " (" + req.getRequesterRole() + ")");
            row.put("purpose", req.getPurpose() != null ? req.getPurpose() : "General Request");
            
            double amt = req.getRequestedAmount() != null ? req.getRequestedAmount() : 0.0;
            row.put("amount", String.format("%.2f", amt));
            
            String status = req.getStatus() != null ? req.getStatus() : "PENDING";
            row.put("status", status);

            if ("APPROVED".equalsIgnoreCase(status) || "CONFIRMED".equalsIgnoreCase(status) || "VERIFIED".equalsIgnoreCase(status) || "PAID".equalsIgnoreCase(status)) {
                approvedCount++;
            } else if ("PENDING".equalsIgnoreCase(status)) {
                pendingCount++;
            }

            totalAmount += amt;
            data.add(row);
        }

        response.setData(data);
        response.setTotalRecords(data.size());

        ReportSummaryDTO summary = new ReportSummaryDTO();
        summary.addMetric("Total Requested", String.format("₹%.2f", totalAmount));
        summary.addMetric("Approved Requests", String.valueOf(approvedCount));
        summary.addMetric("Pending Requests", String.valueOf(pendingCount));
        response.setSummary(summary);

        return response;
    }
}
