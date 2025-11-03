package com.cc.DTO;

import java.time.LocalDate;
import java.util.List;

public class StudentDTO {

    private String fullName;
    private LocalDate dob;
    private Integer age;
    private String applicationStatus;
    private String email;
    private String contactNo;
    private String address;
    private Integer districtId;
	private String emergencyContactNo;
    private String emergencyRelation;
    private String passportNo;
    private LocalDate passportValidTill;
    private String highestDegree;
    private Double marks;
    private String appliedFor;
    

    // Sending only college IDs (not full objects)
    private List<Long> collegeIds;
    public StudentDTO(String fullName, LocalDate dob, Integer age, String applicationStatus, String email,
			String contactNo, String address, Integer districtId, String emergencyContactNo, String emergencyRelation,
			String passportNo, LocalDate passportValidTill, String highestDegree, Double marks, String appliedFor,
			List<Long> collegeIds, String userNameAgent) {
		super();
		this.fullName = fullName;
		this.dob = dob;
		this.age = age;
		this.applicationStatus = applicationStatus;
		this.email = email;
		this.contactNo = contactNo;
		this.address = address;
		this.districtId = districtId;
		this.emergencyContactNo = emergencyContactNo;
		this.emergencyRelation = emergencyRelation;
		this.passportNo = passportNo;
		this.passportValidTill = passportValidTill;
		this.highestDegree = highestDegree;
		this.marks = marks;
		this.appliedFor = appliedFor;
		this.collegeIds = collegeIds;
		this.userNameAgent = userNameAgent;
	}



    // Sending agent's username instead of agent object
    private String userNameAgent;

    public StudentDTO() {}

    public StudentDTO(String fullName, LocalDate dob, Integer age, 
                      String applicationStatus, String email, String contactNo, String address,
                      String emergencyContactNo, String emergencyRelation, String passportNo,
                      LocalDate passportValidTill, String highestDegree, Double marks, 
                      String appliedFor, List<Long> collegeIds, String userNameAgent) {
        this.fullName = fullName;
        this.dob = dob;
        this.age = age;
        this.applicationStatus = applicationStatus;
        this.email = email;
        this.contactNo = contactNo;
        this.address = address;
        this.emergencyContactNo = emergencyContactNo;
        this.emergencyRelation = emergencyRelation;
        this.passportNo = passportNo;
        this.passportValidTill = passportValidTill;
        this.highestDegree = highestDegree;
        this.marks = marks;
        this.appliedFor = appliedFor;
        this.collegeIds = collegeIds;
        this.userNameAgent = userNameAgent;
    }
    
    

    // Getters & Setters

   

    public Integer getDistrictId() {
		return districtId;
	}

	public void setDistrictId(Integer districtId) {
		this.districtId = districtId;
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



    public String getApplicationStatus() {
        return applicationStatus;
    }

    public void setApplicationStatus(String applicationStatus) {
        this.applicationStatus = applicationStatus;
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

    public String getHighestDegree() {
        return highestDegree;
    }

    public void setHighestDegree(String highestDegree) {
        this.highestDegree = highestDegree;
    }

    public Double getMarks() {
        return marks;
    }

    public void setMarks(Double marks) {
        this.marks = marks;
    }

    public String getAppliedFor() {
        return appliedFor;
    }

    public void setAppliedFor(String appliedFor) {
        this.appliedFor = appliedFor;
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
