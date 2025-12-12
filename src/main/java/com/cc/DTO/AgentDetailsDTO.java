package com.cc.DTO;

import java.time.LocalDate;
import java.util.List;

public class AgentDetailsDTO {

    private Long id;

    // --- Agent Personal Details ---
    private String fullName;
    private Integer age;
    private LocalDate dob;
    private String email;
    private String contactNo;
    private String gender;
    private String address;
    private String identityProofType;
    private String identityProofNumber;
    private String designation;
    private String experience;
    private String status;
    private LocalDate activationDate;
    private LocalDate creationDate;

    // --- Company Details ---
    private String companyName;
    private String registeredBusinessName;
    private String businessType;
    private String registrationNumber;
    private String panOrGstNumber;
    private String registeredOfficeAddress;
    private String branchOfficeAddress;
    private String companyEmail;
    private String companyContactNo;
    private String website;

    // --- Login Credentials ---
    private String userId;
    private String password; // Included as per "all means all" request
    private String role;

    // --- Relationships ---
    private List<AgentCollegeAccessDTO> collegeAccessList;

    public AgentDetailsDTO() {
    }

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

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public LocalDate getDob() {
        return dob;
    }

    public void setDob(LocalDate dob) {
        this.dob = dob;
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

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getIdentityProofType() {
        return identityProofType;
    }

    public void setIdentityProofType(String identityProofType) {
        this.identityProofType = identityProofType;
    }

    public String getIdentityProofNumber() {
        return identityProofNumber;
    }

    public void setIdentityProofNumber(String identityProofNumber) {
        this.identityProofNumber = identityProofNumber;
    }

    public String getDesignation() {
        return designation;
    }

    public void setDesignation(String designation) {
        this.designation = designation;
    }

    public String getExperience() {
        return experience;
    }

    public void setExperience(String experience) {
        this.experience = experience;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDate getActivationDate() {
        return activationDate;
    }

    public void setActivationDate(LocalDate activationDate) {
        this.activationDate = activationDate;
    }

    public LocalDate getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(LocalDate creationDate) {
        this.creationDate = creationDate;
    }

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

    public String getBranchOfficeAddress() {
        return branchOfficeAddress;
    }

    public void setBranchOfficeAddress(String branchOfficeAddress) {
        this.branchOfficeAddress = branchOfficeAddress;
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

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public List<AgentCollegeAccessDTO> getCollegeAccessList() {
        return collegeAccessList;
    }

    public void setCollegeAccessList(List<AgentCollegeAccessDTO> collegeAccessList) {
        this.collegeAccessList = collegeAccessList;
    }
}
