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

	@Column(nullable = false, unique = true)
	private String applicationId;

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

	@Column(name = "passport_path", nullable = true)
	private String passportPath;

	@Column(nullable = false)
	private String intake;

	@Column(nullable = false)
	private String mothersName;

	@Column(nullable = false)
	private String fathersName;

	@Column(nullable = false)
	private String adhaarCardNo;

	@Column(nullable = true)
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

	@Column(nullable = true)
	private String markseetPath;

	@Column(nullable = false)
	private boolean hasGivenNEET;

	@Column(nullable = true)
	private Integer neetYear;

	@Column(nullable = true)
	private Integer neetScore;

	@Column(nullable = true)
	private String neetMarksheetPath;

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

	@Column(name = "conditional_offer_letter_path", nullable = true)
	private String conditionalOfferLetterPath;

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

	@Column(nullable = false)
	private String paymentStatus = "Pending"; // Default to Pending

	// Relationships with Specialized Roles
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "telex_man_id")
	@JsonIgnoreProperties("students")
	private TelexMan telexMan;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "visa_man_id")
	@JsonIgnoreProperties("students")
	private VisaMan visaMan;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "doc_person_id")
	@JsonIgnoreProperties("students")
	private DocPerson docPerson;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "accountant_id")
	@JsonIgnoreProperties("students")
	private Accountant accountant;

	// Telex Details
	@Column(name = "telex_number")
	private String telexNumber;

	@Column(name = "telex_issue_date")
	private LocalDate telexIssueDate;

	@Column(name = "telex_documents_path", length = 1000)
	private String telexDocumentsPath;

	// New Fields for Telex Process
	@Column(name = "passport_validation_date")
	private LocalDate passportValidationDate;

	@Column(name = "telex_payment_amount")
	private Double telexPaymentAmount;

	@Column(name = "telex_payment_screenshot_path")
	private String telexPaymentScreenshotPath;

	@Column(name = "telex_payment_tx_id")
	private String telexPaymentTxId;

	@Column(name = "telex_payment_received_amount")
	private Double telexPaymentReceivedAmount;

	@Column(name = "is_telex_accountant_approved")
	private Boolean isTelexAccountantApproved = false;

	@Column(name = "is_telex_doc_approved")
	private Boolean isTelexDocApproved = false;

	@Column(name = "doc_person_comment")
	private String docPersonComment;

	@OneToMany(mappedBy = "student", cascade = CascadeType.ALL)
	// This side was likely already there, which caused the error
	@JsonManagedReference(value = "student-history")
	private List<StudentStatusHistory> studentStatusHistory = new java.util.ArrayList<>();

	// ======================= DOCUMENT TRACKING FIELDS =======================
	@Column(name = "original_doc_location")
	private String originalDocLocation;

	@Column(name = "original_docs_sent")
	private Boolean originalDocsSent = false;

	@Enumerated(EnumType.STRING)
	@Column(name = "doc_delivery_method")
	private DocDeliveryMethod docDeliveryMethod;

	@Column(name = "courier_tracking_id")
	private String courierTrackingId;

	@Column(name = "self_person_contact")
	private String selfPersonContact;

	@Column(name = "doc_tentative_reach_date")
	private LocalDate docTentativeReachDate;

	@Column(name = "doc_received_date")
	private LocalDate docReceivedDate;

	@Column(name = "doc_returned_date")
	private LocalDate docReturnedDate;

	@Enumerated(EnumType.STRING)
	@Column(name = "doc_return_method")
	private DocDeliveryMethod docReturnMethod;

	@Column(name = "doc_return_tracking_id")
	private String docReturnTrackingId;

	@Column(name = "ready_for_doc_return")
	private Boolean readyForDocReturn = false;

	// ======================= PAYMENT DETAILS =======================
	@Column(name = "has_paid")
	private Boolean hasPaid = false;

	@Column(name = "paid_amount")
	private Double paidAmount;

	@Enumerated(EnumType.STRING)
	@Column(name = "paid_to")
	private PaymentRecipient paidTo;

	@Column(name = "tt_copy_path")
	private String ttCopyPath;

	@Column(name = "payment_confirmation_path")
	private String paymentConfirmationPath;

	@Column(name = "payment_due_date")
	private LocalDate paymentDueDate;

	@Enumerated(EnumType.STRING)
	@Column(name = "payment_type")
	private PaymentType paymentType;

	// ======================= PERSONAL/VISA INFORMATION =======================
	@Column(name = "surname")
	private String surname;

	@Column(name = "first_name")
	private String firstName;

	@Column(name = "middle_name")
	private String middleName;

	@Column(name = "sex")
	private String sex;

	@Column(name = "country_of_birth")
	private String countryOfBirth;

	@Column(name = "place_of_birth")
	private String placeOfBirth;

	@Enumerated(EnumType.STRING)
	@Column(name = "marital_status")
	private MaritalStatus maritalStatus;

	@Column(name = "citizenship")
	private String citizenship;

	@Column(name = "previous_citizenship")
	private String previousCitizenship;

	@Enumerated(EnumType.STRING)
	@Column(name = "passport_type")
	private PassportType passportType;

	@Column(name = "passport_issued_by")
	private String passportIssuedBy;

	@Column(name = "passport_issue_date")
	private LocalDate passportIssueDate;

	@Column(name = "passport_expiration_date")
	private LocalDate passportExpirationDate;

	@Column(name = "visa_photo_path")
	private String visaPhotoPath;

	@Column(name = "visa_passport_copy_path")
	private String visaPassportCopyPath;

	private Enum action;
	private Enum actionOnUser;

	public void setAction(Enum action) {
		this.action = action;
	}

	public void setActionOnUser(Enum actionOnUser) {
		this.actionOnUser = actionOnUser;
	}

	public Enum getAction() {
		return action;
	}

	public Enum getActionOnUser() {
		return actionOnUser;
	}

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

	public String getApplicationId() {
		return applicationId;
	}

	public void setApplicationId(String applicationId) {
		this.applicationId = applicationId;
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

	public String getPassportPath() {
		return passportPath;
	}

	public void setPassportPath(String passportPath) {
		this.passportPath = passportPath;
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

	public String getNeetMarksheetPath() {
		return neetMarksheetPath;
	}

	public void setNeetMarksheetPath(String neetMarksheetPath) {
		this.neetMarksheetPath = neetMarksheetPath;
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
		if (this.applicationStatus == StudentStatus.ADMISSION_LETTER_GENERATED) {
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

	public String getConditionalOfferLetterPath() {
		return conditionalOfferLetterPath;
	}

	public void setConditionalOfferLetterPath(String conditionalOfferLetterPath) {
		this.conditionalOfferLetterPath = conditionalOfferLetterPath;
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

	public String getPaymentStatus() {
		return paymentStatus;
	}

	public void setPaymentStatus(String paymentStatus) {
		this.paymentStatus = paymentStatus;
	}

	public TelexMan getTelexMan() {
		return telexMan;
	}

	public void setTelexMan(TelexMan telexMan) {
		this.telexMan = telexMan;
	}

	public VisaMan getVisaMan() {
		return visaMan;
	}

	public void setVisaMan(VisaMan visaMan) {
		this.visaMan = visaMan;
	}

	public DocPerson getDocPerson() {
		return docPerson;
	}

	public void setDocPerson(DocPerson docPerson) {
		this.docPerson = docPerson;
	}

	public Accountant getAccountant() {
		return accountant;
	}

	public void setAccountant(Accountant accountant) {
		this.accountant = accountant;
	}

	public String getTelexNumber() {
		return telexNumber;
	}

	public void setTelexNumber(String telexNumber) {
		this.telexNumber = telexNumber;
	}

	public LocalDate getTelexIssueDate() {
		return telexIssueDate;
	}

	public void setTelexIssueDate(LocalDate telexIssueDate) {
		this.telexIssueDate = telexIssueDate;
	}

	public String getTelexDocumentsPath() {
		return telexDocumentsPath;
	}

	public void setTelexDocumentsPath(String telexDocumentsPath) {
		this.telexDocumentsPath = telexDocumentsPath;
	}

	public LocalDate getPassportValidationDate() {
		return passportValidationDate;
	}

	public void setPassportValidationDate(LocalDate passportValidationDate) {
		this.passportValidationDate = passportValidationDate;
	}

	public Double getTelexPaymentAmount() {
		return telexPaymentAmount;
	}

	public void setTelexPaymentAmount(Double telexPaymentAmount) {
		this.telexPaymentAmount = telexPaymentAmount;
	}

	public String getTelexPaymentScreenshotPath() {
		return telexPaymentScreenshotPath;
	}

	public void setTelexPaymentScreenshotPath(String telexPaymentScreenshotPath) {
		this.telexPaymentScreenshotPath = telexPaymentScreenshotPath;
	}

	public String getTelexPaymentTxId() {
		return telexPaymentTxId;
	}

	public void setTelexPaymentTxId(String telexPaymentTxId) {
		this.telexPaymentTxId = telexPaymentTxId;
	}

	public Double getTelexPaymentReceivedAmount() {
		return telexPaymentReceivedAmount;
	}

	public void setTelexPaymentReceivedAmount(Double telexPaymentReceivedAmount) {
		this.telexPaymentReceivedAmount = telexPaymentReceivedAmount;
	}

	public Boolean getIsTelexAccountantApproved() {
		return isTelexAccountantApproved;
	}

	public void setIsTelexAccountantApproved(Boolean isTelexAccountantApproved) {
		this.isTelexAccountantApproved = isTelexAccountantApproved;
	}

	public Boolean getIsTelexDocApproved() {
		return isTelexDocApproved;
	}

	public void setIsTelexDocApproved(Boolean isTelexDocApproved) {
		this.isTelexDocApproved = isTelexDocApproved;
	}

	public String getDocPersonComment() {
		return docPersonComment;
	}

	public void setDocPersonComment(String docPersonComment) {
		this.docPersonComment = docPersonComment;
	}

	// ======================= DOCUMENT TRACKING GETTERS/SETTERS
	// =======================
	public String getOriginalDocLocation() {
		return originalDocLocation;
	}

	public void setOriginalDocLocation(String originalDocLocation) {
		this.originalDocLocation = originalDocLocation;
	}

	public Boolean getOriginalDocsSent() {
		return originalDocsSent;
	}

	public void setOriginalDocsSent(Boolean originalDocsSent) {
		this.originalDocsSent = originalDocsSent;
	}

	public DocDeliveryMethod getDocDeliveryMethod() {
		return docDeliveryMethod;
	}

	public void setDocDeliveryMethod(DocDeliveryMethod docDeliveryMethod) {
		this.docDeliveryMethod = docDeliveryMethod;
	}

	public String getCourierTrackingId() {
		return courierTrackingId;
	}

	public void setCourierTrackingId(String courierTrackingId) {
		this.courierTrackingId = courierTrackingId;
	}

	public String getSelfPersonContact() {
		return selfPersonContact;
	}

	public void setSelfPersonContact(String selfPersonContact) {
		this.selfPersonContact = selfPersonContact;
	}

	public LocalDate getDocTentativeReachDate() {
		return docTentativeReachDate;
	}

	public void setDocTentativeReachDate(LocalDate docTentativeReachDate) {
		this.docTentativeReachDate = docTentativeReachDate;
	}

	// ======================= PAYMENT DETAILS GETTERS/SETTERS
	// =======================
	public Boolean getHasPaid() {
		return hasPaid;
	}

	public void setHasPaid(Boolean hasPaid) {
		this.hasPaid = hasPaid;
	}

	public Double getPaidAmount() {
		return paidAmount;
	}

	public void setPaidAmount(Double paidAmount) {
		this.paidAmount = paidAmount;
	}

	public PaymentRecipient getPaidTo() {
		return paidTo;
	}

	public void setPaidTo(PaymentRecipient paidTo) {
		this.paidTo = paidTo;
	}

	public String getTtCopyPath() {
		return ttCopyPath;
	}

	public void setTtCopyPath(String ttCopyPath) {
		this.ttCopyPath = ttCopyPath;
	}

	public String getPaymentConfirmationPath() {
		return paymentConfirmationPath;
	}

	public void setPaymentConfirmationPath(String paymentConfirmationPath) {
		this.paymentConfirmationPath = paymentConfirmationPath;
	}

	public LocalDate getPaymentDueDate() {
		return paymentDueDate;
	}

	public void setPaymentDueDate(LocalDate paymentDueDate) {
		this.paymentDueDate = paymentDueDate;
	}

	public PaymentType getPaymentType() {
		return paymentType;
	}

	public void setPaymentType(PaymentType paymentType) {
		this.paymentType = paymentType;
	}

	// ======================= PERSONAL/VISA INFO GETTERS/SETTERS
	// =======================
	public String getSurname() {
		return surname;
	}

	public void setSurname(String surname) {
		this.surname = surname;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getMiddleName() {
		return middleName;
	}

	public void setMiddleName(String middleName) {
		this.middleName = middleName;
	}

	public String getSex() {
		return sex;
	}

	public void setSex(String sex) {
		this.sex = sex;
	}

	public String getCountryOfBirth() {
		return countryOfBirth;
	}

	public void setCountryOfBirth(String countryOfBirth) {
		this.countryOfBirth = countryOfBirth;
	}

	public String getPlaceOfBirth() {
		return placeOfBirth;
	}

	public void setPlaceOfBirth(String placeOfBirth) {
		this.placeOfBirth = placeOfBirth;
	}

	public MaritalStatus getMaritalStatus() {
		return maritalStatus;
	}

	public void setMaritalStatus(MaritalStatus maritalStatus) {
		this.maritalStatus = maritalStatus;
	}

	public String getCitizenship() {
		return citizenship;
	}

	public void setCitizenship(String citizenship) {
		this.citizenship = citizenship;
	}

	public String getPreviousCitizenship() {
		return previousCitizenship;
	}

	public void setPreviousCitizenship(String previousCitizenship) {
		this.previousCitizenship = previousCitizenship;
	}

	public PassportType getPassportType() {
		return passportType;
	}

	public void setPassportType(PassportType passportType) {
		this.passportType = passportType;
	}

	public String getPassportIssuedBy() {
		return passportIssuedBy;
	}

	public void setPassportIssuedBy(String passportIssuedBy) {
		this.passportIssuedBy = passportIssuedBy;
	}

	public LocalDate getPassportIssueDate() {
		return passportIssueDate;
	}

	public void setPassportIssueDate(LocalDate passportIssueDate) {
		this.passportIssueDate = passportIssueDate;
	}

	public LocalDate getPassportExpirationDate() {
		return passportExpirationDate;
	}

	public void setPassportExpirationDate(LocalDate passportExpirationDate) {
		this.passportExpirationDate = passportExpirationDate;
	}

	public String getVisaPhotoPath() {
		return visaPhotoPath;
	}

	public void setVisaPhotoPath(String visaPhotoPath) {
		this.visaPhotoPath = visaPhotoPath;
	}

	public String getVisaPassportCopyPath() {
		return visaPassportCopyPath;
	}

	public void setVisaPassportCopyPath(String visaPassportCopyPath) {
		this.visaPassportCopyPath = visaPassportCopyPath;
	}

	public LocalDate getDocReceivedDate() {
		return docReceivedDate;
	}

	public void setDocReceivedDate(LocalDate docReceivedDate) {
		this.docReceivedDate = docReceivedDate;
	}

	public LocalDate getDocReturnedDate() {
		return docReturnedDate;
	}

	public void setDocReturnedDate(LocalDate docReturnedDate) {
		this.docReturnedDate = docReturnedDate;
	}

	public DocDeliveryMethod getDocReturnMethod() {
		return docReturnMethod;
	}

	public void setDocReturnMethod(DocDeliveryMethod docReturnMethod) {
		this.docReturnMethod = docReturnMethod;
	}

	public String getDocReturnTrackingId() {
		return docReturnTrackingId;
	}

	public void setDocReturnTrackingId(String docReturnTrackingId) {
		this.docReturnTrackingId = docReturnTrackingId;
	}

	public Boolean getReadyForDocReturn() {
		return readyForDocReturn;
	}

	public void setReadyForDocReturn(Boolean readyForDocReturn) {
		this.readyForDocReturn = readyForDocReturn;
	}
}
