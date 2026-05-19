package com.cc.DTO;

import java.time.LocalDate;

public class TelexDetailsDTO {
    private LocalDate passportValidationDate;
    private Double telexPaymentAmount;
    private String telexPaymentTxId;
    private String passportNumber;
    private String agentUsername;

    private Boolean telexTuitionFee;
    private Boolean telexHostelFee;
    private Boolean telexMessFee;
    private Boolean telexContractFee;
    private Boolean telexRegistrationFee;
    private Boolean telexOneTimeCharge;

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

    public String getPassportNumber() {
        return passportNumber;
    }

    public void setPassportNumber(String passportNumber) {
        this.passportNumber = passportNumber;
    }

    public String getAgentUsername() {
        return agentUsername;
    }

    public void setAgentUsername(String agentUsername) {
        this.agentUsername = agentUsername;
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
}
