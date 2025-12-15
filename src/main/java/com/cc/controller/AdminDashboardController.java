package com.cc.controller;

import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.cc.DTO.AdminDashboardResponseDTO;
import com.cc.service.AdminDashboardService;

/**
 * Controller for the consolidated admin dashboard endpoint.
 * Provides a single endpoint that returns a complete system overview.
 */
@RestController
@RequestMapping("/api/admin/dashboard")
public class AdminDashboardController {

    @Autowired
    private AdminDashboardService adminDashboardService;

    /**
     * Get consolidated admin dashboard data.
     * 
     * @param dateFrom    Start date for time-series data (default: 30 days ago)
     * @param dateTo      End date for time-series data (default: today)
     * @param granularity Time-series granularity: daily, weekly, monthly (default:
     *                    daily)
     * @param page        Page number for recent activities pagination (default: 0)
     * @param size        Number of recent activities to return (default: 10)
     * @return AdminDashboardResponseDTO containing all dashboard sections
     */
    @GetMapping
    public ResponseEntity<?> getDashboardData(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            @RequestParam(defaultValue = "daily") String granularity,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        try {
            // Set default date range if not provided
            if (dateTo == null) {
                dateTo = LocalDate.now();
            }
            if (dateFrom == null) {
                dateFrom = dateTo.minusDays(30);
            }

            // Validate granularity
            if (!granularity.equalsIgnoreCase("daily")
                    && !granularity.equalsIgnoreCase("weekly")
                    && !granularity.equalsIgnoreCase("monthly")) {
                return ResponseEntity.badRequest()
                        .body("Invalid granularity. Must be one of: daily, weekly, monthly");
            }

            // Validate page and size
            if (page < 0)
                page = 0;
            if (size < 1)
                size = 10;
            if (size > 100)
                size = 100;

            AdminDashboardResponseDTO response = adminDashboardService.getDashboardData(
                    dateFrom, dateTo, granularity, page, size);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500)
                    .body("Error fetching admin dashboard data: " + e.getMessage());
        }
    }

    /**
     * Get only system overview section.
     */
    @GetMapping("/overview")
    public ResponseEntity<?> getSystemOverview(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo) {
        try {
            if (dateTo == null)
                dateTo = LocalDate.now();
            if (dateFrom == null)
                dateFrom = dateTo.minusDays(30);

            return ResponseEntity.ok(adminDashboardService.getSystemOverview(dateFrom, dateTo));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500)
                    .body("Error fetching system overview: " + e.getMessage());
        }
    }

    /**
     * Get only role summaries section.
     */
    @GetMapping("/roles")
    public ResponseEntity<?> getRoleSummaries(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo) {
        try {
            if (dateTo == null)
                dateTo = LocalDate.now();
            if (dateFrom == null)
                dateFrom = dateTo.minusDays(30);

            return ResponseEntity.ok(adminDashboardService.getRoleSummaries(dateFrom, dateTo));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500)
                    .body("Error fetching role summaries: " + e.getMessage());
        }
    }

    /**
     * Get only student pipeline section.
     */
    @GetMapping("/pipeline")
    public ResponseEntity<?> getStudentPipeline(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo) {
        try {
            if (dateTo == null)
                dateTo = LocalDate.now();
            if (dateFrom == null)
                dateFrom = dateTo.minusDays(30);

            return ResponseEntity.ok(adminDashboardService.getStudentPipeline(dateFrom, dateTo));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500)
                    .body("Error fetching student pipeline: " + e.getMessage());
        }
    }

    /**
     * Get only alerts section.
     */
    @GetMapping("/alerts")
    public ResponseEntity<?> getAlerts(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom) {
        try {
            if (dateFrom == null)
                dateFrom = LocalDate.now().minusDays(30);

            return ResponseEntity.ok(adminDashboardService.getAlerts(dateFrom));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500)
                    .body("Error fetching alerts: " + e.getMessage());
        }
    }
}
