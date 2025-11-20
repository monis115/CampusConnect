package com.cc.modals;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.*;

@Entity
@Table(name = "student")
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
public class Student {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String fullName;

	@Column(nullable = false)
	private LocalDate dob;

	@Column(nullable = false)
	private Integer age;

	@Column(nullable = false)
	private LocalDate createdAt;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private StudentStatus applicationStatus;

	@Column(nullable = false, unique = true)
	private String email;

	@Column(nullable = false)
	private String contactNo;

	@Column(nullable = false)
	private String address;

	@Column(nullable = false)
	private String emergencyContactNo;

	@Column(nullable = false)
	private String emergencyRelation;

	@Column(nullable = false)
	private String passportNo;

	@Column(nullable = false)
	private LocalDate passportValidTill;

	@Column(nullable = false)
	private String intake;

	@Column(nullable = false)
	private String mothersName;

	@Column(nullable = false)
	private String fathersName;

	@Column(nullable = false)
	private String adhaarCardNo;

	@Column(nullable = false)
	private String adhaarPath;

	// preferences: a student can prefer multiple colleges
	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(name = "student_college_preference", joinColumns = @JoinColumn(name = "student_id"), inverseJoinColumns = @JoinColumn(name = "college_id"))
	private Set<College> collegePreferences = new HashSet<>();

	@Column(nullable = false)
	private String class12thPassingYear;

	@Column(nullable = false)
	private String baords;

	@Column(nullable = false)
	private Double class12thMarks;

	@Column(nullable = false)
	private Double maxMarks;

	@Column(nullable = false)
	private String markseetPath;

	@Column(nullable = false)
	private boolean hasGivenNEET;

	@Column(nullable = true)
	private Integer neetYear;

	@Column(nullable = true)
	private Integer neetScore;

	@Column(nullable = false)
	private String appliedFor;

	@Column(nullable = true)
	@Enumerated(EnumType.STRING)
	private StudentStatus nextStep;

	// assigned college (final assignment)
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "college_id")
	@JsonIgnoreProperties({ "admissionManager", "universityPerson" })
	private College college;

	@ManyToOne
	@JoinColumn(name = "admission_manager_id")
	@JsonBackReference(value = "manager-student")
	private AddmissionManager admissionManager;

	@Column(name = "confirmation_page_path", nullable = true)
	private String confirmationPagePath;

	@Column(name = "admission_letter_path", nullable = true)
	private String admissionLetterPath;

	@Column(name = "profile_image_path", nullable = true)
	private String profileImagePath;

	@Column(nullable = true)
	private String updatedBy;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "university_person_id")
	@JsonBackReference(value = "up-student")
	private UniversityPerson universityPerson;

	@ManyToOne
	@JoinColumn(name = "agent_id")
	@JsonBackReference(value = "agent-student")
	private Agent agent;
	// Inside Student.java

	@OneToMany(mappedBy = "student", cascade = CascadeType.ALL)
	// This side was likely already there, which caused the error
	@JsonManagedReference(value = "student-history")
	private List<StudentStatusHistory> studentStatusHistory = new java.util.ArrayList<>();

	// Constructors, minimal for brevity
	public Student() {
	}

	// getters & setters (only essential ones shown for brevity - add the rest as
	// needed)

	public Long getId() {
		return id;
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

	public LocalDate getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDate createdAt) {
		this.createdAt = createdAt;
	}

	public StudentStatus getApplicationStatus() {
		return applicationStatus;
	}

	public void setApplicationStatus(StudentStatus applicationStatus) {
		this.applicationStatus = applicationStatus;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public Set<College> getCollegePreferences() {
		return collegePreferences;
	}

	public void setCollegePreferences(Set<College> collegePreferences) {
		this.collegePreferences = collegePreferences;
	}

	public UniversityPerson getUniversityPerson() {
		return universityPerson;
	}

	public void setUniversityPerson(UniversityPerson universityPerson) {
		this.universityPerson = universityPerson;
	}

	public List<StudentStatusHistory> getStudentStatusHistory() {
		return studentStatusHistory;
	}

	public void setStudentStatusHistory(List<StudentStatusHistory> studentStatusHistory) {
		this.studentStatusHistory = studentStatusHistory;
	}

	public College getCollege() {
		return college;
	}

	public void setCollege(College college) {
		this.college = college;
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

	public String getIntake() {
		return intake;
	}

	public void setIntake(String intake) {
		this.intake = intake;
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

	public String getAppliedFor() {
		return appliedFor;
	}

	public void setAppliedFor(String appliedFor) {
		this.appliedFor = appliedFor;
	}

	public StudentStatus getNextStep() {
		return nextStep;
	}

	public void setNextStep(StudentStatus nextStep) {
		this.nextStep = nextStep;
	}

	public AddmissionManager getAdmissionManager() {
		return admissionManager;
	}

	public void setAdmissionManager(AddmissionManager admissionManager) {
		this.admissionManager = admissionManager;
	}

	public String getConfirmationPagePath() {
		return confirmationPagePath;
	}

	public void setConfirmationPagePath(String confirmationPagePath) {
		this.confirmationPagePath = confirmationPagePath;
	}

	public String getAdmissionLetterPath() {
		// Only return admission letter path if it's been approved
		if (this.applicationStatus == StudentStatus.ADMISSION_LETTER_APPROVED) {
			return admissionLetterPath;
		}
		return null; // Hide from students/agents until approved
	}

	// Internal method for service layer to check if file actually exists
	// This bypasses the security check
	public String getAdmissionLetterPathInternal() {
		return admissionLetterPath;
	}

	public void setAdmissionLetterPath(String admissionLetterPath) {
		this.admissionLetterPath = admissionLetterPath;
	}

	public String getProfileImagePath() {
		return profileImagePath;
	}

	public void setProfileImagePath(String profileImagePath) {
		this.profileImagePath = profileImagePath;
	}

	public String getUpdatedBy() {
		return updatedBy;
	}

	public void setUpdatedBy(String updatedBy) {
		this.updatedBy = updatedBy;
	}

	public Agent getAgent() {
		return agent;
	}

	public void setAgent(Agent agent) {
		this.agent = agent;
	}

	public void setId(Long id) {
		this.id = id;
	}

	// add remaining getters/setters as required by your service logic
}
