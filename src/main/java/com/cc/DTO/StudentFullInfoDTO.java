package com.cc.DTO;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import com.cc.modals.StudentStatus;

/**
 * Comprehensive DTO that returns ALL student information for passport-based
 * lookup.
 * Uses flat fields (no entity references) to prevent infinite JSON loops.
 * Agent is now a nested summary DTO instead of flat name/userId.
 */
public class StudentFullInfoDTO {

    // ======================= IDENTITY =======================
    private Long id;
    private String fullName;
    private String applicationId;
    private LocalDate dob;
    private Integer age;
    private String email;
    private String contactNo;
    private String address;
    private String emergencyContactNo;
    private String emergencyRelation;
    private String mothersName;
    private String fathersName;
    private LocalDate createdAt;

    // ======================= STATUS =======================
    private StudentStatus applicationStatus;
    private StudentStatus nextStep;
    private String paymentStatus;
    private String updatedBy;

    // ======================= PASSPORT & AADHAAR =======================
    private String passportNo;
    private LocalDate passportValidTill;
    private String adhaarCardNo;

    // ======================= EDUCATION =======================
    private String class12thPassingYear;
    private String baords;
    private Double class12thMarks;
    private Double maxMarks;
    private boolean hasGivenNEET;
    private Integer neetYear;
    private Integer neetScore;
    private String intake;
    private String appliedFor;

    // ======================= COLLEGE =======================
    private String collegeName;
    private Long collegeId;

    // ======================= ASSIGNED PERSONNEL =======================
    // Agent is now nested (with company details + college access)
    private AgentPassDTO agent;

    // Other personnel remain flat as per your existing code
    private String admissionManagerName;
    private String admissionManagerUsername;
    private String universityPersonName;
    private String telexManName;
    private String telexManUsername;
    private String visaManName;
    private String visaManUsername;
    private String docPersonName;
    private String docPersonUsername;
    private String accountantName;
    private String accountantUsername;

    // ======================= TELEX DETAILS =======================
    private String telexNumber;
    private LocalDate telexIssueDate;
    private LocalDate passportValidationDate;
    private Double telexPaymentAmount;
    private String telexPaymentTxId;
    private Double telexPaymentReceivedAmount;
    private LocalDate telexPaymentReceivedDate;
    private Boolean isTelexAccountantApproved;
    private Boolean isTelexDocApproved;
    private String docPersonComment;

    private Boolean telexTuitionFee;
    private Boolean telexHostelFee;
    private Boolean telexMessFee;
    private Boolean telexContractFee;
    private Boolean telexRegistrationFee;
    private Boolean telexOneTimeCharge;

    // ======================= VISA / PERSONAL =======================
    private String surname;
    private String firstName;
    private String middleName;
    private String sex;
    private String countryOfBirth;
    private String placeOfBirth;
    private String maritalStatus;
    private String citizenship;
    private String previousCitizenship;
    private String passportType;
    private LocalDate passportIssueDate;
    private LocalDate passportExpirationDate;
    private String durationOfVisaProcedure;
    private String placeOfVisaIssuance;

    // ======================= VISA PAYMENT =======================
    private Double visaManServiceCharge;
    private Double visaPaymentRequestedAmount;
    private String visaPaymentRequestedCurrency;
    private LocalDate visaBankDetailsSentDate;
    private String visaPaymentTxId;
    private Double visaPaymentReceivedAmount;
    private LocalDate visaPaymentReceivedDate;
    private Double accToVisaManPaidAmount;
    private LocalDate accToVisaManPaymentDate;
    private String accToVisaManTxId;
    private String accToVisaManMode;
    private String accToVisaManStatus;

    // ======================= BANK DETAILS =======================
    private String visaBankName;
    private String visaBankAccountNumber;
    private String visaBankSwiftCode;
    private String visaBankBeneficiaryName;
    private String visaBankBranchAddress;

    // ======================= DOCUMENT TRACKING =======================
    private String originalDocLocation;
    private Boolean originalDocsSent;
    private String docDeliveryMethod;
    private String courierPartner;
    private String courierTrackingId;
    private String selfPersonContact;
    private LocalDate docTentativeReachDate;
    private LocalDate docReceivedDate;
    private Boolean received12thMarksheet;
    private Boolean received10thMarksheet;
    private Boolean receivedNeetScorecard;
    private Boolean receivedPassport;
    private LocalDate docReturnedDate;
    private String docReturnMethod;
    private String docReturnTrackingId;
    private Boolean readyForDocReturn;
    private Boolean canRequestDocReturn;
    private Boolean hasRaiseReturnRequest;

