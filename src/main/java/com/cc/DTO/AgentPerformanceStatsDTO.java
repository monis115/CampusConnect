package com.cc.DTO;

public class AgentPerformanceStatsDTO {
    private String agentName;
    private String agentUsername;
    private long registeredStudents;
    private long telexInitiated;
    private long paymentConfirmations;
    private long docsRequested;
    private long totalWork;
    private String period; // DAILY, MONTHLY, YEARLY

    public AgentPerformanceStatsDTO() {
    }

    public AgentPerformanceStatsDTO(String agentName, String agentUsername, long registeredStudents,
            long telexInitiated, long paymentConfirmations, long docsRequested, String period) {
        this.agentName = agentName;
        this.agentUsername = agentUsername;
        this.registeredStudents = registeredStudents;
        this.telexInitiated = telexInitiated;
        this.paymentConfirmations = paymentConfirmations;
        this.docsRequested = docsRequested;
        this.totalWork = registeredStudents + telexInitiated + paymentConfirmations + docsRequested;
        this.period = period;
    }

    public String getAgentName() {
        return agentName;
    }

    public void setAgentName(String agentName) {
        this.agentName = agentName;
    }

    public String getAgentUsername() {
        return agentUsername;
    }

    public void setAgentUsername(String agentUsername) {
        this.agentUsername = agentUsername;
    }

    public long getRegisteredStudents() {
        return registeredStudents;
    }

    public void setRegisteredStudents(long registeredStudents) {
        this.registeredStudents = registeredStudents;
    }

    public long getTelexInitiated() {
        return telexInitiated;
    }

    public void setTelexInitiated(long telexInitiated) {
        this.telexInitiated = telexInitiated;
    }

    public long getPaymentConfirmations() {
        return paymentConfirmations;
    }

    public void setPaymentConfirmations(long paymentConfirmations) {
        this.paymentConfirmations = paymentConfirmations;
    }

    public long getDocsRequested() {
        return docsRequested;
    }

    public void setDocsRequested(long docsRequested) {
        this.docsRequested = docsRequested;
    }

    public long getTotalWork() {
        return totalWork;
    }

    public void setTotalWork(long totalWork) {
        this.totalWork = totalWork;
    }

    public String getPeriod() {
        return period;
    }

    public void setPeriod(String period) {
        this.period = period;
    }
}
