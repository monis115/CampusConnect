package com.cc.DTO;

/**
 * DTO for Admission Manager Report.
 * The periodStats contains counts and trends for the requested period
 * (yearly/monthly/daily).
 */
public class AdmissionManagerReportDTO {
    private Long managerId;
    private String managerName;
    private String managerUsername;
    private String period; // "yearly", "monthly", or "daily"
    private ManagerReportStatsDTO periodStats; // Current period stats with trends

    public AdmissionManagerReportDTO() {
    }

    public AdmissionManagerReportDTO(Long managerId, String managerName, String managerUsername,
            String period, ManagerReportStatsDTO periodStats) {
        this.managerId = managerId;
        this.managerName = managerName;
        this.managerUsername = managerUsername;
        this.period = period;
        this.periodStats = periodStats;
    }

    public Long getManagerId() {
        return managerId;
    }

    public void setManagerId(Long managerId) {
        this.managerId = managerId;
    }

    public String getManagerName() {
        return managerName;
    }

    public void setManagerName(String managerName) {
        this.managerName = managerName;
    }

    public String getManagerUsername() {
        return managerUsername;
    }

    public void setManagerUsername(String managerUsername) {
        this.managerUsername = managerUsername;
    }

    public String getPeriod() {
        return period;
    }

    public void setPeriod(String period) {
        this.period = period;
    }

    public ManagerReportStatsDTO getPeriodStats() {
        return periodStats;
    }

    public void setPeriodStats(ManagerReportStatsDTO periodStats) {
        this.periodStats = periodStats;
    }
}
