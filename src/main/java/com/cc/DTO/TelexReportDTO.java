package com.cc.DTO;

public class TelexReportDTO {
    private long totalStudents;

    // Assigned counts by period (students assigned to TelexMan)
    private long assignedToday;
    private long assignedMonth;
    private long assignedYear;

    // Assigned trends
    private Double assignedTodayTrend;
    private Double assignedMonthTrend;
    private Double assignedYearTrend;

    // Trend Stats (Percentage Growth)
    private Double totalStudentsTrend; // likely 0 or N/A

    private Double submittedDocumentsTodayTrend;
    private Double submittedToUniversityTodayTrend;
    private Double submittedToMEATodayTrend;
    private Double telexIssuedTodayTrend;

    private Double submittedDocumentsMonthTrend;
    private Double submittedToUniversityMonthTrend;
    private Double submittedToMEAMonthTrend;
    private Double telexIssuedMonthTrend;

    private Double submittedDocumentsYearTrend;
    private Double submittedToUniversityYearTrend;
    private Double submittedToMEAYearTrend;
    private Double telexIssuedYearTrend;

    // Activity Panel
    private java.util.List<DailyActivityDTO> activityLogs;

    // Getters and Setters for new fields
    public Double getSubmittedDocumentsTodayTrend() {
        return submittedDocumentsTodayTrend;
    }

    public void setSubmittedDocumentsTodayTrend(Double submittedDocumentsTodayTrend) {
        this.submittedDocumentsTodayTrend = submittedDocumentsTodayTrend;
    }

    public Double getSubmittedToUniversityTodayTrend() {
        return submittedToUniversityTodayTrend;
    }

    public void setSubmittedToUniversityTodayTrend(Double submittedToUniversityTodayTrend) {
        this.submittedToUniversityTodayTrend = submittedToUniversityTodayTrend;
    }

    public Double getSubmittedToMEATodayTrend() {
        return submittedToMEATodayTrend;
    }

    public void setSubmittedToMEATodayTrend(Double submittedToMEATodayTrend) {
        this.submittedToMEATodayTrend = submittedToMEATodayTrend;
    }

    public Double getTelexIssuedTodayTrend() {
        return telexIssuedTodayTrend;
    }

    public void setTelexIssuedTodayTrend(Double telexIssuedTodayTrend) {
        this.telexIssuedTodayTrend = telexIssuedTodayTrend;
    }

    public Double getSubmittedDocumentsMonthTrend() {
        return submittedDocumentsMonthTrend;
    }

    public void setSubmittedDocumentsMonthTrend(Double submittedDocumentsMonthTrend) {
        this.submittedDocumentsMonthTrend = submittedDocumentsMonthTrend;
    }

    public Double getSubmittedToUniversityMonthTrend() {
        return submittedToUniversityMonthTrend;
    }

    public void setSubmittedToUniversityMonthTrend(Double submittedToUniversityMonthTrend) {
        this.submittedToUniversityMonthTrend = submittedToUniversityMonthTrend;
    }

    public Double getSubmittedToMEAMonthTrend() {
        return submittedToMEAMonthTrend;
    }

    public void setSubmittedToMEAMonthTrend(Double submittedToMEAMonthTrend) {
        this.submittedToMEAMonthTrend = submittedToMEAMonthTrend;
    }

    public Double getTelexIssuedMonthTrend() {
        return telexIssuedMonthTrend;
    }

    public void setTelexIssuedMonthTrend(Double telexIssuedMonthTrend) {
        this.telexIssuedMonthTrend = telexIssuedMonthTrend;
    }

    public Double getSubmittedDocumentsYearTrend() {
        return submittedDocumentsYearTrend;
    }

    public void setSubmittedDocumentsYearTrend(Double submittedDocumentsYearTrend) {
        this.submittedDocumentsYearTrend = submittedDocumentsYearTrend;
    }

    public Double getSubmittedToUniversityYearTrend() {
        return submittedToUniversityYearTrend;
    }

    public void setSubmittedToUniversityYearTrend(Double submittedToUniversityYearTrend) {
        this.submittedToUniversityYearTrend = submittedToUniversityYearTrend;
    }

    public Double getSubmittedToMEAYearTrend() {
        return submittedToMEAYearTrend;
    }

    public void setSubmittedToMEAYearTrend(Double submittedToMEAYearTrend) {
        this.submittedToMEAYearTrend = submittedToMEAYearTrend;
    }

    public Double getTelexIssuedYearTrend() {
        return telexIssuedYearTrend;
    }

    public void setTelexIssuedYearTrend(Double telexIssuedYearTrend) {
        this.telexIssuedYearTrend = telexIssuedYearTrend;
    }

    public java.util.List<DailyActivityDTO> getActivityLogs() {
        return activityLogs;
    }

