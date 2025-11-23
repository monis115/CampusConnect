package com.cc.DTO;

import com.cc.modals.Student;
import java.util.List;
import java.util.Map;

public class AdmissionDashboardStatsDTO {
    private long totalStudents;
    private long newApplications; // REGISTERED, SUBMITTED
    private long pendingUniversityAction; // UNDER_REVIEW, FORWARDED_TO_UNI
    private long admissionLettersGenerated; // ADMISSION_LETTER_GENERATED
    private long rejected;

    private Map<String, Long> statusDistribution;
    private List<Student> recentStudents;

    // Getters and Setters
    public long getTotalStudents() {
        return totalStudents;
    }

    public void setTotalStudents(long totalStudents) {
        this.totalStudents = totalStudents;
    }

    public long getNewApplications() {
        return newApplications;
    }

    public void setNewApplications(long newApplications) {
        this.newApplications = newApplications;
    }

    public long getPendingUniversityAction() {
        return pendingUniversityAction;
    }

    public void setPendingUniversityAction(long pendingUniversityAction) {
        this.pendingUniversityAction = pendingUniversityAction;
    }

    public long getAdmissionLettersGenerated() {
        return admissionLettersGenerated;
    }

    public void setAdmissionLettersGenerated(long admissionLettersGenerated) {
        this.admissionLettersGenerated = admissionLettersGenerated;
    }

    public long getRejected() {
        return rejected;
    }

    public void setRejected(long rejected) {
        this.rejected = rejected;
    }

    public Map<String, Long> getStatusDistribution() {
        return statusDistribution;
    }

    public void setStatusDistribution(Map<String, Long> statusDistribution) {
        this.statusDistribution = statusDistribution;
    }

    public List<Student> getRecentStudents() {
        return recentStudents;
    }

    public void setRecentStudents(List<Student> recentStudents) {
        this.recentStudents = recentStudents;
    }
}
