package com.cc.DTO;

import java.util.List;

public class UpdateAdmissionManagerDTO {
    private String fullName;
    private String email;
    private String contactNumber;
    private String role;
    private String status;
    private List<Long> collegeIds;  // Array of college IDs to assign

    // Constructors
    public UpdateAdmissionManagerDTO() {}

    // Getters and Setters
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getContactNumber() { return contactNumber; }
    public void setContactNumber(String contactNumber) { this.contactNumber = contactNumber; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public List<Long> getCollegeIds() { return collegeIds; }
    public void setCollegeIds(List<Long> collegeIds) { this.collegeIds = collegeIds; }
}