package com.cc.modals;

import java.time.LocalDate;
import java.time.LocalDateTime;
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

	@Column(nullable = false, unique = true, length = 100)
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

	@Column(columnDefinition = "TEXT", nullable = false)
	private String address;

	@Column(nullable = false)
	private String emergencyContactNo;

	@Column(columnDefinition = "TEXT", nullable = false)
	private String emergencyRelation;

	@Column(nullable = true, columnDefinition = "TEXT")
	private String passportNo;

	@Column(nullable = true)
	private LocalDate passportValidTill;

	@Column(name = "passport_path", columnDefinition = "TEXT")
	private String passportPath;

	@Column(columnDefinition = "TEXT", nullable = false)
	private String intake;

	@Column(columnDefinition = "TEXT", nullable = false)
	private String mothersName;

	@Column(columnDefinition = "TEXT", nullable = false)
	private String fathersName;

	@Column(nullable = false, columnDefinition = "TEXT")
	private String adhaarCardNo;

	@Column(columnDefinition = "TEXT")
	private String adhaarPath;

	@Column(name = "affidavit_path", columnDefinition = "TEXT")
	private String affidavitPath;

	// preferences: a student can prefer multiple colleges
	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(name = "student_college_preference", joinColumns = @JoinColumn(name = "student_id"), inverseJoinColumns = @JoinColumn(name = "college_id"))
	@JsonIgnoreProperties({ "admissionManager", "universityPerson" })
	private Set<College> collegePreferences = new HashSet<>();

	@Column(nullable = false)
	private String class12thPassingYear;

	@Column(columnDefinition = "TEXT", nullable = false)
	private String baords;

	@Column(nullable = false)
	private Double class12thMarks;

	@Column(nullable = false)
	private Double maxMarks;

	@Column(columnDefinition = "TEXT")
	private String markseetPath;

	@Column(nullable = false)
	private boolean hasGivenNEET;

	@Column(nullable = true)
	private Integer neetYear;

	@Column(nullable = true)
	private Integer neetScore;

	@Column(columnDefinition = "TEXT")
	private String neetMarksheetPath;

	@Column(columnDefinition = "TEXT", nullable = false)
	private String appliedFor;

	@Column(nullable = true)
	@Enumerated(EnumType.STRING)
	private StudentStatus nextStep;

	// assigned college (final assignment)
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "college_id")
	@JsonIgnoreProperties({ "admissionManager", "universityPerson" })
	private College college;

	@ManyToOne
	@JoinColumn(name = "admission_manager_id")
	@JsonIgnoreProperties("students")
	private AddmissionManager admissionManager;

	@Column(name = "confirmation_page_path", columnDefinition = "TEXT")
	private String confirmationPagePath;

	@Column(name = "admission_letter_path", columnDefinition = "TEXT")
	private String admissionLetterPath;

	@Column(name = "conditional_offer_letter_path", columnDefinition = "TEXT")
	private String conditionalOfferLetterPath;

	@Column(name = "profile_image_path", columnDefinition = "TEXT")
	private String profileImagePath;

	@Column(nullable = true, columnDefinition = "TEXT")
	private String updatedBy;

	@Column(nullable = true)
	private Boolean canRequestDocReturn;

	@Column(nullable = true)
	private Boolean hasRaiseReturnRequest = false;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "university_person_id")
	@JsonBackReference(value = "up-student")
	private UniversityPerson universityPerson;

	@ManyToOne
	@JoinColumn(name = "agent_id")
	@JsonIgnoreProperties("student")
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
	@Column(name = "telex_number", columnDefinition = "TEXT")
	private String telexNumber;

	@Column(name = "telex_issue_date")
	private LocalDate telexIssueDate;
	private LocalDate telexRequestDate;

	@Column(name = "telex_documents_path", columnDefinition = "TEXT")
	private String telexDocumentsPath;

	// New Fields for Telex Process
	@Column(name = "passport_validation_date")
	private LocalDate passportValidationDate;

	@Column(name = "telex_payment_amount")
	private Double telexPaymentAmount;

	@Column(name = "telex_payment_screenshot_path", columnDefinition = "TEXT")
	private String telexPaymentScreenshotPath;

	@Column(name = "telex_payment_tx_id", columnDefinition = "TEXT")
	private String telexPaymentTxId;

	@Column(name = "telex_payment_received_amount")
	private Double telexPaymentReceivedAmount;

	@Column(name = "telex_payment_received_date")
	private LocalDate telexPaymentReceivedDate;

	@Column(name = "telex_tuition_fee")
	private Boolean telexTuitionFee = false;

	@Column(name = "telex_hostel_fee")
	private Boolean telexHostelFee = false;

	@Column(name = "telex_mess_fee")
	private Boolean telexMessFee = false;

	@Column(name = "telex_contract_fee")
	private Boolean telexContractFee = false;

	@Column(name = "telex_registration_fee")
	private Boolean telexRegistrationFee = false;

	@Column(name = "telex_one_time_charge")
	private Boolean telexOneTimeCharge = false;

	@Column(name = "is_telex_accountant_approved")
	private Boolean isTelexAccountantApproved = false;

	@Column(name = "is_telex_doc_approved")
	private Boolean isTelexDocApproved = false;

	@Column(name = "doc_person_comment", columnDefinition = "TEXT")
	private String docPersonComment;

	@Column(name = "visa_talab_numa_path", columnDefinition = "TEXT")
	private String visaTalabNumaPath;

	@OneToMany(mappedBy = "student", cascade = CascadeType.ALL)
	// This side was likely already there, which caused the error
	@JsonManagedReference(value = "student-history")
	private List<StudentStatusHistory> studentStatusHistory = new java.util.ArrayList<>();

	// ======================= DOCUMENT TRACKING FIELDS =======================
	@Column(name = "original_doc_location", columnDefinition = "TEXT")
	private String originalDocLocation;

	@Column(name = "original_docs_sent")
	private Boolean originalDocsSent = false;

	@Enumerated(EnumType.STRING)
	@Column(name = "doc_delivery_method")
	private DocDeliveryMethod docDeliveryMethod;

	@Column(name = "courier_partner")
	private String courierPartner;

	@Column(name = "courier_tracking_id")
	private String courierTrackingId;

	@Column(name = "self_person_contact", columnDefinition = "TEXT")
	private String selfPersonContact;

	@Column(name = "doc_tentative_reach_date")
	private LocalDate docTentativeReachDate;

	@Column(name = "doc_received_date")
	private LocalDate docReceivedDate;

	// Document Receipt Tracking - which specific documents were received
	@Column(name = "received_12th_marksheet")
	private Boolean received12thMarksheet = false;

	@Column(name = "received_10th_marksheet")
	private Boolean received10thMarksheet = false;

	@Column(name = "received_neet_scorecard")
	private Boolean receivedNeetScorecard = false;

	@Column(name = "received_passport")
	private Boolean receivedPassport = false;

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

	@Column(name = "tt_copy_path", columnDefinition = "TEXT")
	private String ttCopyPath;

	@Column(name = "payment_confirmation_path", columnDefinition = "TEXT")
	private String paymentConfirmationPath;

	@Column(name = "payment_due_date")
	private LocalDate paymentDueDate;

	@Column(name = "payment_type", columnDefinition = "TEXT")
	@Enumerated(EnumType.STRING)
	private PaymentType paymentType;

	// ======================= PERSONAL/VISA INFORMATION =======================
	@Column(name = "surname", columnDefinition = "TEXT")
	private String surname;

	@Column(name = "first_name", columnDefinition = "TEXT")
	private String firstName;

	@Column(name = "middle_name", columnDefinition = "TEXT")
	private String middleName;

	@Column(name = "sex", columnDefinition = "TEXT")
	private String sex;

	@Column(name = "country_of_birth", columnDefinition = "TEXT")
	private String countryOfBirth;

	@Column(name = "place_of_birth", columnDefinition = "TEXT")
	private String placeOfBirth;

	@Enumerated(EnumType.STRING)
	@Column(name = "marital_status", columnDefinition = "TEXT")
	private MaritalStatus maritalStatus;

	@Column(name = "citizenship", columnDefinition = "TEXT")
	private String citizenship;

	@Column(name = "previous_citizenship", columnDefinition = "TEXT")
	private String previousCitizenship;

	@Enumerated(EnumType.STRING)
	@Column(name = "passport_type", columnDefinition = "TEXT")
	private PassportType passportType;

	@Column(name = "passport_issue_date")
	private LocalDate passportIssueDate;

	@Column(name = "passport_expiration_date")
	private LocalDate passportExpirationDate;

	@Column(name = "duration_of_visa_procedure", columnDefinition = "TEXT")
	private String durationOfVisaProcedure;

	@Column(name = "place_of_visa_issuance", columnDefinition = "TEXT")
	private String placeOfVisaIssuance;

	@Column(name = "visa_photo_path", columnDefinition = "TEXT")
	private String visaPhotoPath;

	@Column(name = "visa_number", columnDefinition = "TEXT")
	private String visaNumber;

	@Column(name = "visa_passport_copy_path", columnDefinition = "TEXT")
	private String visaPassportCopyPath;

	// ======================= TRAVEL DETAILS =======================
	@Column(name = "travel_doj")
	private LocalDate travelDoj;

	@Column(name = "travel_airlines", columnDefinition = "TEXT")
	private String travelAirlines;

	@Column(name = "travel_source", columnDefinition = "TEXT")
	private String travelSource;

	@Column(name = "travel_destination", columnDefinition = "TEXT")
	private String travelDestination;

	@Column(name = "travel_ticket_pdf_path", columnDefinition = "TEXT")
	private String travelTicketPdfPath;

	// ======================= VISA PAYMENT FIELDS =======================
	@Column(name = "visa_man_service_charge")
	private Double visaManServiceCharge;

	@Column(name = "visa_fee_payment_date")
	private LocalDateTime visaFeePaymentDate;

	@Column(name = "visa_payment_requested_amount")
	private Double visaPaymentRequestedAmount;

	@Column(name = "visa_payment_requested_currency", columnDefinition = "TEXT")
	private String visaPaymentRequestedCurrency;

	@Column(name = "visa_bank_details_sent_date")
	private LocalDate visaBankDetailsSentDate;

	@Column(name = "visa_payment_tx_id", columnDefinition = "TEXT")
	private String visaPaymentTxId;

	@Column(name = "visa_payment_confirmation_path", columnDefinition = "TEXT")
	private String visaPaymentConfirmationPath;

	@Column(name = "visa_payment_received_amount")
	private Double visaPaymentReceivedAmount;

	@Column(name = "visa_payment_received_date")
	private LocalDate visaPaymentReceivedDate;

	@Column(name = "acc_to_visaman_paid_amount")
	private Double accToVisaManPaidAmount = 0.0;

	@Column(name = "acc_to_visaman_payment_date")
	private LocalDate accToVisaManPaymentDate;

	@Column(name = "acc_to_visaman_tx_id", columnDefinition = "TEXT")
	private String accToVisaManTxId;

	@Column(name = "acc_to_visaman_mode", columnDefinition = "TEXT")
	private String accToVisaManMode;

	@Column(name = "acc_to_visaman_status", columnDefinition = "TEXT")
	private String accToVisaManStatus = "PENDING";

	// ======================= BANK DETAILS (Uzbekistan → India Transfer)
	// =======================
	@Column(name = "visa_bank_name", columnDefinition = "TEXT")
	private String visaBankName;

	@Column(name = "visa_bank_account_number", columnDefinition = "TEXT")
	private String visaBankAccountNumber;

	@Column(name = "visa_bank_swift_code", columnDefinition = "TEXT")
	private String visaBankSwiftCode;

	@Column(name = "visa_bank_beneficiary_name", columnDefinition = "TEXT")
	private String visaBankBeneficiaryName;

	@Column(name = "visa_bank_branch_address", columnDefinition = "TEXT")
	private String visaBankBranchAddress;

	@Column(name = "payment_rejection_remarks", columnDefinition = "TEXT")
	private String paymentRejectionRemarks;

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

	public String getAffidavitPath() {
		return affidavitPath;
	}

	public void setAffidavitPath(String affidavitPath) {
		this.affidavitPath = affidavitPath;
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
		return admissionLetterPath;
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

	public String getVisaTalabNumaPath() {
		return visaTalabNumaPath;
	}

	public void setVisaTalabNumaPath(String visaTalabNumaPath) {
		this.visaTalabNumaPath = visaTalabNumaPath;
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

	public LocalDate getTravelDoj() {
		return travelDoj;
	}

	public void setTravelDoj(LocalDate travelDoj) {
		this.travelDoj = travelDoj;
	}

	public String getTravelAirlines() {
		return travelAirlines;
	}

	public void setTravelAirlines(String travelAirlines) {
		this.travelAirlines = travelAirlines;
	}

	public String getTravelSource() {
		return travelSource;
	}

	public void setTravelSource(String travelSource) {
		this.travelSource = travelSource;
	}

	public String getTravelDestination() {
		return travelDestination;
	}

	public void setTravelDestination(String travelDestination) {
		this.travelDestination = travelDestination;
	}

	public String getTravelTicketPdfPath() {
		return travelTicketPdfPath;
	}

	public void setTravelTicketPdfPath(String travelTicketPdfPath) {
		this.travelTicketPdfPath = travelTicketPdfPath;
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

	public String getPaymentRejectionRemarks() {
		return paymentRejectionRemarks;
	}

	public void setPaymentRejectionRemarks(String paymentRejectionRemarks) {
		this.paymentRejectionRemarks = paymentRejectionRemarks;
	}

	@Column(name = "doc_person_assigned_date")
	private LocalDate docPersonAssignedDate;

	public LocalDate getDocPersonAssignedDate() {
		return docPersonAssignedDate;
	}

	public void setDocPersonAssignedDate(LocalDate docPersonAssignedDate) {
		this.docPersonAssignedDate = docPersonAssignedDate;
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

	public Double getVisaManServiceCharge() {
		return visaManServiceCharge;
	}

	public void setVisaManServiceCharge(Double visaManServiceCharge) {
		this.visaManServiceCharge = visaManServiceCharge;
	}

	public LocalDate getTelexIssueDate() {
		return telexIssueDate;
	}

	public void setTelexIssueDate(LocalDate telexIssueDate) {
		this.telexIssueDate = telexIssueDate;
	}

	public LocalDate getTelexRequestDate() {
		return telexRequestDate;
	}

	public void setTelexRequestDate(LocalDate telexRequestDate) {
		this.telexRequestDate = telexRequestDate;
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

	public LocalDate getTelexPaymentReceivedDate() {
		return telexPaymentReceivedDate;
	}

	public void setTelexPaymentReceivedDate(LocalDate telexPaymentReceivedDate) {
		this.telexPaymentReceivedDate = telexPaymentReceivedDate;
	}

	public Boolean getTelexTuitionFee() {
		return telexTuitionFee;
	}

	public void setTelexTuitionFee(Boolean telexTuitionFee) {
		this.telexTuitionFee = telexTuitionFee;
	}

	public Boolean getTelexHostelFee() {
		return telexHostelFee;
	}

	public void setTelexHostelFee(Boolean telexHostelFee) {
		this.telexHostelFee = telexHostelFee;
	}

	public Boolean getTelexMessFee() {
		return telexMessFee;
	}

	public void setTelexMessFee(Boolean telexMessFee) {
		this.telexMessFee = telexMessFee;
	}

	public Boolean getTelexContractFee() {
		return telexContractFee;
	}

	public void setTelexContractFee(Boolean telexContractFee) {
		this.telexContractFee = telexContractFee;
	}

	public Boolean getTelexRegistrationFee() {
		return telexRegistrationFee;
	}

	public void setTelexRegistrationFee(Boolean telexRegistrationFee) {
		this.telexRegistrationFee = telexRegistrationFee;
	}

	public Boolean getTelexOneTimeCharge() {
		return telexOneTimeCharge;
	}

	public void setTelexOneTimeCharge(Boolean telexOneTimeCharge) {
		this.telexOneTimeCharge = telexOneTimeCharge;
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

	public String getCourierPartner() {
		return courierPartner;
	}

	public void setCourierPartner(String courierPartner) {
		this.courierPartner = courierPartner;
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

	public Boolean getReceived12thMarksheet() {
		return received12thMarksheet;
	}

	public void setReceived12thMarksheet(Boolean received12thMarksheet) {
		this.received12thMarksheet = received12thMarksheet;
	}

	public Boolean getReceived10thMarksheet() {
		return received10thMarksheet;
	}

	public void setReceived10thMarksheet(Boolean received10thMarksheet) {
		this.received10thMarksheet = received10thMarksheet;
	}

	public Boolean getReceivedNeetScorecard() {
		return receivedNeetScorecard;
	}

	public void setReceivedNeetScorecard(Boolean receivedNeetScorecard) {
		this.receivedNeetScorecard = receivedNeetScorecard;
	}

	public Boolean getReceivedPassport() {
		return receivedPassport;
	}

	public void setReceivedPassport(Boolean receivedPassport) {
		this.receivedPassport = receivedPassport;
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

	public String getDurationOfVisaProcedure() {
		return durationOfVisaProcedure;
	}

	public void setDurationOfVisaProcedure(String durationOfVisaProcedure) {
		this.durationOfVisaProcedure = durationOfVisaProcedure;
	}

	public String getPlaceOfVisaIssuance() {
		return placeOfVisaIssuance;
	}

	public void setPlaceOfVisaIssuance(String placeOfVisaIssuance) {
		this.placeOfVisaIssuance = placeOfVisaIssuance;
	}

	public String getVisaPhotoPath() {
		return visaPhotoPath;
	}

	public void setVisaPhotoPath(String visaPhotoPath) {
		this.visaPhotoPath = visaPhotoPath;
	}

	public String getVisaNumber() {
		return visaNumber;
	}

	public void setVisaNumber(String visaNumber) {
		this.visaNumber = visaNumber;
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

	// ======================= VISA PAYMENT GETTERS/SETTERS =======================
	public LocalDateTime getVisaFeePaymentDate() {
		return visaFeePaymentDate;
	}

	public void setVisaFeePaymentDate(LocalDateTime visaFeePaymentDate) {
		this.visaFeePaymentDate = visaFeePaymentDate;
	}

	public Double getVisaPaymentRequestedAmount() {
		return visaPaymentRequestedAmount;
	}

	public void setVisaPaymentRequestedAmount(Double visaPaymentRequestedAmount) {
		this.visaPaymentRequestedAmount = visaPaymentRequestedAmount;
	}

	public String getVisaPaymentRequestedCurrency() {
		return visaPaymentRequestedCurrency;
	}

	public void setVisaPaymentRequestedCurrency(String visaPaymentRequestedCurrency) {
		this.visaPaymentRequestedCurrency = visaPaymentRequestedCurrency;
	}

	public LocalDate getVisaBankDetailsSentDate() {
		return visaBankDetailsSentDate;
	}

	public void setVisaBankDetailsSentDate(LocalDate visaBankDetailsSentDate) {
		this.visaBankDetailsSentDate = visaBankDetailsSentDate;
	}

	public String getVisaPaymentTxId() {
		return visaPaymentTxId;
	}

	public void setVisaPaymentTxId(String visaPaymentTxId) {
		this.visaPaymentTxId = visaPaymentTxId;
	}

	public String getVisaPaymentConfirmationPath() {
		return visaPaymentConfirmationPath;
	}

	public void setVisaPaymentConfirmationPath(String visaPaymentConfirmationPath) {
		this.visaPaymentConfirmationPath = visaPaymentConfirmationPath;
	}

	public Double getVisaPaymentReceivedAmount() {
		return visaPaymentReceivedAmount;
	}

	public void setVisaPaymentReceivedAmount(Double visaPaymentReceivedAmount) {
		this.visaPaymentReceivedAmount = visaPaymentReceivedAmount;
	}

	public LocalDate getVisaPaymentReceivedDate() {
		return visaPaymentReceivedDate;
	}

	public void setVisaPaymentReceivedDate(LocalDate visaPaymentReceivedDate) {
		this.visaPaymentReceivedDate = visaPaymentReceivedDate;
	}

	public Double getAccToVisaManPaidAmount() {
		return accToVisaManPaidAmount;
	}

	public void setAccToVisaManPaidAmount(Double accToVisaManPaidAmount) {
		this.accToVisaManPaidAmount = accToVisaManPaidAmount;
	}

	public LocalDate getAccToVisaManPaymentDate() {
		return accToVisaManPaymentDate;
	}

	public void setAccToVisaManPaymentDate(LocalDate accToVisaManPaymentDate) {
		this.accToVisaManPaymentDate = accToVisaManPaymentDate;
	}

	public String getAccToVisaManTxId() {
		return accToVisaManTxId;
	}

	public void setAccToVisaManTxId(String accToVisaManTxId) {
		this.accToVisaManTxId = accToVisaManTxId;
	}

	public String getAccToVisaManMode() {
		return accToVisaManMode;
	}

	public void setAccToVisaManMode(String accToVisaManMode) {
		this.accToVisaManMode = accToVisaManMode;
	}

	public String getAccToVisaManStatus() {
		return accToVisaManStatus;
	}

	public void setAccToVisaManStatus(String accToVisaManStatus) {
		this.accToVisaManStatus = accToVisaManStatus;
	}

	// ======================= BANK DETAILS GETTERS/SETTERS =======================
	public String getVisaBankName() {
		return visaBankName;
	}

	public void setVisaBankName(String visaBankName) {
		this.visaBankName = visaBankName;
	}

	public String getVisaBankAccountNumber() {
		return visaBankAccountNumber;
	}

	public void setVisaBankAccountNumber(String visaBankAccountNumber) {
		this.visaBankAccountNumber = visaBankAccountNumber;
	}

	public String getVisaBankSwiftCode() {
		return visaBankSwiftCode;
	}

	public void setVisaBankSwiftCode(String visaBankSwiftCode) {
		this.visaBankSwiftCode = visaBankSwiftCode;
	}

	public String getVisaBankBeneficiaryName() {
		return visaBankBeneficiaryName;
	}

	public void setVisaBankBeneficiaryName(String visaBankBeneficiaryName) {
		this.visaBankBeneficiaryName = visaBankBeneficiaryName;
	}

	public String getVisaBankBranchAddress() {
		return visaBankBranchAddress;
	}

	public void setVisaBankBranchAddress(String visaBankBranchAddress) {
		this.visaBankBranchAddress = visaBankBranchAddress;
	}

	public Boolean getCanRequestDocReturn() {
		return canRequestDocReturn;
	}

	public void setCanRequestDocReturn(Boolean canRequestDocReturn) {
		this.canRequestDocReturn = canRequestDocReturn;
	}

	public Boolean getHasRaiseReturnRequest() {
		return hasRaiseReturnRequest;
	}

	public void setHasRaiseReturnRequest(Boolean hasRaiseReturnRequest) {
		this.hasRaiseReturnRequest = hasRaiseReturnRequest;
	}

	@OneToOne(mappedBy = "student", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@JsonIgnoreProperties({ "student", "agent", "hibernateLazyInitializer", "handler" })
	private DocumentReturnRequest documentReturnRequest;

	public DocumentReturnRequest getDocumentReturnRequest() {
		return documentReturnRequest;
	}

	public void setDocumentReturnRequest(DocumentReturnRequest documentReturnRequest) {
		this.documentReturnRequest = documentReturnRequest;
	}

	// ── Computed transient fields for frontend consumption ──

	@jakarta.persistence.Transient
	@com.fasterxml.jackson.annotation.JsonProperty("agentFullName")
	public String getAgentFullName() {
		try {
			return agent != null ? agent.getFullName() : null;
		} catch (Exception e) {
			return null;
		}
	}

	@jakarta.persistence.Transient
	@com.fasterxml.jackson.annotation.JsonProperty("collegeName")
	public String getCollegeName() {
		try {
			return college != null ? college.getCollegeName() : null;
		} catch (Exception e) {
			return null;
		}
	}
}
