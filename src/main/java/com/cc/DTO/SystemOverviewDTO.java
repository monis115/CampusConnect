package com.cc.DTO;

/**
 * DTO for system-wide overview statistics.
 */
public class SystemOverviewDTO {

    private Long totalStudents;
    private Long totalAgents;
    private Long totalAdmissionManagers;
    private Long totalUniversityCoordinators;
    private Long totalDocPersons;
    private Long totalAccountants;
    private Long totalTelexPersons;
    private Long totalVisaPersons;
    private Long activeStudentsToday;
    private Long newRegistrationsThisWeek;
    private Long visaStampedThisMonth;

    public SystemOverviewDTO() {
    }

    // Getters and Setters
    public Long getTotalStudents() {
        return totalStudents;
    }

    public void setTotalStudents(Long totalStudents) {
        this.totalStudents = totalStudents;
    }

    public Long getTotalAgents() {
        return totalAgents;
    }

    public void setTotalAgents(Long totalAgents) {
        this.totalAgents = totalAgents;
    }

    public Long getTotalAdmissionManagers() {
        return totalAdmissionManagers;
    }

    public void setTotalAdmissionManagers(Long totalAdmissionManagers) {
        this.totalAdmissionManagers = totalAdmissionManagers;
    }

    public Long getTotalUniversityCoordinators() {
        return totalUniversityCoordinators;
    }

    public void setTotalUniversityCoordinators(Long totalUniversityCoordinators) {
        this.totalUniversityCoordinators = totalUniversityCoordinators;
    }

    public Long getTotalDocPersons() {
        return totalDocPersons;
    }

    public void setTotalDocPersons(Long totalDocPersons) {
        this.totalDocPersons = totalDocPersons;
    }

    public Long getTotalAccountants() {
        return totalAccountants;
    }

    public void setTotalAccountants(Long totalAccountants) {
        this.totalAccountants = totalAccountants;
    }

    public Long getTotalTelexPersons() {
        return totalTelexPersons;
    }

    public void setTotalTelexPersons(Long totalTelexPersons) {
        this.totalTelexPersons = totalTelexPersons;
    }

    public Long getTotalVisaPersons() {
        return totalVisaPersons;
    }

    public void setTotalVisaPersons(Long totalVisaPersons) {
        this.totalVisaPersons = totalVisaPersons;
    }

    public Long getActiveStudentsToday() {
        return activeStudentsToday;
    }

    public void setActiveStudentsToday(Long activeStudentsToday) {
        this.activeStudentsToday = activeStudentsToday;
    }

    public Long getNewRegistrationsThisWeek() {
        return newRegistrationsThisWeek;
    }

    public void setNewRegistrationsThisWeek(Long newRegistrationsThisWeek) {
        this.newRegistrationsThisWeek = newRegistrationsThisWeek;
    }

    public Long getVisaStampedThisMonth() {
        return visaStampedThisMonth;
    }

    public void setVisaStampedThisMonth(Long visaStampedThisMonth) {
        this.visaStampedThisMonth = visaStampedThisMonth;
    }
}
