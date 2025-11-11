package com.cc.modals;

import java.time.LocalDate;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "student")
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

    @Column(nullable = false)
    private String applicationStatus;

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

    // Student can have multiple preferred colleges
    @ManyToMany
    @JoinTable(
        name = "student_college_preferences",
        joinColumns = @JoinColumn(name = "student_id"),
        inverseJoinColumns = @JoinColumn(name = "college_id")
    )
    private List<College> collegePreferences;

    @Column(nullable = false)
    private String highestDegree;

    @Column(nullable = false)
    private Double marks;

    @Column(nullable = false)
    private String appliedFor;
    
    
    @Column(nullable = true)
    @Enumerated(EnumType.STRING)
    private StudentStatus presentStatus;

    @Column(nullable = true)
    @Enumerated(EnumType.STRING)
    private StudentStatus nextStep;
    @ManyToOne
    @JoinColumn(name = "college_id")
    private College college;


    
    @ManyToOne
    @JoinColumn(name = "admission_manager_id")
    @JsonBackReference(value = "manager-student")
    private AddmissionManager admissionManager;
    
    
    @Column(name = "confirmation_page_path", nullable = true)
    private String confirmationPagePath;

    @Column(name = "admission_letter_path", nullable = true)
    private String admissionLetterPath;
    
    
    
    @Column(nullable=true)
    private String updatedBy;


    public College getCollege() {
		return college;
	}

	public void setCollege(College college) {
		this.college = college;
	}


	public StudentStatus getPresentStatus() {
		return presentStatus;
	}

	public void setPresentStatus(StudentStatus presentStatus) {
		this.presentStatus = presentStatus;
	}

	public StudentStatus getNextStep() {
		return nextStep;
	}

	public void setNextStep(StudentStatus nextStep) {
		this.nextStep = nextStep;
	}

	// One-to-one relation with Agent
	@ManyToOne
	@JoinColumn(name = "agent_id")
	@JsonBackReference(value = "agent-student") // <-- CHANGE TO BACK
	private Agent agent;

    public Student() {}

    public Student(String fullName, LocalDate dob, Integer age, LocalDate createdAt, String applicationStatus,
                   String email, String contactNo, String address, String emergencyContactNo, String emergencyRelation,
                   String passportNo, LocalDate passportValidTill, String highestDegree, Double marks, String appliedFor) {
        this.fullName = fullName;
        this.dob = dob;
        this.age = age;
        this.createdAt = createdAt;
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

    public List<College> getCollegePreferences() {
        return collegePreferences;
    }

    public void setCollegePreferences(List<College> collegePreferences) {
        this.collegePreferences = collegePreferences;
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

    public Agent getAgent() {
        return agent;
    }

    public void setAgent(Agent agent) {
        this.agent = agent;
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
	    return admissionLetterPath;
	}

	public void setAdmissionLetterPath(String admissionLetterPath) {
	    this.admissionLetterPath = admissionLetterPath;
	}
    
    
}
