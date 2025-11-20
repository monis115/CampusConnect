package com.cc.DTO;

import java.util.List;

public class AddmissionManagerCreateDTO {
    private String fullName;
    private String email;
    private String contactNumber;
    private List<Long> collegeIds;

    public AddmissionManagerCreateDTO() {
    }

    // getters and setters
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

    public String getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }

    public List<Long> getCollegeIds() {
        return collegeIds;
    }

    public void setCollegeIds(List<Long> collegeIds) {
        this.collegeIds = collegeIds;
    }
}
