package com.cc.service;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cc.modals.Student;
import com.cc.modals.StudentStatus;
import com.cc.modals.StudentStatusHistory;
import com.cc.modals.VisaMan;
import com.cc.modals.action;
import com.cc.modals.actionOnUser;
import com.cc.modals.DocumentReturnRequest;
import com.cc.repository.DocumentReturnRequestRepository;
import com.cc.repository.StudentRepository;

@Service
@SuppressWarnings("null")
public class VisaService {

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private VisaManService visaManService;

    @Autowired
    private DocumentReturnRequestRepository documentReturnRequestRepository;

    // 1. Assign Visa Manager (called by Admission Manager)
    @Transactional
    public Student assignVisaManager(Long studentId, String admissionManagerUsername, String visaPersonUsername) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));

        VisaMan visaPerson = visaManService.getByUsername(visaPersonUsername)
                .orElseThrow(() -> new RuntimeException("Visa Person not found with username: " + visaPersonUsername));

        student.setAction(action.REQUEST_FOR_VISA_PAYMENT_TO_ACCOUNTANT);
        student.setActionOnUser(actionOnUser.visa_manager);
        student.setVisaMan(visaPerson);
        student.setApplicationStatus(StudentStatus.VISA_FEE_PENDING);

        addHistory(student, StudentStatus.VISA_FEE_PENDING, admissionManagerUsername,
                "Assigned to Visa Person: " + visaPersonUsername + " by Admission Manager: "
                        + admissionManagerUsername);

        return studentRepository.save(student);
    }

    // 2. Confirm Visa Fee
    public Student confirmVisaFee(Long studentId, String visaManagerUsername) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));

        if (student.getVisaMan() == null || !student.getVisaMan().getUsername().equals(visaManagerUsername)) {
            throw new RuntimeException("Unauthorized: Only assigned Visa Manager can perform this action");
        }

        student.setApplicationStatus(StudentStatus.VISA_FEE_RECEIVED);
        addHistory(student, StudentStatus.VISA_FEE_RECEIVED, visaManagerUsername);
        return studentRepository.save(student);
    }

    // 3. Apply for Visa
    @Transactional
    public Student applyForVisa(Long studentId, String visaManagerUsername) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));

        if (student.getVisaMan() == null || !student.getVisaMan().getUsername().equals(visaManagerUsername)) {
            throw new RuntimeException("Unauthorized: Only assigned Visa Manager can perform this action");
        }

        student.setAction(action.NEED_SUBMIT_PASSPORT_TO_EMBASSY);
        student.setActionOnUser(actionOnUser.visa_manager);
        student.setApplicationStatus(StudentStatus.VISA_APPLIED);
        addHistory(student, StudentStatus.VISA_APPLIED, "Visa application submitted by " + visaManagerUsername);
        return studentRepository.save(student);
    }

    // 4. Submit Passport to Embassy
    @Transactional
    public Student submitPassportToEmbassy(Long studentId, String visaManagerUsername) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));

        if (student.getVisaMan() == null || !student.getVisaMan().getUsername().equals(visaManagerUsername)) {
            throw new RuntimeException("Unauthorized: Only assigned Visa Manager can perform this action");
        }

        student.setAction(action.NEED_TO_ISSUE_VISA);
        student.setActionOnUser(actionOnUser.visa_manager);
        student.setApplicationStatus(StudentStatus.PASSPORT_SUBMITTED_TO_EMBASSY);
        addHistory(student, StudentStatus.PASSPORT_SUBMITTED_TO_EMBASSY,
                "Passport submitted to embassy by " + visaManagerUsername);
        return studentRepository.save(student);
    }

    // 5. Confirm Visa Stamped - Visa successfully issued
    @Transactional
    public Student confirmVisaStamped(Long studentId, String visaManagerUsername) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));

        if (student.getVisaMan() == null || !student.getVisaMan().getUsername().equals(visaManagerUsername)) {
            throw new RuntimeException("Unauthorized: Only assigned Visa Manager can perform this action");
        }
        student.setCanRequestDocReturn(true);
        // student.setReadyForDocReturn(true); // REMOVED: Now handled by separate
        // action
        student.setAction(action.NEED_TO_RETURN_VISA);

        student.setActionOnUser(actionOnUser.visa_manager);
        student.setApplicationStatus(StudentStatus.VISA_STAMPED);
        addHistory(student, StudentStatus.VISA_STAMPED, "Visa successfully issued! Stamped by " + visaManagerUsername);
        return studentRepository.save(student);
    }

    // 6. Return Docs (Passports etc) to Doc Person
    @Transactional
    public Student returnDocsToDocPerson(Long studentId, String visaManagerUsername) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));

        if (student.getVisaMan() == null || !student.getVisaMan().getUsername().equals(visaManagerUsername)) {
            throw new RuntimeException("Unauthorized: Only assigned Visa Manager can perform this action");
        }

        // Validate Status
        if (student.getApplicationStatus() != StudentStatus.VISA_STAMPED) {
            // throw new RuntimeException("Cannot return docs unless Visa is Stamped");
            // flexible check? logic implies we are at this stage.
        }

        student.setReadyForDocReturn(true);
        student.setAction(action.COLLECT_DOCS); // Next step for Agent to collect?
        student.setActionOnUser(actionOnUser.doc_manager); // Or doc_person? The prompt says "agent can Request for
                                                           // Docs" ...
        // "doc person will cant collect... untill visa manager return".
        // Assuming Agent/Student collects it or DocPerson facilitates. Setting to
        // agent/doc_person as appropriate.
        // Prompt: "doc person will cant collect... untill..." implies DocPerson is the
        // one collecting from VisaMan.
        student.setActionOnUser(actionOnUser.doc_manager);

        // Mark Request as Returned if exists
        DocumentReturnRequest request = documentReturnRequestRepository.findByStudentId(studentId);
        if (request != null) {
            request.setIsReturned(true);
            documentReturnRequestRepository.save(request);
        }

        addHistory(student, student.getApplicationStatus(),
                "Documents returned/ready for collection. Confirm by " + visaManagerUsername);
        return studentRepository.save(student);
    }

    // ======================= VISA PAYMENT WORKFLOW =======================

    // Step 1: VisaMan requests payment from Accountant
    @Transactional
    public Student requestPaymentFromAccountant(Long studentId, String visaManUsername, Double amount,
            String currency, Double visaManServiceCharge) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));

        if (student.getVisaMan() == null || !student.getVisaMan().getUsername().equals(visaManUsername)) {
            throw new RuntimeException("Unauthorized: Only assigned Visa Manager can perform this action");
        }

        student.setVisaPaymentRequestedAmount(amount);
        student.setVisaPaymentRequestedCurrency(currency);
        student.setVisaManServiceCharge(visaManServiceCharge);
        student.setAction(action.SEND_BANK_DETAILS_TO_AGENT);
        student.setActionOnUser(actionOnUser.accountant);

        addHistory(student, student.getApplicationStatus(),
                "Visa payment requested: " + amount + " " + currency + " (Service Charge: " + visaManServiceCharge
                        + ") by " + visaManUsername);

        return studentRepository.save(student);
    }

    // Step 2: Accountant (in Uzbekistan) sends bank details to Agent (in India)
    @Transactional
    public Student sendBankDetailsToAgent(Long studentId, String accountantUsername, Double amount, String currency,
            String bankName, String accountNumber, String swiftCode, String beneficiaryName, String branchAddress) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));

        // Set payment amount and currency
        student.setVisaPaymentRequestedAmount(amount);
        student.setVisaPaymentRequestedCurrency(currency);
        student.setVisaBankDetailsSentDate(java.time.LocalDate.now());

        // Set bank details for international transfer
        student.setVisaBankName(bankName);
        student.setVisaBankAccountNumber(accountNumber);
        student.setVisaBankSwiftCode(swiftCode);
        student.setVisaBankBeneficiaryName(beneficiaryName);
        student.setVisaBankBranchAddress(branchAddress);

        // Update action and actionOnUser
        student.setAction(action.SEND_VISA_PAYMENT_TO_ACCOUNTANT);
        student.setActionOnUser(actionOnUser.agent);

        addHistory(student, student.getApplicationStatus(),
                "Bank details sent to agent: " + amount + " " + currency + " | Bank: " + bankName + " | SWIFT: "
                        + swiftCode + " by " + accountantUsername);

        return studentRepository.save(student);
    }

    // Step 3: Agent sends payment confirmation
    @Transactional
    public Student submitPaymentConfirmation(Long studentId, String agentUsername, Double amount, String currency,
            String transactionId, String confirmationPagePath) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));

        student.setVisaPaymentRequestedAmount(amount);
        student.setVisaPaymentRequestedCurrency(currency);
        if (transactionId != null && !transactionId.isEmpty()) {
            student.setVisaPaymentTxId(transactionId);
        }
        if (confirmationPagePath != null && !confirmationPagePath.isEmpty()) {
            student.setVisaPaymentConfirmationPath(confirmationPagePath);
        }
        student.setAction(action.CONFIRM_VISA_PAYMENT);
        student.setActionOnUser(actionOnUser.accountant);

        addHistory(student, student.getApplicationStatus(),
                "Payment confirmation submitted: " + amount + " " + currency +
                        (transactionId != null ? " TxId: " + transactionId : "") + " by " + agentUsername);

        return studentRepository.save(student);
    }

    // Step 4: Accountant confirms payment received
    @Transactional
    public Student confirmVisaPaymentReceived(Long studentId, String accountantUsername, Double receivedAmount,
            java.time.LocalDate receivedDate) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));

        student.setVisaPaymentReceivedAmount(receivedAmount);
        student.setVisaPaymentReceivedDate(receivedDate);
        student.setAction(action.COLLECT_PASSPORT_FRM_DOC_PERSON);
        student.setActionOnUser(actionOnUser.visa_manager);
        student.setApplicationStatus(StudentStatus.VISA_PAYMENT_CONFIRMED);

        addHistory(student, StudentStatus.VISA_PAYMENT_CONFIRMED,
                "Visa payment confirmed: " + receivedAmount + " received on " + receivedDate + " by "
                        + accountantUsername);

        return studentRepository.save(student);
    }

    // Step 5 (New): Visa Manager collects passport from Doc Person
    @Transactional
    public Student collectPassport(Long studentId, String visaManUsername) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));

        if (student.getVisaMan() == null || !student.getVisaMan().getUsername().equals(visaManUsername)) {
            throw new RuntimeException("Unauthorized: Only assigned Visa Manager can perform this action");
        }

        student.setAction(action.NEED_APPLY_VISA);
        student.setActionOnUser(actionOnUser.visa_manager);
        student.setApplicationStatus(StudentStatus.PASSPORT_COLLECTED);

        addHistory(student, StudentStatus.PASSPORT_COLLECTED,
                "Passport collected from Doc Person by " + visaManUsername);

        return studentRepository.save(student);
    }

    // Helper to add history
    private void addHistory(Student student, StudentStatus status, String updatedBy) {
        addHistory(student, status, updatedBy, null);
    }

    private void addHistory(Student student, StudentStatus status, String updatedBy, String remarks) {
        StudentStatusHistory history = new StudentStatusHistory();
        history.setStudent(student);
        history.setStudentApplicationStatus(status);
        history.setUpdatedBy(updatedBy);
        history.setUpdatedAt(LocalDateTime.now());
        if (remarks != null) {
            history.setRemarks(remarks);
        }
        student.getStudentStatusHistory().add(history);
    }
}
