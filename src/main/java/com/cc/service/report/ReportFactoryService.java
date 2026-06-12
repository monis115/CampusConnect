package com.cc.service.report;

import com.cc.DTO.report.ReportRequest;
import com.cc.DTO.report.ReportResponseDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ReportFactoryService {

    private final Map<String, ReportGenerator> reportGenerators = new HashMap<>();

    @Autowired
    public ReportFactoryService(List<ReportGenerator> generators) {
        for (ReportGenerator generator : generators) {
            String key = generator.getCategory() + "-" + generator.getReportName();
            reportGenerators.put(key, generator);
        }
    }

    public ReportResponseDTO generate(ReportRequest request) {
        String key = request.getCategory() + "-" + request.getReportName();
        ReportGenerator generator = reportGenerators.get(key);
        if (generator == null) {
            throw new IllegalArgumentException("Report not found for category: " + request.getCategory() + " and name: " + request.getReportName());
        }
        return generator.generate(request);
    }
    
    public ReportGenerator getGenerator(String category, String reportName) {
        String key = category + "-" + reportName;
        return reportGenerators.get(key);
    }
}
