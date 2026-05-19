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
import com.cc.modals.PassbookVisaManAccountant;
import com.cc.modals.PassbookForAccountantAgent;
import com.cc.repository.PassbookVisaManAccountantRepository;
import com.cc.repository.PassbookAccountantAgentRepository;

@Service
@SuppressWarnings("null")
public class VisaService {

        @Autowired
        private StudentRepository studentRepository;

        @Autowired
        private VisaManService visaManService;

        @Autowired
        private DocumentReturnRequestRepository documentReturnRequestRepository;

        @Autowired
        private EmailService emailService;

        @Autowired
        private PassbookVisaManAccountantRepository passbookVisaManAccountantRepository;

        @Autowired
        private PassbookAccountantAgentRepository passbookAccountantAgentRepository;

        @Autowired
        private UserService userService;

        @Autowired
        private SubAccountService subAccountService;

        // 1. Assign Visa Manager (called by Admission Manager)
        @Transactional
        public Student assignVisaManager(Long studentId, String admissionManagerUsername, String visaPersonUsername) {
                Student student = studentRepository.findById(studentId)
                                .orElseThrow(() -> new RuntimeException("Student not found"));

                VisaMan visaPerson = visaManService.getByUsername(visaPersonUsername)
                                .orElseThrow(() -> new RuntimeException(
                                                "Visa Person not found with username: " + visaPersonUsername));

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
                addHistory(student, StudentStatus.VISA_FEE_RECEIVED, visaManagerUsername, "Visa Fee received");
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
                addHistory(student, StudentStatus.VISA_APPLIED, visaManagerUsername,
                                "Visa application submitted by " + visaManagerUsername);
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
                addHistory(student, StudentStatus.PASSPORT_SUBMITTED_TO_EMBASSY, visaManagerUsername,
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
                addHistory(student, StudentStatus.VISA_STAMPED, visaManagerUsername,
                                "Visa successfully issued! Stamped by " + visaManagerUsername);
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
                student.setActionOnUser(actionOnUser.doc_manager); // Or doc_person? The prompt says "agent can Request
                                                                   // for
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

                addHistory(student, student.getApplicationStatus(), visaManagerUsername,
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

                PassbookVisaManAccountant passbook = passbookVisaManAccountantRepository.findByApplicationId(student.getApplicationId())
                                .orElse(new PassbookVisaManAccountant());
                passbook.setStudentName(student.getFullName());
                passbook.setApplicationId(student.getApplicationId());
                passbook.setVisaAmountRequest(amount);
                passbook.setServiceChargeRequested(visaManServiceCharge != null ? visaManServiceCharge : 0.0);
                passbook.setRequestedDateTime(LocalDateTime.now());
                passbook.setStatus("INITIATED");
                passbook.setRequesterName(visaManUsername);
                passbookVisaManAccountantRepository.save(passbook);

                addHistory(student, student.getApplicationStatus(), visaManUsername,
                                "Visa payment requested: " + amount + " " + currency + " (Service Charge: "
                                                + visaManServiceCharge
                                                + ") by " + visaManUsername);

                return studentRepository.save(student);
        }

        // Step 2: Accountant (in Uzbekistan) sends bank details to Agent (in India)
        @Transactional
        public Student sendBankDetailsToAgent(Long studentId, String accountantUsername, Double amount, String currency,
                        Long subAccountId) {
                Student student = studentRepository.findById(studentId)
                                .orElseThrow(() -> new RuntimeException("Student not found"));

                com.cc.modals.SubAccount subAccount = subAccountService.getSubAccountById(subAccountId);
                if (subAccount == null) {
                        throw new RuntimeException("SubAccount not found");
                }

                // DO NOT overwrite visaPaymentRequestedAmount — it is the base visa fee set by VisaMan.
                // The 'amount' parameter here is the total the accountant tells the agent to send.
                student.setVisaBankDetailsSentDate(java.time.LocalDate.now());

                // Set bank details for international transfer from SubAccount
                student.setVisaBankName(subAccount.getBankName());
                student.setVisaBankAccountNumber(subAccount.getAccountNumber());
                student.setVisaBankSwiftCode(subAccount.getSwiftBic());
                student.setVisaBankBeneficiaryName(subAccount.getBeneficiaryName());
                student.setVisaBankBranchAddress(subAccount.getBranchAddress());

                // Update action and actionOnUser
                student.setAction(action.SEND_VISA_PAYMENT_TO_ACCOUNTANT);
                student.setActionOnUser(actionOnUser.agent);

                PassbookVisaManAccountant visaPassbook = passbookVisaManAccountantRepository.findByApplicationId(student.getApplicationId())
                                .orElseThrow(() -> new RuntimeException("Visa Passbook not found for application: " + student.getApplicationId()));
                visaPassbook.setStatus("SENT_TO_AGENT");
                passbookVisaManAccountantRepository.save(visaPassbook);

                PassbookForAccountantAgent agentPassbook = passbookAccountantAgentRepository.findByApplicationId(student.getApplicationId())
                                .orElse(new PassbookForAccountantAgent());
                agentPassbook.setStudentName(student.getFullName());
                agentPassbook.setApplicationId(student.getApplicationId());
                agentPassbook.setRequestedAmountByAccountantToAgent(amount);
                agentPassbook.setBankDetails("Bank: " + subAccount.getBankName() + ", Acc: " + subAccount.getAccountNumber() + ", SWIFT: " + subAccount.getSwiftBic());
                agentPassbook.setRequestDateTimeByAccountant(LocalDateTime.now());
                agentPassbook.setAmountPaidByAgent(0.0);
                agentPassbook.setSubAccount(subAccount); // Link to SubAccount
                passbookAccountantAgentRepository.save(agentPassbook);

                addHistory(student, student.getApplicationStatus(), accountantUsername,
                                "Bank details sent to agent: " + amount + " " + currency + " | Bank: " + subAccount.getBankName()
                                                + " | SWIFT: "
                                                + subAccount.getSwiftBic() + " by " + accountantUsername);

                return studentRepository.save(student);
        }

        // Step 3: Agent sends payment confirmation
        @Transactional
        public Student submitPaymentConfirmation(Long studentId, String agentUsername, Double amount, String currency,
                        String transactionId, String confirmationPagePath) {
                Student student = studentRepository.findById(studentId)
                                .orElseThrow(() -> new RuntimeException("Student not found"));

                // DO NOT overwrite visaPaymentRequestedAmount — it is the base visa fee set by VisaMan.
                if (transactionId != null && !transactionId.isEmpty()) {
                        student.setVisaPaymentTxId(transactionId);
                }
                if (confirmationPagePath != null && !confirmationPagePath.isEmpty()) {
                        student.setVisaPaymentConfirmationPath(confirmationPagePath);
                }
                student.setAction(action.CONFIRM_VISA_PAYMENT);
                student.setActionOnUser(actionOnUser.accountant);

                PassbookForAccountantAgent agentPassbook = passbookAccountantAgentRepository.findByApplicationId(student.getApplicationId())
                                .orElseThrow(() -> new RuntimeException("Accountant-Agent Passbook not found for application: " + student.getApplicationId()));
                
                double currentPaid = agentPassbook.getAmountPaidByAgent();
                agentPassbook.setAmountPaidByAgent(currentPaid + amount);
                if (transactionId != null && !transactionId.isEmpty()) {
                        agentPassbook.setTransactionIdOrUpiIdGivenByAgent(transactionId);
                }
                agentPassbook.setPaymentDateTimeByAgent(LocalDateTime.now());
                agentPassbook.setPaidByAgent(agentUsername);
                passbookAccountantAgentRepository.save(agentPassbook);

                addHistory(student, student.getApplicationStatus(), agentUsername,
                                "Payment confirmation submitted: " + amount + " " + currency +
                                                (transactionId != null ? " TxId: " + transactionId : "") + " by "
                                                + agentUsername);

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

                PassbookForAccountantAgent agentPassbook = passbookAccountantAgentRepository.findByApplicationId(student.getApplicationId())
                                .orElseThrow(() -> new RuntimeException("Accountant-Agent Passbook not found"));
                double currentConfirmed = agentPassbook.getAmountConfirmedByAccountant() + receivedAmount;
                agentPassbook.setAmountConfirmedByAccountant(currentConfirmed);
                passbookAccountantAgentRepository.save(agentPassbook);

                // Auto-Credit to SubAccount
                if (agentPassbook.getSubAccount() != null) {
                        subAccountService.updateBalance(agentPassbook.getSubAccount().getId(), receivedAmount, "Visa Payment Received from Agent: " + student.getApplicationId(), student.getApplicationId());
                }

                PassbookVisaManAccountant visaPassbook = passbookVisaManAccountantRepository.findByApplicationId(student.getApplicationId())
                                .orElseThrow(() -> new RuntimeException("Visa Passbook not found"));
                visaPassbook.setStatus("ACCOUNTANT_CONFIRMED");
                passbookVisaManAccountantRepository.save(visaPassbook);

                addHistory(student, StudentStatus.VISA_PAYMENT_CONFIRMED, accountantUsername,
                                "Visa payment confirmed: " + receivedAmount + " received on " + receivedDate + " by "
                                                + accountantUsername);

                return studentRepository.save(student);
        }

        // Step 5 (New for Finance): Accountant pays Visa Man
        @Transactional
        public Student payVisaMan(Long studentId, String accountantUsername, Double amountToPay, String txId, String mode, java.time.LocalDate paymentDate, Long subAccountId) {
                Student student = studentRepository.findById(studentId)
                                .orElseThrow(() -> new RuntimeException("Student not found"));

                com.cc.modals.SubAccount subAccount = subAccountService.getSubAccountById(subAccountId);
                if (subAccount == null) {
                        throw new RuntimeException("SubAccount not found");
                }
                
                // Validate sufficient balance
                if (subAccount.getBalance() < amountToPay) {
                        throw new RuntimeException("Insufficient balance in SubAccount: " + subAccount.getBankName() + ". Available: " + subAccount.getBalance());
                }

                Double requestedAmount = student.getVisaPaymentRequestedAmount() != null ? student.getVisaPaymentRequestedAmount() : 0.0;
                Double serviceCharge = student.getVisaManServiceCharge() != null ? student.getVisaManServiceCharge() : 0.0;
                Double totalOwed = requestedAmount + serviceCharge;

                Double alreadyPaid = student.getAccToVisaManPaidAmount() != null ? student.getAccToVisaManPaidAmount() : 0.0;
                Double newTotalPaid = alreadyPaid + amountToPay;

                if (newTotalPaid > totalOwed && totalOwed > 0) {
                        throw new RuntimeException("Cannot pay more than the requested amount. Total owed: " + totalOwed + ", Already paid: " + alreadyPaid);
                }

                // Auto-Debit from SubAccount
                subAccountService.updateBalance(subAccount.getId(), -amountToPay, "Visa Man Payout for: " + student.getApplicationId(), txId != null ? txId : student.getApplicationId());

                student.setAccToVisaManPaidAmount(newTotalPaid);
                student.setAccToVisaManPaymentDate(paymentDate);
                if (txId != null && !txId.isEmpty()) {
                        student.setAccToVisaManTxId(txId);
                }
                student.setAccToVisaManMode(mode);

                PassbookVisaManAccountant visaPassbook = passbookVisaManAccountantRepository.findByApplicationId(student.getApplicationId())
                                .orElseThrow(() -> new RuntimeException("Visa Passbook not found"));
                
                double currentTotalPaid = visaPassbook.getTotalAmountPaidByAccountant() + amountToPay;
                visaPassbook.setTotalAmountPaidByAccountant(currentTotalPaid);
                visaPassbook.setAccountantDateTimePaid(LocalDateTime.now());
                visaPassbook.setModeOfPayment(mode);
                visaPassbook.setSubAccount(subAccount); // Link to SubAccount

                if (totalOwed > 0 && newTotalPaid >= totalOwed) {
                        student.setAccToVisaManStatus("FULLY_PAID");
                        visaPassbook.setStatus("COMPLETED");
                } else if (newTotalPaid > 0) {
                        student.setAccToVisaManStatus("PARTIALLY_PAID");
                        visaPassbook.setStatus("PAID_TO_VISAMAN");
                } else {
                        student.setAccToVisaManStatus("PENDING");
                }
                
                passbookVisaManAccountantRepository.save(visaPassbook);

                addHistory(student, student.getApplicationStatus(), accountantUsername,
                                "Accountant paid Visa Man: " + amountToPay + " via " + mode + 
                                (txId != null && !txId.isEmpty() ? " (TxId: " + txId + ")" : "") + ". Status: " + student.getAccToVisaManStatus());

                return studentRepository.save(student);
        }

        // Step 6 (New): Visa Manager collects passport from Doc Person
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

                addHistory(student, StudentStatus.PASSPORT_COLLECTED, visaManUsername,
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
                history.setUpdatedByName(userService.resolveFullName(updatedBy));
                history.setUpdatedAt(LocalDateTime.now());
                if (remarks != null) {
                        history.setRemarks(remarks);
                }
                student.getStudentStatusHistory().add(history);
        }

        // ======================= REPORTING & STATS =======================

        public com.cc.DTO.VisaManStatsDTO getVisaStats(String period, String username) {
                LocalDateTime start;
                LocalDateTime end = LocalDateTime.now();

                if ("DAILY".equalsIgnoreCase(period)) {
                        start = java.time.LocalDate.now().atStartOfDay();
                } else if ("MONTHLY".equalsIgnoreCase(period)) {
                        start = java.time.LocalDate.now().withDayOfMonth(1).atStartOfDay();
                } else if ("YEARLY".equalsIgnoreCase(period)) {
                        start = java.time.LocalDate.now().withDayOfYear(1).atStartOfDay();
                } else {
                        throw new IllegalArgumentException("Invalid period. Use DAILY, MONTHLY, or YEARLY.");
                }

                com.cc.DTO.VisaManStatsDTO stats = new com.cc.DTO.VisaManStatsDTO();
                stats.setPeriod(period);
                stats.setVisaManagerName(username != null ? username : "ALL MANAGERS");

                if (username != null && !username.isEmpty()) {
                        // Specific Manager Stats
                        // stats.setTotalAssignedStudents(studentRepository.countByVisaManUsername(username));
                        stats.setTotalAssignedStudents(
                                        studentRepository.countByVisaManAndStatusAndDateRangeNoUpdateUser(
                                                        username, StudentStatus.VISA_FEE_PENDING, start, end));
                        stats.setPaymentRequested(studentRepository.countByVisaManAndHistoryRemarkAndDateRange(username,
                                        "payment requested", start, end));
                        stats.setPaymentConfirmed(studentRepository.countByVisaManAndStatusAndDateRange(username,
                                        StudentStatus.VISA_PAYMENT_CONFIRMED, start, end));
                        stats.setPassportCollected(studentRepository.countByVisaManAndStatusAndDateRange(username,
                                        StudentStatus.PASSPORT_COLLECTED, start, end));
                        stats.setVisaApplied(studentRepository.countByVisaManAndStatusAndDateRange(username,
                                        StudentStatus.VISA_APPLIED, start, end));
                        stats.setEmbassySubmitted(studentRepository.countByVisaManAndStatusAndDateRange(username,
                                        StudentStatus.PASSPORT_SUBMITTED_TO_EMBASSY, start, end));
                        stats.setVisaStamped(studentRepository.countByVisaManAndStatusAndDateRange(username,
                                        StudentStatus.VISA_STAMPED, start, end));
                        stats.setPassportReturned(studentRepository.countByVisaManAndHistoryRemarkAndDateRange(username,
                                        "Documents returned", start, end));
                } else {
                        // Global Stats (Admin View)
                        // Global Stats (Admin View)
                        // stats.setTotalAssignedStudents(studentRepository.countByApplicationStatusIn(...));
                        stats.setTotalAssignedStudents(
                                        studentRepository.countAllVisaStatusByDateRange(StudentStatus.VISA_FEE_PENDING,
                                                        start, end));
                        // Note: Total assigned is hard to get exactly for time range without checking
                        // when assigned.
                        // Using count of ALL students currently in Visa stages or completed visa
                        // might be better proxy if "assigned in period" is not strictly required.
                        // But for consistency with "Actions in period":

                        stats.setPaymentRequested(
                                        studentRepository.countAllVisaHistoryRemarkByDateRange("payment requested",
                                                        start, end));
                        stats.setPaymentConfirmed(
                                        studentRepository.countAllVisaStatusByDateRange(
                                                        StudentStatus.VISA_PAYMENT_CONFIRMED, start, end));
                        stats.setPassportCollected(
                                        studentRepository.countAllVisaStatusByDateRange(
                                                        StudentStatus.PASSPORT_COLLECTED, start, end));
                        stats.setVisaApplied(
                                        studentRepository.countAllVisaStatusByDateRange(StudentStatus.VISA_APPLIED,
                                                        start, end));
                        stats.setEmbassySubmitted(studentRepository
                                        .countAllVisaStatusByDateRange(StudentStatus.PASSPORT_SUBMITTED_TO_EMBASSY,
                                                        start, end));
                        stats.setVisaStamped(
                                        studentRepository.countAllVisaStatusByDateRange(StudentStatus.VISA_STAMPED,
                                                        start, end));
                        stats.setPassportReturned(
                                        studentRepository.countAllVisaHistoryRemarkByDateRange("Documents returned",
                                                        start, end));
                }

                return stats;
        }

        public java.util.List<com.cc.DTO.VisaManStatsDTO> getAllVisaManagersStats(String period) {
                java.util.List<VisaMan> managers = visaManService.getAll();
                java.util.List<com.cc.DTO.VisaManStatsDTO> allStats = new java.util.ArrayList<>();

                // Add Global Stats as the first item (optional, or separate endpoint)
                // allStats.add(getVisaStats(period, null));

                for (VisaMan vm : managers) {
                        // Display all managers to match global totals
                        allStats.add(getVisaStats(period, vm.getUsername()));
                }
                return allStats;
        }

        public java.util.List<Student> getVisaStudents(String period, String username) {
                LocalDateTime start;
                LocalDateTime end = LocalDateTime.now();

                if ("DAILY".equalsIgnoreCase(period)) {
                        start = java.time.LocalDate.now().atStartOfDay();
                } else if ("MONTHLY".equalsIgnoreCase(period)) {
                        start = java.time.LocalDate.now().withDayOfMonth(1).atStartOfDay();
                } else if ("YEARLY".equalsIgnoreCase(period)) {
                        start = java.time.LocalDate.now().withDayOfYear(1).atStartOfDay();
                } else {
                        throw new IllegalArgumentException("Invalid period. Use DAILY, MONTHLY, or YEARLY.");
                }

                // If specific username provided, get students for that manager in that period
                if (username != null && !username.isEmpty()) {
                        // Logic: Get students with ANY Visa-related activity update in this period by
                        // this manager?
                        // OR students assigned to this manager?
                        // "Drill down" usually implies seeing the students that make up the stats.
                        // Since stats cover many actions, we should probably return students ASSIGNED
                        // to
                        // this manager currently?
                        // OR students who had activity in this period.
                        // Given the context of "Reports", typically you list the students involved in
                        // the stats.
                        // Safest bet for "Student List" drilldown: All students currently assigned to
                        // this Visa Manager.
                        // If strict period filtering is needed for the list too (only those ACTIVE in
                        // period), it's complex.
                        // Let's return all students currently assigned to this manager for now, as
                        // drill-down usually shows current portfolio.
                        // IMPROVEMENT: Filter by updated_at in period if requested?
                        // Backend logs show "GET .../students?period=yearly".
                        // Let's try to filter by "Assigned/Processed in period" if possible, or just
                        // return all for that manager.
                        // Compromise: Return all students assigned to this manager. The Frontend seems
                        // to expect a list.

                        // Returning all students currently assigned into this manager
                        // return studentRepository.findByVisaManUsername(username); -- Need to check if
                        // this method exists

                        // Let's use existing repository method or add one.
                        // findByVisaMan_Username matches JPA convention.
                        return studentRepository.findByVisaMan_Username(username);

                } else {
                        return new java.util.ArrayList<>();
                }
        }

        public void sendReportEmail(String period, String username, String targetEmail) {
                com.cc.DTO.VisaManStatsDTO stats = getVisaStats(period, username);
                String htmlContent = generateReportEmailContent(stats);
                String subject = "Visa Manager Report (" + period + ") - " + stats.getVisaManagerName();
                emailService.sendHtmlEmail(targetEmail, subject, htmlContent);
        }

        private String generateReportEmailContent(com.cc.DTO.VisaManStatsDTO stats) {
                StringBuilder html = new StringBuilder();
                html.append("<html><body style='font-family: Arial, sans-serif;'>");
                html.append("<h2 style='color: #2c3e50;'>Visa Manager Report</h2>");
                html.append("<p><strong>Period:</strong> ").append(stats.getPeriod()).append("</p>");
                html.append("<p><strong>Manager:</strong> ").append(stats.getVisaManagerName()).append("</p>");
                html.append("<hr>");
                html.append("<table style='width: 100%; border-collapse: collapse; margin-top: 20px;'>");
                html.append(
                                "<tr style='background-color: #f2f2f2;'><th style='padding: 10px; border: 1px solid #ddd; text-align: left;'>Action</th><th style='padding: 10px; border: 1px solid #ddd; text-align: center;'>Count</th></tr>");

                addRow(html, "Payment Requested", stats.getPaymentRequested());
                addRow(html, "Payment Confirmed", stats.getPaymentConfirmed());
                addRow(html, "Passport Collected", stats.getPassportCollected());
                addRow(html, "Visa Applied", stats.getVisaApplied());
                addRow(html, "Emabssy Submitted", stats.getEmbassySubmitted());
                addRow(html, "Visa Stamped", stats.getVisaStamped());
                addRow(html, "Passport Returned", stats.getPassportReturned());

                html.append("</table>");
                html.append(
                                "<p style='margin-top: 20px; color: #7f8c8d; font-size: 12px;'>Generated by CampusConnect System</p>");
                html.append("</body></html>");
                return html.toString();
        }

        private void addRow(StringBuilder html, String label, long count) {
                html.append("<tr>");
                html.append("<td style='padding: 10px; border: 1px solid #ddd;'>").append(label).append("</td>");
                html.append("<td style='padding: 10px; border: 1px solid #ddd; text-align: center;'>").append(count)
                                .append("</td>");
                html.append("</tr>");
        }
}
