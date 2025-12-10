package com.cc.DTO;

/**
 * DTO containing role-specific summaries for the admin dashboard.
 */
public class RoleSummariesDTO {

    private AgentSummary agent;
    private AdmissionManagerSummary admissionManager;
    private UniversityCoordinatorSummary universityCoordinator;
    private DocPersonSummary docPerson;
    private AccountantSummary accountant;
    private TelexPersonSummary telexPerson;
    private VisaPersonSummary visaPerson;

    public RoleSummariesDTO() {
    }

    // Getters and Setters
    public AgentSummary getAgent() {
        return agent;
    }

    public void setAgent(AgentSummary agent) {
        this.agent = agent;
    }

    public AdmissionManagerSummary getAdmissionManager() {
        return admissionManager;
    }

    public void setAdmissionManager(AdmissionManagerSummary admissionManager) {
        this.admissionManager = admissionManager;
    }

    public UniversityCoordinatorSummary getUniversityCoordinator() {
        return universityCoordinator;
    }

    public void setUniversityCoordinator(UniversityCoordinatorSummary universityCoordinator) {
        this.universityCoordinator = universityCoordinator;
    }

    public DocPersonSummary getDocPerson() {
        return docPerson;
    }

    public void setDocPerson(DocPersonSummary docPerson) {
        this.docPerson = docPerson;
    }

    public AccountantSummary getAccountant() {
        return accountant;
    }

    public void setAccountant(AccountantSummary accountant) {
        this.accountant = accountant;
    }

    public TelexPersonSummary getTelexPerson() {
        return telexPerson;
    }

    public void setTelexPerson(TelexPersonSummary telexPerson) {
        this.telexPerson = telexPerson;
    }

    public VisaPersonSummary getVisaPerson() {
        return visaPerson;
    }

    public void setVisaPerson(VisaPersonSummary visaPerson) {
        this.visaPerson = visaPerson;
    }

    // Inner classes for each role summary
    public static class AgentSummary {
        private Long totalStudents;
        private Long pendingApplications;
        private Long visaApproved;
        private Long rejected;

        public AgentSummary() {
        }

        public AgentSummary(Long totalStudents, Long pendingApplications, Long visaApproved, Long rejected) {
            this.totalStudents = totalStudents;
            this.pendingApplications = pendingApplications;
            this.visaApproved = visaApproved;
            this.rejected = rejected;
        }

        public Long getTotalStudents() {
            return totalStudents;
        }

        public void setTotalStudents(Long totalStudents) {
            this.totalStudents = totalStudents;
        }

        public Long getPendingApplications() {
            return pendingApplications;
        }

        public void setPendingApplications(Long pendingApplications) {
            this.pendingApplications = pendingApplications;
        }

        public Long getVisaApproved() {
            return visaApproved;
        }

        public void setVisaApproved(Long visaApproved) {
            this.visaApproved = visaApproved;
        }

        public Long getRejected() {
            return rejected;
        }

        public void setRejected(Long rejected) {
            this.rejected = rejected;
        }
    }

    public static class AdmissionManagerSummary {
        private Long newApplications;
        private Long pendingUniversityAction;
        private Long admissionLettersGenerated;

        public AdmissionManagerSummary() {
        }

        public AdmissionManagerSummary(Long newApplications, Long pendingUniversityAction,
                Long admissionLettersGenerated) {
            this.newApplications = newApplications;
            this.pendingUniversityAction = pendingUniversityAction;
            this.admissionLettersGenerated = admissionLettersGenerated;
        }

        public Long getNewApplications() {
            return newApplications;
        }

        public void setNewApplications(Long newApplications) {
            this.newApplications = newApplications;
        }

        public Long getPendingUniversityAction() {
            return pendingUniversityAction;
        }

        public void setPendingUniversityAction(Long pendingUniversityAction) {
            this.pendingUniversityAction = pendingUniversityAction;
        }

        public Long getAdmissionLettersGenerated() {
            return admissionLettersGenerated;
        }

        public void setAdmissionLettersGenerated(Long admissionLettersGenerated) {
            this.admissionLettersGenerated = admissionLettersGenerated;
        }
    }

    public static class UniversityCoordinatorSummary {
        private Long pendingAdmissions;
        private Long admittedStudents;

        public UniversityCoordinatorSummary() {
        }

        public UniversityCoordinatorSummary(Long pendingAdmissions, Long admittedStudents) {
            this.pendingAdmissions = pendingAdmissions;
            this.admittedStudents = admittedStudents;
        }

        public Long getPendingAdmissions() {
            return pendingAdmissions;
        }

        public void setPendingAdmissions(Long pendingAdmissions) {
            this.pendingAdmissions = pendingAdmissions;
        }

