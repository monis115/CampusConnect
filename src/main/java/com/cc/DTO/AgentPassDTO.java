package com.cc.DTO;

import java.time.LocalDate;
import java.util.Set;

/**
 * Summary DTO for Agent - Contains only necessary information for Student view
 */
public class AgentPassDTO {

    private Long id;
    private String fullName;
    private String userId; // username / login id
    private String email;
    private String contactNo;
    private String gender;
    private String designation;
    private String status;

    // Company Profile - Basic details only
    private String companyName;
    private String registeredBusinessName;
    private String businessType;
    private String registrationNumber;
    private String panOrGstNumber;
    private String registeredOfficeAddress;
    private String companyEmail;
    private String companyContactNo;
    private String website;

    // College Access
    private Set<CollegeAccessDTO> collegeAccess;

    // ======================= Constructors =======================
    public AgentPassDTO() {
    }

    // ======================= Getters & Setters =======================
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

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getContactNo() {
        return contactNo;
    }

    public void setContactNo(String contactNo) {
        this.contactNo = contactNo;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getDesignation() {
        return designation;
    }

    public void setDesignation(String designation) {
        this.designation = designation;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    // Company Getters & Setters
    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getRegisteredBusinessName() {
        return registeredBusinessName;
    }

    public void setRegisteredBusinessName(String registeredBusinessName) {
        this.registeredBusinessName = registeredBusinessName;
    }

    public String getBusinessType() {
        return businessType;
    }

    public void setBusinessType(String businessType) {
        this.businessType = businessType;
    }

    public String getRegistrationNumber() {
        return registrationNumber;
    }

    public void setRegistrationNumber(String registrationNumber) {
        this.registrationNumber = registrationNumber;
    }

    public String getPanOrGstNumber() {
        return panOrGstNumber;
    }

    public void setPanOrGstNumber(String panOrGstNumber) {
        this.panOrGstNumber = panOrGstNumber;
    }

    public String getRegisteredOfficeAddress() {
        return registeredOfficeAddress;
    }

    public void setRegisteredOfficeAddress(String registeredOfficeAddress) {
        this.registeredOfficeAddress = registeredOfficeAddress;
    }

    public String getCompanyEmail() {
        return companyEmail;
    }

    public void setCompanyEmail(String companyEmail) {
        this.companyEmail = companyEmail;
    }

    public String getCompanyContactNo() {
        return companyContactNo;
    }

    public void setCompanyContactNo(String companyContactNo) {
        this.companyContactNo = companyContactNo;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public Set<CollegeAccessDTO> getCollegeAccess() {
        return collegeAccess;
    }

    public void setCollegeAccess(Set<CollegeAccessDTO> collegeAccess) {
        this.collegeAccess = collegeAccess;
    }
}