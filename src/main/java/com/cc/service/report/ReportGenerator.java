package com.cc.service.report;

import com.cc.DTO.report.ReportRequest;
import com.cc.DTO.report.ReportResponseDTO;

public interface ReportGenerator {
    
    String getCategory();
    
    String getReportName();
    
    ReportResponseDTO generate(ReportRequest request);
}
