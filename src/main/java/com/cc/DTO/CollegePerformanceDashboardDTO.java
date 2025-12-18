package com.cc.DTO;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Main response DTO for the College Performance Dashboard.
 * Aggregates all dashboard data for a specific college.
 */
public class CollegePerformanceDashboardDTO {

    private CollegeInfoDTO collegeInfo;
    private CollegeStatsOverviewDTO statsOverview;
    private StudentPipelineDTO studentPipeline;
    private EntityPerformanceDTO entityPerformance;
    private CollegeTimeSeriesDTO timeSeries;
    private List<CollegeRecentActivityDTO> recentActivities;
    private List<CollegeAlertDTO> alerts;
    private DashboardMetadata metadata;

    public CollegePerformanceDashboardDTO() {
    }

    // Getters and Setters
    public CollegeInfoDTO getCollegeInfo() {
        return collegeInfo;
    }

    public void setCollegeInfo(CollegeInfoDTO collegeInfo) {
        this.collegeInfo = collegeInfo;
    }

    public CollegeStatsOverviewDTO getStatsOverview() {
        return statsOverview;
    }

    public void setStatsOverview(CollegeStatsOverviewDTO statsOverview) {
        this.statsOverview = statsOverview;
    }

    public StudentPipelineDTO getStudentPipeline() {
        return studentPipeline;
    }

    public void setStudentPipeline(StudentPipelineDTO studentPipeline) {
        this.studentPipeline = studentPipeline;
    }

    public EntityPerformanceDTO getEntityPerformance() {
        return entityPerformance;
    }

    public void setEntityPerformance(EntityPerformanceDTO entityPerformance) {
        this.entityPerformance = entityPerformance;
    }

    public CollegeTimeSeriesDTO getTimeSeries() {
        return timeSeries;
    }

    public void setTimeSeries(CollegeTimeSeriesDTO timeSeries) {
        this.timeSeries = timeSeries;
    }

    public List<CollegeRecentActivityDTO> getRecentActivities() {
        return recentActivities;
    }

    public void setRecentActivities(List<CollegeRecentActivityDTO> recentActivities) {
        this.recentActivities = recentActivities;
    }

    public List<CollegeAlertDTO> getAlerts() {
        return alerts;
    }

    public void setAlerts(List<CollegeAlertDTO> alerts) {
        this.alerts = alerts;
    }

    public DashboardMetadata getMetadata() {
        return metadata;
    }

    public void setMetadata(DashboardMetadata metadata) {
        this.metadata = metadata;
    }

    /**
     * Inner class for dashboard metadata.
     */
    public static class DashboardMetadata {
        private LocalDateTime generatedAt;
        private LocalDate dateFrom;
        private LocalDate dateTo;
        private Map<String, String> filtersApplied;

        public DashboardMetadata() {
            this.generatedAt = LocalDateTime.now();
        }

        public DashboardMetadata(LocalDate dateFrom, LocalDate dateTo) {
            this.generatedAt = LocalDateTime.now();
            this.dateFrom = dateFrom;
            this.dateTo = dateTo;
        }

        public LocalDateTime getGeneratedAt() {
            return generatedAt;
        }

        public void setGeneratedAt(LocalDateTime generatedAt) {
            this.generatedAt = generatedAt;
        }

        public LocalDate getDateFrom() {
            return dateFrom;
        }

        public void setDateFrom(LocalDate dateFrom) {
            this.dateFrom = dateFrom;
        }

        public LocalDate getDateTo() {
            return dateTo;
        }

        public void setDateTo(LocalDate dateTo) {
            this.dateTo = dateTo;
        }

        public Map<String, String> getFiltersApplied() {
            return filtersApplied;
        }

        public void setFiltersApplied(Map<String, String> filtersApplied) {
            this.filtersApplied = filtersApplied;
        }
    }
}
