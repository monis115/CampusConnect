package com.cc.DTO;

import java.time.LocalDate;

public class TelexDetailsDTO {
    private LocalDate passportValidationDate;
    private Double telexPaymentAmount;
    private String telexPaymentTxId;
    private String passportNumber;
    private String agentUsername;

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
}
