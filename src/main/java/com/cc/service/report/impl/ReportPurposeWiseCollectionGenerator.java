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
public class ReportPurposeWiseCollectionGenerator implements ReportGenerator {

    private final CollectionRequestRepository collectionRequestRepository;

    public ReportPurposeWiseCollectionGenerator(CollectionRequestRepository collectionRequestRepository) {
        this.collectionRequestRepository = collectionRequestRepository;
    }

    @Override
    public String getCategory() {
        return "collection-reports";
    }

    @Override
    public String getReportName() {
        return "purpose-wise-collection";
    }

    @Override
    public ReportResponseDTO generate(ReportRequest request) {
        ReportResponseDTO response = new ReportResponseDTO();

        List<ColumnDefDTO> columns = new ArrayList<>();
        columns.add(new ColumnDefDTO("purpose", "Purpose / Remarks", "string"));
        columns.add(new ColumnDefDTO("transactionCount", "Transaction Count", "string"));
        columns.add(new ColumnDefDTO("totalAmount", "Total Collected (₹)", "currency"));
        response.setColumns(columns);

        List<CollectionRequest> allRequests = collectionRequestRepository.findAll();
        allRequests = DateFilterUtil.filter(allRequests, request, CollectionRequest::getCreatedAt);
        
        // Group by Purpose (Remarks)
        Map<String, double[]> purposeAggregations = new HashMap<>(); // [count, collected]
        
        double grandCollected = 0;

        for (CollectionRequest req : allRequests) {
            String purpose = req.getRemarks() != null && !req.getRemarks().trim().isEmpty() ? req.getRemarks() : "General Collection";
            double coll = req.getCollectedAmount() != null ? req.getCollectedAmount() : 0.0;

            if (coll > 0) {
                purposeAggregations.putIfAbsent(purpose, new double[]{0, 0});
                double[] agg = purposeAggregations.get(purpose);
                agg[0]++; // transactions
                agg[1] += coll;

                grandCollected += coll;
            }
        }

        List<Map<String, Object>> data = new ArrayList<>();
        for (Map.Entry<String, double[]> entry : purposeAggregations.entrySet()) {
            Map<String, Object> row = new HashMap<>();
            row.put("purpose", entry.getKey());
            row.put("transactionCount", String.valueOf((int) entry.getValue()[0]));
            row.put("totalAmount", String.format("%.2f", entry.getValue()[1]));
            data.add(row);
        }

        response.setData(data);
        response.setTotalRecords(data.size());

        ReportSummaryDTO summary = new ReportSummaryDTO();
        summary.addMetric("Total Collection Categories", String.valueOf(purposeAggregations.size()));
        summary.addMetric("Total Amount Collected", String.format("₹%.2f", grandCollected));
        response.setSummary(summary);

        return response;
    }
}
