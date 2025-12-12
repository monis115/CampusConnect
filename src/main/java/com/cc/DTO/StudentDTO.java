package com.cc.DTO;

import java.time.LocalDate;
import java.util.List;

public class StudentDTO {

    // Personal & Contact
    private String fullName;
    private LocalDate dob;
    private Integer age;
    private String email;
    private String contactNo;
    private String address;
    private Integer districtId; // Used for validation
    private String emergencyContactNo;
    private String emergencyRelation;

    // Parents
    private String mothersName;
    private String fathersName;

    // ID Documents
    private String passportNo;
    private LocalDate passportValidTill;
    private String adhaarCardNo;
    private String adhaarPath;
    private String passportPath;

    // Academic (12th)
    private String class12thPassingYear;
    private String baords; // Matches Entity spelling
    private Double class12thMarks;
    private Double maxMarks;
    private String markseetPath; // Matches Entity spelling

    // NEET Details
    private boolean hasGivenNEET;
    private Integer neetYear;
    private Integer neetScore;
    private String neetMarksheetPath;

    // Application Details
    private String intake;
    private String appliedFor;
    private String applicationStatus; // Optional (default to REGISTERED in service)

    // File Paths (Optional for input, usually set by system)
    private String confirmationPagePath;
    private String admissionLetterPath;

    // Relationships
    private List<Long> collegeIds;
    private List<String> collegeNames; // [NEW]
    private String userNameAgent;
    private String assignedCollegeName; // [NEW]

    public StudentDTO() {
    }

    // Getters and Setters

    public List<String> getCollegeNames() {
        return collegeNames;
    }

    public void setCollegeNames(List<String> collegeNames) {
        this.collegeNames = collegeNames;
    }

    public String getAssignedCollegeName() {
        return assignedCollegeName;
    }

    public void setAssignedCollegeName(String assignedCollegeName) {
        this.assignedCollegeName = assignedCollegeName;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public LocalDate getDob() {
        return dob;
    }

    public void setDob(LocalDate dob) {
        this.dob = dob;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
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

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Integer getDistrictId() {
        return districtId;
    }

    public void setDistrictId(Integer districtId) {
        this.districtId = districtId;
    }

    public String getEmergencyContactNo() {
        return emergencyContactNo;
    }

    public void setEmergencyContactNo(String emergencyContactNo) {
        this.emergencyContactNo = emergencyContactNo;
    }

    public String getEmergencyRelation() {
        return emergencyRelation;
    }

    public void setEmergencyRelation(String emergencyRelation) {
        this.emergencyRelation = emergencyRelation;
    }

    public String getMothersName() {
        return mothersName;
    }

    public void setMothersName(String mothersName) {
        this.mothersName = mothersName;
    }

    public String getFathersName() {
        return fathersName;
    }

    public void setFathersName(String fathersName) {
        this.fathersName = fathersName;
    }

    public String getPassportNo() {
        return passportNo;
    }

    public void setPassportNo(String passportNo) {
        this.passportNo = passportNo;
    }

    public LocalDate getPassportValidTill() {
        return passportValidTill;
    }

    public void setPassportValidTill(LocalDate passportValidTill) {
        this.passportValidTill = passportValidTill;
    }

    public String getAdhaarCardNo() {
        return adhaarCardNo;
    }

    public void setAdhaarCardNo(String adhaarCardNo) {
        this.adhaarCardNo = adhaarCardNo;
    }

    public String getAdhaarPath() {
        return adhaarPath;
    }

    public void setAdhaarPath(String adhaarPath) {
        this.adhaarPath = adhaarPath;
    }

    public String getPassportPath() {
        return passportPath;
    }

    public void setPassportPath(String passportPath) {
        this.passportPath = passportPath;
    }

    public String getClass12thPassingYear() {
        return class12thPassingYear;
    }

    public void setClass12thPassingYear(String class12thPassingYear) {
        this.class12thPassingYear = class12thPassingYear;
    }

    public String getBaords() {
        return baords;
    }

    public void setBaords(String baords) {
        this.baords = baords;
    }

    public Double getClass12thMarks() {
        return class12thMarks;
    }

    public void setClass12thMarks(Double class12thMarks) {
        this.class12thMarks = class12thMarks;
    }

    public Double getMaxMarks() {
        return maxMarks;
    }

    public void setMaxMarks(Double maxMarks) {
        this.maxMarks = maxMarks;
    }

    public String getMarkseetPath() {
        return markseetPath;
    }

    public void setMarkseetPath(String markseetPath) {
        this.markseetPath = markseetPath;
    }

    public boolean isHasGivenNEET() {
        return hasGivenNEET;
    }

    public void setHasGivenNEET(boolean hasGivenNEET) {
        this.hasGivenNEET = hasGivenNEET;
    }

    public Integer getNeetYear() {
        return neetYear;
    }

    public void setNeetYear(Integer neetYear) {
        this.neetYear = neetYear;
    }

    public Integer getNeetScore() {
        return neetScore;
    }

    public void setNeetScore(Integer neetScore) {
        this.neetScore = neetScore;
    }

    public String getNeetMarksheetPath() {
        return neetMarksheetPath;
    }

    public void setNeetMarksheetPath(String neetMarksheetPath) {
        this.neetMarksheetPath = neetMarksheetPath;
    }

    public String getIntake() {
        return intake;
    }

    public void setIntake(String intake) {
        this.intake = intake;
    }

    public String getAppliedFor() {
        return appliedFor;
    }

    public void setAppliedFor(String appliedFor) {
        this.appliedFor = appliedFor;
    }

    public String getApplicationStatus() {
        return applicationStatus;
    }

    public void setApplicationStatus(String applicationStatus) {
        this.applicationStatus = applicationStatus;
    }

    public String getConfirmationPagePath() {
        return confirmationPagePath;
    }

    public void setConfirmationPagePath(String confirmationPagePath) {
        this.confirmationPagePath = confirmationPagePath;
    }

    public String getAdmissionLetterPath() {
        return admissionLetterPath;
    }

    public void setAdmissionLetterPath(String admissionLetterPath) {
        this.admissionLetterPath = admissionLetterPath;
    }

    public List<Long> getCollegeIds() {
        return collegeIds;
    }

    public void setCollegeIds(List<Long> collegeIds) {
        this.collegeIds = collegeIds;
    }

    public String getUserNameAgent() {
        return userNameAgent;
    }

    public void setUserNameAgent(String userNameAgent) {
        this.userNameAgent = userNameAgent;
    }
}