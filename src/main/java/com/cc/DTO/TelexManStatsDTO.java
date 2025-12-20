package com.cc.DTO;

/**
 * DTO for TelexMan with performance stats
 * Used for displaying all TelexMan in a summary table with their individual
 * stat counts
 */
public class TelexManStatsDTO {
    private Long id;
    private String username;
    private String fullName;
    private String status;

    // Stat counts
    private long totalStudents;
    private long docsUploaded;
    private long submittedToUniversity;
    private long submittedToMEA;
    private long telexIssued;

    public TelexManStatsDTO() {
    }

    public TelexManStatsDTO(Long id, String username, String fullName, String status,
            long totalStudents, long docsUploaded, long submittedToUniversity,
            long submittedToMEA, long telexIssued) {
        this.id = id;
        this.username = username;
        this.fullName = fullName;
        this.status = status;
        this.totalStudents = totalStudents;
        this.docsUploaded = docsUploaded;
        this.submittedToUniversity = submittedToUniversity;
        this.submittedToMEA = submittedToMEA;
        this.telexIssued = telexIssued;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public long getTotalStudents() {
        return totalStudents;
    }

    public void setTotalStudents(long totalStudents) {
        this.totalStudents = totalStudents;
    }

    public long getDocsUploaded() {
        return docsUploaded;
    }

    public void setDocsUploaded(long docsUploaded) {
        this.docsUploaded = docsUploaded;
    }

    public long getSubmittedToUniversity() {
        return submittedToUniversity;
    }

    public void setSubmittedToUniversity(long submittedToUniversity) {
        this.submittedToUniversity = submittedToUniversity;
    }

    public long getSubmittedToMEA() {
        return submittedToMEA;
    }

    public void setSubmittedToMEA(long submittedToMEA) {
        this.submittedToMEA = submittedToMEA;
    }

    public long getTelexIssued() {
        return telexIssued;
    }

    public void setTelexIssued(long telexIssued) {
        this.telexIssued = telexIssued;
    }
}
