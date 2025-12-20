package com.cc.DTO;

import java.util.Map;

public class VisaManStatsDTO {
    private String visaManagerName;
    private String period; // DAILY, MONTHLY, YEARLY

    // Stats Counters
    private long totalAssignedStudents;
    private long paymentRequested;
    private long paymentConfirmed;
    private long passportCollected;
    private long visaApplied;
    private long embassySubmitted;
    private long visaStamped;
    private long passportReturned; // Documents returned to DocPerson

    // Additional breakdown or trend data can be added here if needed
    // private Map<String, Long> yearlyTrend;

    public VisaManStatsDTO() {
    }

    public String getVisaManagerName() {
        return visaManagerName;
    }

    public void setVisaManagerName(String visaManagerName) {
        this.visaManagerName = visaManagerName;
    }

    public String getPeriod() {
        return period;
    }

    public void setPeriod(String period) {
        this.period = period;
    }

    public long getTotalAssignedStudents() {
        return totalAssignedStudents;
    }

    public void setTotalAssignedStudents(long totalAssignedStudents) {
        this.totalAssignedStudents = totalAssignedStudents;
    }

    public long getPaymentRequested() {
        return paymentRequested;
    }

    public void setPaymentRequested(long paymentRequested) {
        this.paymentRequested = paymentRequested;
    }

    public long getPaymentConfirmed() {
        return paymentConfirmed;
    }

    public void setPaymentConfirmed(long paymentConfirmed) {
        this.paymentConfirmed = paymentConfirmed;
    }

    public long getPassportCollected() {
        return passportCollected;
    }

    public void setPassportCollected(long passportCollected) {
        this.passportCollected = passportCollected;
    }

    public long getVisaApplied() {
        return visaApplied;
    }

    public void setVisaApplied(long visaApplied) {
        this.visaApplied = visaApplied;
    }

    public long getEmbassySubmitted() {
        return embassySubmitted;
    }

    public void setEmbassySubmitted(long embassySubmitted) {
        this.embassySubmitted = embassySubmitted;
    }

    public long getVisaStamped() {
        return visaStamped;
    }

    public void setVisaStamped(long visaStamped) {
        this.visaStamped = visaStamped;
    }

    public long getPassportReturned() {
        return passportReturned;
    }

    public void setPassportReturned(long passportReturned) {
        this.passportReturned = passportReturned;
    }
}
