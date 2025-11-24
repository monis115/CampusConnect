package com.cc.DTO;

public class AgentDashboardStatsDTO {
    private Long totalStudents;
    private Long activeStudents;
    private Long pendingApplications;
    private Long visaApproved;
    private Long rejected;

    public AgentDashboardStatsDTO() {
    }

    public AgentDashboardStatsDTO(Long totalStudents, Long activeStudents, Long pendingApplications, Long visaApproved,
            Long rejected) {
        this.totalStudents = totalStudents;
        this.activeStudents = activeStudents;
        this.pendingApplications = pendingApplications;
        this.visaApproved = visaApproved;
        this.rejected = rejected;
    }

    public Long getTotalStudents() {
        return totalStudents;
    }

    public void setTotalStudents(Long totalStudents) {
        this.totalStudents = totalStudents;
    }

    public Long getActiveStudents() {
        return activeStudents;
    }

    public void setActiveStudents(Long activeStudents) {
        this.activeStudents = activeStudents;
    }

    public Long getPendingApplications() {
        return pendingApplications;
    }

    public void setPendingApplications(Long pendingApplications) {
        this.pendingApplications = pendingApplications;
    }

    public Long getVisaApproved() {
        return visaApproved;
    }

    public void setVisaApproved(Long visaApproved) {
        this.visaApproved = visaApproved;
    }

    public Long getRejected() {
        return rejected;
    }

    public void setRejected(Long rejected) {
        this.rejected = rejected;
    }
}
