package com.cc.DTO;

public class DocPersonStatsDTO {
    private String period; // Today, Monthly, Yearly, All
    private Long totalAssignedStudents;
    private Long documentsReceived;
    private Long documentsVerified;
    private Long documentsCollected;
    private Long documentsReturned;

    public DocPersonStatsDTO() {
    }

    public DocPersonStatsDTO(String period, Long totalAssignedStudents, Long documentsReceived, Long documentsVerified,
            Long documentsCollected, Long documentsReturned) {
        this.period = period;
        this.totalAssignedStudents = totalAssignedStudents;
        this.documentsReceived = documentsReceived;
        this.documentsVerified = documentsVerified;
        this.documentsCollected = documentsCollected;
        this.documentsReturned = documentsReturned;
    }

    public String getPeriod() {
        return period;
    }

    public void setPeriod(String period) {
        this.period = period;
    }

    public Long getTotalAssignedStudents() {
        return totalAssignedStudents;
    }

    public void setTotalAssignedStudents(Long totalAssignedStudents) {
        this.totalAssignedStudents = totalAssignedStudents;
    }

    public Long getDocumentsReceived() {
        return documentsReceived;
    }

    public void setDocumentsReceived(Long documentsReceived) {
        this.documentsReceived = documentsReceived;
    }

    public Long getDocumentsVerified() {
        return documentsVerified;
    }

    public void setDocumentsVerified(Long documentsVerified) {
        this.documentsVerified = documentsVerified;
    }

    public Long getDocumentsCollected() {
        return documentsCollected;
    }

    public void setDocumentsCollected(Long documentsCollected) {
        this.documentsCollected = documentsCollected;
    }

    public Long getDocumentsReturned() {
        return documentsReturned;
    }

    public void setDocumentsReturned(Long documentsReturned) {
        this.documentsReturned = documentsReturned;
    }
}
