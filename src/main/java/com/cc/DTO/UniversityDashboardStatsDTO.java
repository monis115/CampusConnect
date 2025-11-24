package com.cc.DTO;

public class UniversityDashboardStatsDTO {
    private Long totalStudents;
    private Long admittedStudents;
    private Long pendingApplications;
    private Long rejectedStudents;

    public UniversityDashboardStatsDTO(Long totalStudents, Long admittedStudents, Long pendingApplications,
            Long rejectedStudents) {
        this.totalStudents = totalStudents;
        this.admittedStudents = admittedStudents;
        this.pendingApplications = pendingApplications;
        this.rejectedStudents = rejectedStudents;
    }

    public Long getTotalStudents() {
        return totalStudents;
    }

    public void setTotalStudents(Long totalStudents) {
        this.totalStudents = totalStudents;
    }

    public Long getAdmittedStudents() {
        return admittedStudents;
    }

    public void setAdmittedStudents(Long admittedStudents) {
        this.admittedStudents = admittedStudents;
    }

    public Long getPendingApplications() {
        return pendingApplications;
    }

    public void setPendingApplications(Long pendingApplications) {
        this.pendingApplications = pendingApplications;
    }

    public Long getRejectedStudents() {
        return rejectedStudents;
    }

    public void setRejectedStudents(Long rejectedStudents) {
        this.rejectedStudents = rejectedStudents;
    }
}
