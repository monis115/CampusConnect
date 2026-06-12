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
public class ReportStudentCollectionGenerator implements ReportGenerator {

    private final CollectionRequestRepository collectionRequestRepository;

    public ReportStudentCollectionGenerator(CollectionRequestRepository collectionRequestRepository) {
        this.collectionRequestRepository = collectionRequestRepository;
    }

    @Override
    public String getCategory() {
        return "collection-reports";
    }

    @Override
    public String getReportName() {
        return "student-collection";
    }

    @Override
    public ReportResponseDTO generate(ReportRequest request) {
        ReportResponseDTO response = new ReportResponseDTO();

        List<ColumnDefDTO> columns = new ArrayList<>();
        columns.add(new ColumnDefDTO("studentName", "Student Name", "string"));
        columns.add(new ColumnDefDTO("applicationId", "Application ID", "string"));
        columns.add(new ColumnDefDTO("universityName", "University", "string"));
        columns.add(new ColumnDefDTO("expectedAmount", "Expected Fee (₹)", "currency"));
        columns.add(new ColumnDefDTO("collectedAmount", "Collected Fee (₹)", "currency"));
        columns.add(new ColumnDefDTO("pendingAmount", "Pending (₹)", "currency"));
        columns.add(new ColumnDefDTO("status", "Status", "status"));
        response.setColumns(columns);

        List<CollectionRequest> allRequests = collectionRequestRepository.findAll();
        allRequests = DateFilterUtil.filter(allRequests, request, CollectionRequest::getCreatedAt);
        
        List<Map<String, Object>> data = new ArrayList<>();
        double totalExpected = 0;
        double totalCollected = 0;
        double totalPending = 0;

        for (CollectionRequest req : allRequests) {
            Map<String, Object> row = new HashMap<>();
            row.put("studentName", req.getStudentName() != null ? req.getStudentName() : "Unknown");
            row.put("applicationId", req.getApplicationId() != null ? req.getApplicationId() : "-");
            row.put("universityName", req.getUniversityName() != null ? req.getUniversityName() : "-");
            
            double exp = req.getRequestedAmount() != null ? req.getRequestedAmount() : 0.0;
            double coll = req.getCollectedAmount() != null ? req.getCollectedAmount() : 0.0;
            double pend = req.getPendingAmount() != null ? req.getPendingAmount() : 0.0;

            row.put("expectedAmount", String.format("%.2f", exp));
            row.put("collectedAmount", String.format("%.2f", coll));
            row.put("pendingAmount", String.format("%.2f", pend));
            row.put("status", req.getStatus() != null ? req.getStatus() : "PENDING");

            data.add(row);
            
            totalExpected += exp;
            totalCollected += coll;
            totalPending += pend;
        }

        response.setData(data);
        response.setTotalRecords(data.size());

        ReportSummaryDTO summary = new ReportSummaryDTO();
        summary.addMetric("Total Expected", String.format("₹%.2f", totalExpected));
        summary.addMetric("Total Collected", String.format("₹%.2f", totalCollected));
        summary.addMetric("Total Pending", String.format("₹%.2f", totalPending));
        response.setSummary(summary);

        return response;
    }
}
