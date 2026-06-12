package com.cc.service.report.impl;

import com.cc.DTO.report.ColumnDefDTO;
import com.cc.DTO.report.ReportRequest;
import com.cc.service.report.impl.DateFilterUtil;
import com.cc.DTO.report.ReportResponseDTO;
import com.cc.DTO.report.ReportSummaryDTO;
import com.cc.modals.CollectionRequest;
import com.cc.repository.CollectionRequestRepository;
import com.cc.service.report.ReportGenerator;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ReportCollectionSummaryGenerator implements ReportGenerator {

    private final CollectionRequestRepository collectionRequestRepository;

    public ReportCollectionSummaryGenerator(CollectionRequestRepository collectionRequestRepository) {
        this.collectionRequestRepository = collectionRequestRepository;
    }

    @Override
    public String getCategory() {
        return "collection-reports";
    }

    @Override
    public String getReportName() {
        return "collection-summary";
    }

    @Override
    public ReportResponseDTO generate(ReportRequest request) {
        ReportResponseDTO response = new ReportResponseDTO();

        List<ColumnDefDTO> columns = new ArrayList<>();
        columns.add(new ColumnDefDTO("status", "Status", "status"));
        columns.add(new ColumnDefDTO("totalRequests", "Total Requests", "string"));
        columns.add(new ColumnDefDTO("expectedAmount", "Expected Amount (₹)", "currency"));
        columns.add(new ColumnDefDTO("collectedAmount", "Collected Amount (₹)", "currency"));
        columns.add(new ColumnDefDTO("shortfall", "Shortfall/Pending (₹)", "currency"));
        response.setColumns(columns);

        List<CollectionRequest> allRequests = collectionRequestRepository.findAll();
        allRequests = DateFilterUtil.filter(allRequests, request, CollectionRequest::getCreatedAt);
        
        // Group by Status
        Map<String, double[]> statusAggregations = new HashMap<>(); // [count, expected, collected, pending]
        
        double totalExpected = 0;
        double totalCollected = 0;
        double totalPending = 0;

        for (CollectionRequest req : allRequests) {
            String status = req.getStatus() != null ? req.getStatus().toUpperCase() : "UNKNOWN";
            double exp = req.getRequestedAmount() != null ? req.getRequestedAmount() : 0.0;
            double coll = req.getCollectedAmount() != null ? req.getCollectedAmount() : 0.0;
            double pend = req.getPendingAmount() != null ? req.getPendingAmount() : 0.0;

            statusAggregations.putIfAbsent(status, new double[]{0, 0, 0, 0});
            double[] agg = statusAggregations.get(status);
            agg[0]++;
            agg[1] += exp;
            agg[2] += coll;
            agg[3] += pend;

            totalExpected += exp;
            totalCollected += coll;
            totalPending += pend;
        }

        List<Map<String, Object>> data = new ArrayList<>();
        for (Map.Entry<String, double[]> entry : statusAggregations.entrySet()) {
            Map<String, Object> row = new HashMap<>();
            row.put("status", entry.getKey());
            row.put("totalRequests", String.valueOf((int) entry.getValue()[0]));
            row.put("expectedAmount", String.format("%.2f", entry.getValue()[1]));
            row.put("collectedAmount", String.format("%.2f", entry.getValue()[2]));
            row.put("shortfall", String.format("%.2f", entry.getValue()[3]));
            data.add(row);
        }

        response.setData(data);
        response.setTotalRecords(data.size());

        ReportSummaryDTO summary = new ReportSummaryDTO();
        summary.addMetric("Total Expected", String.format("₹%.2f", totalExpected));
        summary.addMetric("Total Collected", String.format("₹%.2f", totalCollected));
        summary.addMetric("Total Shortfall", String.format("₹%.2f", totalPending));
        response.setSummary(summary);

        return response;
    }
}
