package com.cc.DTO;

public class AdvancedTransactionRequestDTO {
    private String role; // "Agent", "DocManager", "University", "VisaMan"
    private String userId; // Specific user's name/ID
    private String collegeName; // If agent is selected, optional college
    private String startDate;
    private String endDate;

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getCollegeName() { return collegeName; }
    public void setCollegeName(String collegeName) { this.collegeName = collegeName; }

    public String getStartDate() { return startDate; }
    public void setStartDate(String startDate) { this.startDate = startDate; }

    public String getEndDate() { return endDate; }
    public void setEndDate(String endDate) { this.endDate = endDate; }
}
