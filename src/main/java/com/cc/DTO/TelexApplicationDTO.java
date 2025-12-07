package com.cc.DTO;

import java.time.LocalDate;
import com.cc.modals.*;

public class TelexApplicationDTO {

    // Agent Info
    private String agentUsername;

    // ======================= DOCUMENT TRACKING =======================
    private String originalDocLocation;
    private Boolean originalDocsSent;
    private DocDeliveryMethod docDeliveryMethod;
    private String courierTrackingId;
    private String courierTrackingNumber; // Added to handle frontend payload variation
    private String courierPartner; // Added to handle frontend payload
    private String selfPersonContact;
    private LocalDate docTentativeReachDate;
    private String otherCourierDetails; // For "Other" courier partner selection

    // ======================= PAYMENT DETAILS =======================
    private Boolean hasPaid;
    private Double paidAmount;
    private PaymentRecipient paidTo;
    private LocalDate paymentDueDate;
    private PaymentType paymentType;

    // ======================= PERSONAL/VISA INFORMATION =======================
    // Note: * indicates mandatory fields - validation handled by frontend and
    // service layer
    private String surname; // *

    private String firstName; // *

    private String middleName;

    private String sex; // *

    private String countryOfBirth; // *

    private LocalDate dateOfBirth; // *

    private String placeOfBirth; // *

    private MaritalStatus maritalStatus; // *

    private String citizenship; // *

    private String previousCitizenship;

    private PassportType passportType; // *

    private LocalDate passportIssueDate; // *

    private LocalDate passportExpirationDate; // *

    private String passportNumber; // *

    private String durationOfVisaProcedure;
    private String placeOfVisaIssuance;

    // ======================= GETTERS & SETTERS =======================

    public String getAgentUsername() {
        return agentUsername;
    }

    public void setAgentUsername(String agentUsername) {
        this.agentUsername = agentUsername;
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

    public String getCourierTrackingNumber() {
        return courierTrackingNumber;
    }

    public void setCourierTrackingNumber(String courierTrackingNumber) {
        this.courierTrackingNumber = courierTrackingNumber;
    }

    public String getCourierPartner() {
        return courierPartner;
    }

    public void setCourierPartner(String courierPartner) {
        this.courierPartner = courierPartner;
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

    public String getOtherCourierDetails() {
        return otherCourierDetails;
    }

    public void setOtherCourierDetails(String otherCourierDetails) {
        this.otherCourierDetails = otherCourierDetails;
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

    public PaymentRecipient getPaidTo() {
        return paidTo;
    }

    public void setPaidTo(PaymentRecipient paidTo) {
        this.paidTo = paidTo;
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

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
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

    public String getPassportNumber() {
        return passportNumber;
    }

    public void setPassportNumber(String passportNumber) {
        this.passportNumber = passportNumber;
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

}
