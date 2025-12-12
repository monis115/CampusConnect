package com.cc.DTO;

public class AgentSummaryDTO {
    private Long id;
    private String fullName;
    private String email;
    private String companyName;
    private String contactNo;
    private String status;
    private String userId;

    private int collegeAccessCount;
    private int locationAccessCount;

    public AgentSummaryDTO(Long id, String fullName, String email, String companyName, String contactNo, String status,
            String userId, int collegeAccessCount, int locationAccessCount) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.companyName = companyName;
        this.contactNo = contactNo;
        this.status = status;
        this.userId = userId;
        this.collegeAccessCount = collegeAccessCount;
        this.locationAccessCount = locationAccessCount;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getContactNo() {
        return contactNo;
    }

    public void setContactNo(String contactNo) {
        this.contactNo = contactNo;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public int getCollegeAccessCount() {
        return collegeAccessCount;
    }

    public void setCollegeAccessCount(int collegeAccessCount) {
        this.collegeAccessCount = collegeAccessCount;
    }

    public int getLocationAccessCount() {
        return locationAccessCount;
    }

    public void setLocationAccessCount(int locationAccessCount) {
        this.locationAccessCount = locationAccessCount;
    }
}