        public Long getAdmittedStudents() {
            return admittedStudents;
        }

        public void setAdmittedStudents(Long admittedStudents) {
            this.admittedStudents = admittedStudents;
        }
    }

    public static class DocPersonSummary {
        private Long pendingDocVerification;
        private Long docsReceived;
        private Long docsReturned;

        public DocPersonSummary() {
        }

        public DocPersonSummary(Long pendingDocVerification, Long docsReceived, Long docsReturned) {
            this.pendingDocVerification = pendingDocVerification;
            this.docsReceived = docsReceived;
            this.docsReturned = docsReturned;
        }

        public Long getPendingDocVerification() {
            return pendingDocVerification;
        }

        public void setPendingDocVerification(Long pendingDocVerification) {
            this.pendingDocVerification = pendingDocVerification;
        }

        public Long getDocsReceived() {
            return docsReceived;
        }

        public void setDocsReceived(Long docsReceived) {
            this.docsReceived = docsReceived;
        }

        public Long getDocsReturned() {
            return docsReturned;
        }

        public void setDocsReturned(Long docsReturned) {
            this.docsReturned = docsReturned;
        }
    }

    public static class AccountantSummary {
        private Long pendingPayments;
        private Long paymentsReceived;
        private Double totalAmountCollected;

        public AccountantSummary() {
        }

        public AccountantSummary(Long pendingPayments, Long paymentsReceived, Double totalAmountCollected) {
            this.pendingPayments = pendingPayments;
            this.paymentsReceived = paymentsReceived;
            this.totalAmountCollected = totalAmountCollected;
        }

        public Long getPendingPayments() {
            return pendingPayments;
        }

        public void setPendingPayments(Long pendingPayments) {
            this.pendingPayments = pendingPayments;
        }

        public Long getPaymentsReceived() {
            return paymentsReceived;
        }

        public void setPaymentsReceived(Long paymentsReceived) {
            this.paymentsReceived = paymentsReceived;
        }

        public Double getTotalAmountCollected() {
            return totalAmountCollected;
        }

        public void setTotalAmountCollected(Double totalAmountCollected) {
            this.totalAmountCollected = totalAmountCollected;
        }
    }

    public static class TelexPersonSummary {
        private Long pendingTelex;
        private Long submittedToUniversity;
        private Long submittedToMEA;
        private Long telexIssued;

        public TelexPersonSummary() {
        }

        public TelexPersonSummary(Long pendingTelex, Long submittedToUniversity, Long submittedToMEA,
                Long telexIssued) {
            this.pendingTelex = pendingTelex;
            this.submittedToUniversity = submittedToUniversity;
            this.submittedToMEA = submittedToMEA;
            this.telexIssued = telexIssued;
        }

        public Long getPendingTelex() {
            return pendingTelex;
        }

        public void setPendingTelex(Long pendingTelex) {
            this.pendingTelex = pendingTelex;
        }

        public Long getSubmittedToUniversity() {
            return submittedToUniversity;
        }

        public void setSubmittedToUniversity(Long submittedToUniversity) {
            this.submittedToUniversity = submittedToUniversity;
        }

        public Long getSubmittedToMEA() {
            return submittedToMEA;
        }

        public void setSubmittedToMEA(Long submittedToMEA) {
            this.submittedToMEA = submittedToMEA;
        }

        public Long getTelexIssued() {
            return telexIssued;
        }

        public void setTelexIssued(Long telexIssued) {
            this.telexIssued = telexIssued;
        }
    }

    public static class VisaPersonSummary {
        private Long pendingVisaFee;
        private Long passportSubmitted;
        private Long visaApplied;
        private Long visaStamped;

        public VisaPersonSummary() {
        }

        public VisaPersonSummary(Long pendingVisaFee, Long passportSubmitted, Long visaApplied, Long visaStamped) {
            this.pendingVisaFee = pendingVisaFee;
            this.passportSubmitted = passportSubmitted;
            this.visaApplied = visaApplied;
            this.visaStamped = visaStamped;
        }

        public Long getPendingVisaFee() {
            return pendingVisaFee;
        }

        public void setPendingVisaFee(Long pendingVisaFee) {
            this.pendingVisaFee = pendingVisaFee;
        }

        public Long getPassportSubmitted() {
            return passportSubmitted;
        }

        public void setPassportSubmitted(Long passportSubmitted) {
            this.passportSubmitted = passportSubmitted;
        }

        public Long getVisaApplied() {
            return visaApplied;
        }

        public void setVisaApplied(Long visaApplied) {
            this.visaApplied = visaApplied;
        }

        public Long getVisaStamped() {
            return visaStamped;
        }

        public void setVisaStamped(Long visaStamped) {
            this.visaStamped = visaStamped;
        }
    }
}
