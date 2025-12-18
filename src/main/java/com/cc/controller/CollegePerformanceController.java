package com.cc.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.cc.DTO.CollegeInfoDTO;
import com.cc.DTO.CollegePerformanceDashboardDTO;
import com.cc.service.CollegePerformanceService;

/**
 * REST Controller for College Performance Dashboard.
 * Provides API endpoints for college-specific analytics and metrics.
 */
@RestController
@RequestMapping("/api/college-performance")
@CrossOrigin(originPatterns = "*", allowCredentials = "true")
public class CollegePerformanceController {

    @Autowired
    private CollegePerformanceService collegePerformanceService;

    /**
     * Get complete dashboard data for a specific college.
     * 
     * @param collegeId The ID of the college
     * @param dateFrom  Optional start date for filtering (default: 30 days ago)
     * @param dateTo    Optional end date for filtering (default: today)
     * @return Complete dashboard DTO with all metrics
     */
    @GetMapping("/{collegeId}")
    public ResponseEntity<CollegePerformanceDashboardDTO> getDashboard(
            @PathVariable Long collegeId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo) {

        // Default to last 30 days if no dates provided
        if (dateFrom == null) {
            dateFrom = LocalDate.now().minusDays(30);
        }
        if (dateTo == null) {
            dateTo = LocalDate.now();
        }

        CollegePerformanceDashboardDTO dashboard = collegePerformanceService.getDashboard(
                collegeId, dateFrom, dateTo);

        return ResponseEntity.ok(dashboard);
    }

    /**
     * Get list of all colleges for dropdown selection.
     * 
     * @return List of college info DTOs
     */
    @GetMapping("/colleges")
    public ResponseEntity<List<CollegeInfoDTO>> getAllColleges() {
        List<CollegeInfoDTO> colleges = collegePerformanceService.getAllCollegesForDropdown();
        return ResponseEntity.ok(colleges);
    }

    /**
     * Get dashboard with preset date range.
     * 
     * @param collegeId The ID of the college
     * @param preset    Date range preset (7d, 30d, 90d, thisMonth, thisYear)
     * @return Complete dashboard DTO
     */
    @GetMapping("/{collegeId}/preset/{preset}")
    public ResponseEntity<CollegePerformanceDashboardDTO> getDashboardWithPreset(
            @PathVariable Long collegeId,
            @PathVariable String preset) {

        LocalDate dateFrom;
        LocalDate dateTo = LocalDate.now();

        switch (preset.toLowerCase()) {
            case "7d":
                dateFrom = LocalDate.now().minusDays(7);
                break;
            case "30d":
                dateFrom = LocalDate.now().minusDays(30);
                break;
            case "90d":
                dateFrom = LocalDate.now().minusDays(90);
                break;
            case "thismonth":
                dateFrom = LocalDate.now().withDayOfMonth(1);
                break;
            case "thisyear":
                dateFrom = LocalDate.now().withDayOfYear(1);
                break;
            case "today":
                dateFrom = LocalDate.now();
                break;
            default:
                dateFrom = LocalDate.now().minusDays(30);
        }

        CollegePerformanceDashboardDTO dashboard = collegePerformanceService.getDashboard(
                collegeId, dateFrom, dateTo);

        return ResponseEntity.ok(dashboard);
    }
}