    // ======================= PAYMENT =======================
    private Boolean hasPaid;
    private Double paidAmount;
    private String paidTo;
    private String paymentType;
    private LocalDate paymentDueDate;

    // ======================= FILE PATHS =======================
    private String profileImagePath;
    private String confirmationPagePath;
    private String admissionLetterPath;
    private String conditionalOfferLetterPath;
    private String passportPath;
    private String adhaarPath;
    private String markseetPath;
    private String neetMarksheetPath;
    private String telexDocumentsPath;
    private String telexPaymentScreenshotPath;
    private String ttCopyPath;
    private String paymentConfirmationPath;
    private String visaPhotoPath;
    private String visaPassportCopyPath;
    private String visaPaymentConfirmationPath;

    // ======================= ACTION =======================
    private String action;
    private String actionOnUser;

    // ======================= HISTORY =======================
    private List<StatusHistoryItem> statusHistory;

    // Inner class for timeline items
    public static class StatusHistoryItem {
        private StudentStatus status;
        private LocalDateTime updatedAt;
        private String updatedBy;
        private String remarks;

        public StatusHistoryItem() {
        }

        public StatusHistoryItem(StudentStatus status, LocalDateTime updatedAt, String updatedBy, String remarks) {
            this.status = status;
            this.updatedAt = updatedAt;
            this.updatedBy = updatedBy;
            this.remarks = remarks;
        }

        public StudentStatus getStatus() {
            return status;
        }

        public void setStatus(StudentStatus status) {
            this.status = status;
        }

        public LocalDateTime getUpdatedAt() {
            return updatedAt;
        }

        public void setUpdatedAt(LocalDateTime updatedAt) {
            this.updatedAt = updatedAt;
        }

        public String getUpdatedBy() {
            return updatedBy;
        }

        public void setUpdatedBy(String updatedBy) {
            this.updatedBy = updatedBy;
        }

        public String getRemarks() {
            return remarks;
        }

        public void setRemarks(String remarks) {
            this.remarks = remarks;
        }
    }

    // ======================= GETTERS & SETTERS =======================

    // Identity
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

