package com.cc.controller;

import com.cc.DTO.report.ReportRequest;
import com.cc.DTO.report.ReportResponseDTO;
import com.cc.service.report.ReportFactoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reports")
@CrossOrigin(originPatterns = "*", allowedHeaders = "*")
public class ReportController {

    private final ReportFactoryService reportFactoryService;

    @Autowired
    public ReportController(ReportFactoryService reportFactoryService) {
        this.reportFactoryService = reportFactoryService;
    }

    @PostMapping("/data")
    public ResponseEntity<ReportResponseDTO> getReportData(@RequestBody ReportRequest request) {
        try {
            ReportResponseDTO response = reportFactoryService.generate(request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(null);
        }
    }

    @PostMapping("/export")
    public ResponseEntity<byte[]> exportReport(
            @RequestBody ReportRequest request,
            @RequestParam("format") String format) {
        try {
            // Re-use the generation logic, but you might want to skip pagination (set size to max)
            request.setPage(0);
            request.setSize(Integer.MAX_VALUE); // Fetch all for export
            ReportResponseDTO data = reportFactoryService.generate(request);

            com.cc.service.report.ExportService exportService = new com.cc.service.report.ExportService();
            byte[] fileBytes;
            String fileName;

            if ("excel".equalsIgnoreCase(format)) {
                fileBytes = exportService.exportToExcel(data, request.getReportName());
                fileName = request.getReportName() + ".xlsx";
                return ResponseEntity.ok()
                        .header("Content-Disposition", "attachment; filename=\"" + fileName + "\"")
                        .header("Content-Type", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                        .body(fileBytes);
            } else if ("pdf".equalsIgnoreCase(format)) {
                fileBytes = exportService.exportToPdf(data, request.getReportName());
                fileName = request.getReportName() + ".pdf";
                return ResponseEntity.ok()
                        .header("Content-Disposition", "attachment; filename=\"" + fileName + "\"")
                        .header("Content-Type", "application/pdf")
                        .body(fileBytes);
            } else {
                return ResponseEntity.badRequest().body(null);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(null);
        }
    }
}