    public void setActivityLogs(java.util.List<DailyActivityDTO> activityLogs) {
        this.activityLogs = activityLogs;
    }

    // Daily Stats
    private long submittedDocumentsToday;
    private long submittedToUniversityToday;
    private long submittedToMEAToday;
    private long telexIssuedToday;

    // Monthly Stats
    private long submittedDocumentsMonth;
    private long submittedToUniversityMonth;
    private long submittedToMEAMonth;
    private long telexIssuedMonth;

    // Yearly Stats
    private long submittedDocumentsYear;
    private long submittedToUniversityYear;
    private long submittedToMEAYear;
    private long telexIssuedYear;

    // Getters and Setters
    public long getTotalStudents() {
        return totalStudents;
    }

    public void setTotalStudents(long totalStudents) {
        this.totalStudents = totalStudents;
    }

    public long getSubmittedDocumentsToday() {
        return submittedDocumentsToday;
    }

    public void setSubmittedDocumentsToday(long submittedDocumentsToday) {
        this.submittedDocumentsToday = submittedDocumentsToday;
    }

    public long getSubmittedToUniversityToday() {
        return submittedToUniversityToday;
    }

    public void setSubmittedToUniversityToday(long submittedToUniversityToday) {
        this.submittedToUniversityToday = submittedToUniversityToday;
    }

    public long getSubmittedToMEAToday() {
        return submittedToMEAToday;
    }

    public void setSubmittedToMEAToday(long submittedToMEAToday) {
        this.submittedToMEAToday = submittedToMEAToday;
    }

    public long getTelexIssuedToday() {
        return telexIssuedToday;
    }

    public void setTelexIssuedToday(long telexIssuedToday) {
        this.telexIssuedToday = telexIssuedToday;
    }

    public long getSubmittedDocumentsMonth() {
        return submittedDocumentsMonth;
    }

    public void setSubmittedDocumentsMonth(long submittedDocumentsMonth) {
        this.submittedDocumentsMonth = submittedDocumentsMonth;
    }

    public long getSubmittedToUniversityMonth() {
        return submittedToUniversityMonth;
    }

    public void setSubmittedToUniversityMonth(long submittedToUniversityMonth) {
        this.submittedToUniversityMonth = submittedToUniversityMonth;
    }

    public long getSubmittedToMEAMonth() {
        return submittedToMEAMonth;
    }

    public void setSubmittedToMEAMonth(long submittedToMEAMonth) {
        this.submittedToMEAMonth = submittedToMEAMonth;
    }

    public long getTelexIssuedMonth() {
        return telexIssuedMonth;
    }

    public void setTelexIssuedMonth(long telexIssuedMonth) {
        this.telexIssuedMonth = telexIssuedMonth;
    }

    public long getSubmittedDocumentsYear() {
        return submittedDocumentsYear;
    }

    public void setSubmittedDocumentsYear(long submittedDocumentsYear) {
        this.submittedDocumentsYear = submittedDocumentsYear;
    }

    public long getSubmittedToUniversityYear() {
        return submittedToUniversityYear;
    }

    public void setSubmittedToUniversityYear(long submittedToUniversityYear) {
        this.submittedToUniversityYear = submittedToUniversityYear;
    }

    public long getSubmittedToMEAYear() {
        return submittedToMEAYear;
    }

    public void setSubmittedToMEAYear(long submittedToMEAYear) {
        this.submittedToMEAYear = submittedToMEAYear;
    }

    public long getTelexIssuedYear() {
        return telexIssuedYear;
    }

    public void setTelexIssuedYear(long telexIssuedYear) {
        this.telexIssuedYear = telexIssuedYear;
    }

    // Assigned counts getters/setters
    public long getAssignedToday() {
        return assignedToday;
    }

    public void setAssignedToday(long assignedToday) {
        this.assignedToday = assignedToday;
    }

    public long getAssignedMonth() {
        return assignedMonth;
    }

    public void setAssignedMonth(long assignedMonth) {
        this.assignedMonth = assignedMonth;
    }

    public long getAssignedYear() {
        return assignedYear;
    }

    public void setAssignedYear(long assignedYear) {
        this.assignedYear = assignedYear;
    }

    public Double getAssignedTodayTrend() {
        return assignedTodayTrend;
    }

    public void setAssignedTodayTrend(Double assignedTodayTrend) {
        this.assignedTodayTrend = assignedTodayTrend;
    }

    public Double getAssignedMonthTrend() {
        return assignedMonthTrend;
    }

    public void setAssignedMonthTrend(Double assignedMonthTrend) {
        this.assignedMonthTrend = assignedMonthTrend;
    }

    public Double getAssignedYearTrend() {
        return assignedYearTrend;
    }

    public void setAssignedYearTrend(Double assignedYearTrend) {
        this.assignedYearTrend = assignedYearTrend;
    }
}
