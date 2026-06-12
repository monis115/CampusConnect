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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ReportUniversityCollectionGenerator implements ReportGenerator {

    private final CollectionRequestRepository collectionRequestRepository;

    public ReportUniversityCollectionGenerator(CollectionRequestRepository collectionRequestRepository) {
        this.collectionRequestRepository = collectionRequestRepository;
    }

    @Override
    public String getCategory() {
        return "collection-reports";
    }

    @Override
    public String getReportName() {
        return "university-collection";
    }

    @Override
    public ReportResponseDTO generate(ReportRequest request) {
        ReportResponseDTO response = new ReportResponseDTO();

        List<ColumnDefDTO> columns = new ArrayList<>();
        columns.add(new ColumnDefDTO("universityName", "University Name", "string"));
        columns.add(new ColumnDefDTO("totalStudents", "Total Students", "string"));
        columns.add(new ColumnDefDTO("totalTarget", "Total Target (₹)", "currency"));
        columns.add(new ColumnDefDTO("totalCollected", "Total Collected (₹)", "currency"));
        columns.add(new ColumnDefDTO("totalPending", "Total Pending (₹)", "currency"));
        response.setColumns(columns);

        LocalDateTime startDate = null;
        LocalDateTime endDate = null;
        if (request.getStartDate() != null && !request.getStartDate().isEmpty()) {
            startDate = LocalDate.parse(request.getStartDate()).atStartOfDay();
        }
        if (request.getEndDate() != null && !request.getEndDate().isEmpty()) {
            endDate = LocalDate.parse(request.getEndDate()).atTime(23, 59, 59);
        }

        List<CollectionRequest> allRequests = collectionRequestRepository.findAll();
        allRequests = DateFilterUtil.filter(allRequests, request, CollectionRequest::getCreatedAt);
        
        // Group by University Name
        Map<String, double[]> universityAggregations = new HashMap<>(); // [count, expected, collected, pending]
        
        double grandTarget = 0;
        double grandCollected = 0;
        double grandPending = 0;

        for (CollectionRequest req : allRequests) {
            LocalDateTime created = req.getCreatedAt();
            if (isWithinRange(created, startDate, endDate)) {
                String university = req.getUniversityName() != null && !req.getUniversityName().isEmpty() ? req.getUniversityName() : "Unknown University";
                double exp = req.getRequestedAmount() != null ? req.getRequestedAmount() : 0.0;
                double coll = req.getCollectedAmount() != null ? req.getCollectedAmount() : 0.0;
                double pend = req.getPendingAmount() != null ? req.getPendingAmount() : 0.0;

                universityAggregations.putIfAbsent(university, new double[]{0, 0, 0, 0});
                double[] agg = universityAggregations.get(university);
                agg[0]++; // students / requests
                agg[1] += exp;
                agg[2] += coll;
                agg[3] += pend;

                grandTarget += exp;
                grandCollected += coll;
                grandPending += pend;
            }
        }

        List<Map<String, Object>> data = new ArrayList<>();
        for (Map.Entry<String, double[]> entry : universityAggregations.entrySet()) {
            Map<String, Object> row = new HashMap<>();
            row.put("universityName", entry.getKey());
            row.put("totalStudents", String.valueOf((int) entry.getValue()[0]));
            row.put("totalTarget", String.format("%.2f", entry.getValue()[1]));
            row.put("totalCollected", String.format("%.2f", entry.getValue()[2]));
            row.put("totalPending", String.format("%.2f", entry.getValue()[3]));
            data.add(row);
        }

        response.setData(data);
        response.setTotalRecords(data.size());

        ReportSummaryDTO summary = new ReportSummaryDTO();
        summary.addMetric("Total Universities", String.valueOf(universityAggregations.size()));
        summary.addMetric("Grand Target", String.format("₹%.2f", grandTarget));
        summary.addMetric("Grand Collected", String.format("₹%.2f", grandCollected));
        summary.addMetric("Grand Pending", String.format("₹%.2f", grandPending));
        response.setSummary(summary);

        return response;
    }

    private boolean isWithinRange(LocalDateTime date, LocalDateTime startDate, LocalDateTime endDate) {
        if (date == null) return true;
        if (startDate != null && date.isBefore(startDate)) return false;
        if (endDate != null && date.isAfter(endDate)) return false;
        return true;
    }
}
