package com.cc.DTO;

public class ManagerReportStatsDTO {
    private int forwardedToUniversity;
    private Double forwardedToUniversityTrend; // Percentage change

    private int admissionLettersApproved;
    private Double admissionLettersApprovedTrend;

    private int telexManagersAssigned;
    private Double telexManagersAssignedTrend;

    private int visaManagersAssigned;
    private Double visaManagersAssignedTrend;

    public ManagerReportStatsDTO() {
        this.forwardedToUniversity = 0;
        this.admissionLettersApproved = 0;
        this.telexManagersAssigned = 0;
        this.visaManagersAssigned = 0;
    }

    // Increment methods
    public void incrementForwardedToUniversity() {
        this.forwardedToUniversity++;
    }

    public void incrementAdmissionLettersApproved() {
        this.admissionLettersApproved++;
    }

    public void incrementTelexManagersAssigned() {
        this.telexManagersAssigned++;
    }

    public void incrementVisaManagersAssigned() {
        this.visaManagersAssigned++;
    }

    // Calculate trend percentage
    public static Double calculateTrend(int current, int previous) {
        if (previous == 0) {
            return current > 0 ? 100.0 : null; // null means no data to compare
        }
        return ((double) (current - previous) / previous) * 100.0;
    }

    // Getters and Setters
    public int getForwardedToUniversity() {
        return forwardedToUniversity;
    }

    public void setForwardedToUniversity(int forwardedToUniversity) {
        this.forwardedToUniversity = forwardedToUniversity;
    }

    public Double getForwardedToUniversityTrend() {
        return forwardedToUniversityTrend;
    }

    public void setForwardedToUniversityTrend(Double forwardedToUniversityTrend) {
        this.forwardedToUniversityTrend = forwardedToUniversityTrend;
    }

    public int getAdmissionLettersApproved() {
        return admissionLettersApproved;
    }

    public void setAdmissionLettersApproved(int admissionLettersApproved) {
        this.admissionLettersApproved = admissionLettersApproved;
    }

    public Double getAdmissionLettersApprovedTrend() {
        return admissionLettersApprovedTrend;
    }

    public void setAdmissionLettersApprovedTrend(Double admissionLettersApprovedTrend) {
        this.admissionLettersApprovedTrend = admissionLettersApprovedTrend;
    }

    public int getTelexManagersAssigned() {
        return telexManagersAssigned;
    }

    public void setTelexManagersAssigned(int telexManagersAssigned) {
        this.telexManagersAssigned = telexManagersAssigned;
    }

    public Double getTelexManagersAssignedTrend() {
        return telexManagersAssignedTrend;
    }

    public void setTelexManagersAssignedTrend(Double telexManagersAssignedTrend) {
        this.telexManagersAssignedTrend = telexManagersAssignedTrend;
    }

    public int getVisaManagersAssigned() {
        return visaManagersAssigned;
    }

    public void setVisaManagersAssigned(int visaManagersAssigned) {
        this.visaManagersAssigned = visaManagersAssigned;
    }

    public Double getVisaManagersAssignedTrend() {
        return visaManagersAssignedTrend;
    }

    public void setVisaManagersAssignedTrend(Double visaManagersAssignedTrend) {
        this.visaManagersAssignedTrend = visaManagersAssignedTrend;
    }
}
