package com.cc.modals;

import java.time.LocalDate;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "agent")

@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })

public class Agent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // -------------------- Agent Personal Details --------------------
    @Column(nullable = false)
    private String fullName;

    @Column(nullable = false)
    private Integer age;

    @Column(nullable = false)
    private LocalDate dob;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String contactNo;

    @Column(nullable = false)
    private String gender;

    @Column(nullable = false)
    private String address;

    @Column(nullable = false)
    private String identityProofType;

    @Column(nullable = false)
    private String identityProofNumber;

    @Column(nullable = false)
    private String designation;

    @Column
    private String experience;

    @Column(nullable = false)
    private String status; // Active / Inactive / Pending Approval

    @Column(nullable = true)
    private LocalDate activationDate;

    @Column(nullable = false)
    private LocalDate creationDate;

    // -------------------- Company Details --------------------
    @Column(nullable = false)
    private String companyName;

    @Column
    private String registeredBusinessName;

    @Column(nullable = false)
    private String businessType; // Pvt. Ltd., LLP, etc.

    @Column(nullable = false)
    private String registrationNumber;

    @Column(nullable = false)
    private String panOrGstNumber;

    @Column(nullable = false)
    private String registeredOfficeAddress;

    @Column
    private String branchOfficeAddress;

    @Column(nullable = false)
    private String companyEmail;

    @Column(nullable = false)
    private String companyContactNo;

    @Column
    private String website;

    // -------------------- Login Credentials --------------------
    @Column(nullable = false, unique = true)
    private String userId;

    @Column(nullable = false)
    private String password;

    @Column
    private String role;

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    // -------------------- Relationships --------------------
    @OneToMany(mappedBy = "agent", cascade = jakarta.persistence.CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private java.util.Set<AgentCollegeAccess> collegeAccess;

    @OneToMany(mappedBy = "agent")
    @JsonManagedReference(value = "agent-student") // <-- CHANGE TO MANAGED
    private List<Student> student;

    // -------------------- Constructors --------------------
    public Agent() {
    }

    public Agent(String fullName, Integer age, LocalDate dob, String email, String contactNo, String status,
            LocalDate activationDate, LocalDate creationDate, List<College> colleges) {
        this.fullName = fullName;
        this.age = age;
        this.dob = dob;
        this.email = email;
        this.contactNo = contactNo;
        this.status = status;
        this.activationDate = activationDate;
        this.creationDate = creationDate;
        this.creationDate = creationDate;
        // this.collegePrivileges = colleges; // Removed
        this.role = "agent";
    }

    // -------------------- Getters and Setters --------------------
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

    public java.util.Set<AgentCollegeAccess> getCollegeAccess() {
        return collegeAccess;
    }

    public void setCollegeAccess(java.util.Set<AgentCollegeAccess> collegeAccess) {
        this.collegeAccess = collegeAccess;
    }

    public List<Student> getStudent() {
        return student;
    }

    public void setStudent(List<Student> student) {
        this.student = student;
    }
}
