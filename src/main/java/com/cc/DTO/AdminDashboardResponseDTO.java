package com.cc.DTO;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Main response DTO for the Admin Dashboard endpoint.
 * Consolidates all system-wide statistics and role-specific summaries.
 */
public class AdminDashboardResponseDTO {

    private SystemOverviewDTO systemOverview;
    private RoleSummariesDTO roleSummaries;
    private Map<String, Long> studentPipeline;
    private TimeSeriesDTO timeSeries;
    private List<AlertDTO> alerts;
    private List<RecentActivityDTO> recentActivities;
    private DashboardMetadataDTO metadata;

    public AdminDashboardResponseDTO() {
    }

    // Getters and Setters
    public SystemOverviewDTO getSystemOverview() {
        return systemOverview;
    }

    public void setSystemOverview(SystemOverviewDTO systemOverview) {
        this.systemOverview = systemOverview;
    }

    public RoleSummariesDTO getRoleSummaries() {
        return roleSummaries;
    }

    public void setRoleSummaries(RoleSummariesDTO roleSummaries) {
        this.roleSummaries = roleSummaries;
    }

    public Map<String, Long> getStudentPipeline() {
        return studentPipeline;
    }

    public void setStudentPipeline(Map<String, Long> studentPipeline) {
        this.studentPipeline = studentPipeline;
    }

    public TimeSeriesDTO getTimeSeries() {
        return timeSeries;
    }

    public void setTimeSeries(TimeSeriesDTO timeSeries) {
        this.timeSeries = timeSeries;
    }

    public List<AlertDTO> getAlerts() {
        return alerts;
    }

    public void setAlerts(List<AlertDTO> alerts) {
        this.alerts = alerts;
    }

    public List<RecentActivityDTO> getRecentActivities() {
        return recentActivities;
    }

    public void setRecentActivities(List<RecentActivityDTO> recentActivities) {
        this.recentActivities = recentActivities;
    }

    public DashboardMetadataDTO getMetadata() {
        return metadata;
    }

    public void setMetadata(DashboardMetadataDTO metadata) {
        this.metadata = metadata;
    }

    // Inner class for metadata
    public static class DashboardMetadataDTO {
        private LocalDateTime generatedAt;
        private Map<String, String> filtersApplied;

        public DashboardMetadataDTO() {
        }

        public DashboardMetadataDTO(LocalDateTime generatedAt, Map<String, String> filtersApplied) {
            this.generatedAt = generatedAt;
            this.filtersApplied = filtersApplied;
        }

        public LocalDateTime getGeneratedAt() {
            return generatedAt;
        }

        public void setGeneratedAt(LocalDateTime generatedAt) {
            this.generatedAt = generatedAt;
        }

        public Map<String, String> getFiltersApplied() {
            return filtersApplied;
        }

        public void setFiltersApplied(Map<String, String> filtersApplied) {
            this.filtersApplied = filtersApplied;
        }
    }
}
