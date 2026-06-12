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
public class ReportAgentCollectionGenerator implements ReportGenerator {

    private final CollectionRequestRepository collectionRequestRepository;

    public ReportAgentCollectionGenerator(CollectionRequestRepository collectionRequestRepository) {
        this.collectionRequestRepository = collectionRequestRepository;
    }

    @Override
    public String getCategory() {
        return "collection-reports";
    }

    @Override
    public String getReportName() {
        return "agent-collection";
    }

    @Override
    public ReportResponseDTO generate(ReportRequest request) {
        ReportResponseDTO response = new ReportResponseDTO();

        List<ColumnDefDTO> columns = new ArrayList<>();
        columns.add(new ColumnDefDTO("agentName", "Agent Name", "string"));
        columns.add(new ColumnDefDTO("totalStudents", "Total Students", "string"));
        columns.add(new ColumnDefDTO("totalTarget", "Total Target (₹)", "currency"));
        columns.add(new ColumnDefDTO("totalCollected", "Total Collected (₹)", "currency"));
        columns.add(new ColumnDefDTO("totalPending", "Total Pending (₹)", "currency"));
        response.setColumns(columns);

        List<CollectionRequest> allRequests = collectionRequestRepository.findAll();
        allRequests = DateFilterUtil.filter(allRequests, request, CollectionRequest::getCreatedAt);
        
        // Group by Agent Name
        Map<String, double[]> agentAggregations = new HashMap<>(); // [count, expected, collected, pending]
        
        double grandTarget = 0;
        double grandCollected = 0;
        double grandPending = 0;

        for (CollectionRequest req : allRequests) {
            String agent = req.getAgentName() != null && !req.getAgentName().isEmpty() ? req.getAgentName() : "Direct / Unknown";
            double exp = req.getRequestedAmount() != null ? req.getRequestedAmount() : 0.0;
            double coll = req.getCollectedAmount() != null ? req.getCollectedAmount() : 0.0;
            double pend = req.getPendingAmount() != null ? req.getPendingAmount() : 0.0;

            agentAggregations.putIfAbsent(agent, new double[]{0, 0, 0, 0});
            double[] agg = agentAggregations.get(agent);
            agg[0]++; // students / requests
            agg[1] += exp;
            agg[2] += coll;
            agg[3] += pend;

            grandTarget += exp;
            grandCollected += coll;
            grandPending += pend;
        }

        List<Map<String, Object>> data = new ArrayList<>();
        for (Map.Entry<String, double[]> entry : agentAggregations.entrySet()) {
            Map<String, Object> row = new HashMap<>();
            row.put("agentName", entry.getKey());
            row.put("totalStudents", String.valueOf((int) entry.getValue()[0]));
            row.put("totalTarget", String.format("%.2f", entry.getValue()[1]));
            row.put("totalCollected", String.format("%.2f", entry.getValue()[2]));
            row.put("totalPending", String.format("%.2f", entry.getValue()[3]));
            data.add(row);
        }

        response.setData(data);
        response.setTotalRecords(data.size());

        ReportSummaryDTO summary = new ReportSummaryDTO();
        summary.addMetric("Grand Target", String.format("₹%.2f", grandTarget));
        summary.addMetric("Grand Collected", String.format("₹%.2f", grandCollected));
        summary.addMetric("Grand Pending", String.format("₹%.2f", grandPending));
        response.setSummary(summary);

        return response;
    }
}