    public LocalDate getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDate createdAt) {
        this.createdAt = createdAt;
    }

    // Status
    public StudentStatus getApplicationStatus() {
        return applicationStatus;
    }

    public void setApplicationStatus(StudentStatus applicationStatus) {
        this.applicationStatus = applicationStatus;
    }

    public StudentStatus getNextStep() {
        return nextStep;
    }

    public void setNextStep(StudentStatus nextStep) {
        this.nextStep = nextStep;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    // Passport & Aadhaar
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

    // Education
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

    // College
    public String getCollegeName() {
        return collegeName;
    }

    public void setCollegeName(String collegeName) {
        this.collegeName = collegeName;
    }

    public Long getCollegeId() {
        return collegeId;
    }

    public void setCollegeId(Long collegeId) {
        this.collegeId = collegeId;
    }

    // ======================= AGENT (Nested) =======================
    public AgentPassDTO getAgent() {
        return agent;
    }

    public void setAgent(AgentPassDTO agentDTO) {
        this.agent = agentDTO;
    }

    // Other Personnel (flat)
    public String getAdmissionManagerName() {
        return admissionManagerName;
    }

    public void setAdmissionManagerName(String admissionManagerName) {
        this.admissionManagerName = admissionManagerName;
    }

    public String getAdmissionManagerUsername() {
        return admissionManagerUsername;
    }

    public void setAdmissionManagerUsername(String admissionManagerUsername) {
        this.admissionManagerUsername = admissionManagerUsername;
    }

    public String getUniversityPersonName() {
        return universityPersonName;
    }

    public void setUniversityPersonName(String universityPersonName) {
        this.universityPersonName = universityPersonName;
    }

    public String getTelexManName() {
        return telexManName;
    }

    public void setTelexManName(String telexManName) {
        this.telexManName = telexManName;
    }

    public String getTelexManUsername() {
        return telexManUsername;
    }

    public void setTelexManUsername(String telexManUsername) {
        this.telexManUsername = telexManUsername;
    }

    public String getVisaManName() {
        return visaManName;
    }

    public void setVisaManName(String visaManName) {
        this.visaManName = visaManName;
    }

    public String getVisaManUsername() {
        return visaManUsername;
    }

    public void setVisaManUsername(String visaManUsername) {
        this.visaManUsername = visaManUsername;
    }

    public String getDocPersonName() {
        return docPersonName;
    }

    public void setDocPersonName(String docPersonName) {
        this.docPersonName = docPersonName;
    }

    public String getDocPersonUsername() {
        return docPersonUsername;
    }

    public void setDocPersonUsername(String docPersonUsername) {
        this.docPersonUsername = docPersonUsername;
    }

    public String getAccountantName() {
        return accountantName;
    }

    public void setAccountantName(String accountantName) {
        this.accountantName = accountantName;
    }

    public String getAccountantUsername() {
        return accountantUsername;
    }

    public void setAccountantUsername(String accountantUsername) {
        this.accountantUsername = accountantUsername;
    }

    // Telex
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

    // Visa / Personal, Visa Payment, Bank Details, Document Tracking,
    // Payment, File Paths, Action, History — all getters & setters below

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

    public String getMaritalStatus() {
        return maritalStatus;
    }

    public void setMaritalStatus(String maritalStatus) {
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

    public String getPassportType() {
        return passportType;
    }

    public void setPassportType(String passportType) {
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

    public Double getVisaManServiceCharge() {
        return visaManServiceCharge;
    }

    public void setVisaManServiceCharge(Double visaManServiceCharge) {
        this.visaManServiceCharge = visaManServiceCharge;
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

    public String getDocDeliveryMethod() {
        return docDeliveryMethod;
    }

    public void setDocDeliveryMethod(String docDeliveryMethod) {
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

    public LocalDate getDocReceivedDate() {
        return docReceivedDate;
    }

    public void setDocReceivedDate(LocalDate docReceivedDate) {
        this.docReceivedDate = docReceivedDate;
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

    public LocalDate getDocReturnedDate() {
        return docReturnedDate;
    }

    public void setDocReturnedDate(LocalDate docReturnedDate) {
        this.docReturnedDate = docReturnedDate;
    }

    public String getDocReturnMethod() {
        return docReturnMethod;
    }

    public void setDocReturnMethod(String docReturnMethod) {
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

    public String getPaidTo() {
        return paidTo;
    }

    public void setPaidTo(String paidTo) {
        this.paidTo = paidTo;
    }

    public String getPaymentType() {
        return paymentType;
    }

    public void setPaymentType(String paymentType) {
        this.paymentType = paymentType;
    }

    public LocalDate getPaymentDueDate() {
        return paymentDueDate;
    }

    public void setPaymentDueDate(LocalDate paymentDueDate) {
        this.paymentDueDate = paymentDueDate;
    }

    // File Paths
    public String getProfileImagePath() {
        return profileImagePath;
    }

    public void setProfileImagePath(String profileImagePath) {
        this.profileImagePath = profileImagePath;
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

    public String getConditionalOfferLetterPath() {
        return conditionalOfferLetterPath;
    }

    public void setConditionalOfferLetterPath(String conditionalOfferLetterPath) {
        this.conditionalOfferLetterPath = conditionalOfferLetterPath;
    }

    public String getPassportPath() {
        return passportPath;
    }

    public void setPassportPath(String passportPath) {
        this.passportPath = passportPath;
    }

    public String getAdhaarPath() {
        return adhaarPath;
    }

    public void setAdhaarPath(String adhaarPath) {
        this.adhaarPath = adhaarPath;
    }

    public String getMarkseetPath() {
        return markseetPath;
    }

    public void setMarkseetPath(String markseetPath) {
        this.markseetPath = markseetPath;
    }

    public String getNeetMarksheetPath() {
        return neetMarksheetPath;
    }

    public void setNeetMarksheetPath(String neetMarksheetPath) {
        this.neetMarksheetPath = neetMarksheetPath;
    }

    public String getTelexDocumentsPath() {
        return telexDocumentsPath;
    }

    public void setTelexDocumentsPath(String telexDocumentsPath) {
        this.telexDocumentsPath = telexDocumentsPath;
    }

    public String getTelexPaymentScreenshotPath() {
        return telexPaymentScreenshotPath;
    }

    public void setTelexPaymentScreenshotPath(String telexPaymentScreenshotPath) {
        this.telexPaymentScreenshotPath = telexPaymentScreenshotPath;
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

    public String getVisaPaymentConfirmationPath() {
        return visaPaymentConfirmationPath;
    }

    public void setVisaPaymentConfirmationPath(String visaPaymentConfirmationPath) {
        this.visaPaymentConfirmationPath = visaPaymentConfirmationPath;
    }

    // Action
    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getActionOnUser() {
        return actionOnUser;
    }

    public void setActionOnUser(String actionOnUser) {
        this.actionOnUser = actionOnUser;
    }

    // History
    public List<StatusHistoryItem> getStatusHistory() {
        return statusHistory;
    }

    public void setStatusHistory(List<StatusHistoryItem> statusHistory) {
        this.statusHistory = statusHistory;
    }
}