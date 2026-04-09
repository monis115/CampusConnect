package com.cc.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.cc.modals.Accountant;
import com.cc.modals.action;
import com.cc.modals.actionOnUser;
import com.cc.repository.AccountantRepository;
import java.util.List;
import java.util.Optional;

@Service
@SuppressWarnings("null")
public class AccountantService {

        @Autowired
        private AccountantRepository repository;

        @Autowired
        private UserService userService;

        public Accountant create(Accountant entity) {
                Accountant savedEntity = repository.save(entity);

                com.cc.modals.User user = new com.cc.modals.User();
                user.setUsername(savedEntity.getUsername());
                user.setPassword(savedEntity.getPassword());
                user.setRole("accountant");
                userService.createUser(user);

                return savedEntity;
        }

        public List<Accountant> getAll() {
                return repository.findAll();
        }

        public Optional<Accountant> getById(Long id) {
                return repository.findById(id);
        }

        public Optional<Accountant> getByUsername(String username) {
                return repository.findByUsername(username);
        }

        public Optional<Accountant> getOne() {
                return repository.findAll().stream()
                                .filter(a -> "Active".equalsIgnoreCase(a.getStatus()))
                                .findFirst();
        }

        public java.util.Map<String, Object> checkAvailability() {
                List<Accountant> allAccountants = repository.findAll();

                if (allAccountants.isEmpty()) {
                        return java.util.Map.of(
                                        "canCreate", true,
                                        "message", "No Accountant exists. You can create one.",
                                        "existingAccountants", java.util.Collections.emptyList());
                }

                boolean hasActiveAccountant = allAccountants.stream()
                                .anyMatch(a -> "Active".equalsIgnoreCase(a.getStatus()));

                List<java.util.Map<String, Object>> existingList = allAccountants.stream()
                                .map(a -> java.util.Map.<String, Object>of(
                                                "id", a.getId(),
                                                "username", a.getUsername(),
                                                "fullName", a.getFullName(),
                                                "status", a.getStatus()))
                                .collect(java.util.stream.Collectors.toList());

                if (hasActiveAccountant) {
                        return java.util.Map.of(
                                        "canCreate", false,
                                        "message", "An active Accountant already exists.",
                                        "existingAccountants", existingList);
                } else {
                        return java.util.Map.of(
                                        "canCreate", true,
                                        "message", "All existing Accountants are inactive. You can create a new one.",
                                        "existingAccountants", existingList);
                }
        }

        public Accountant update(Long id, Accountant entity) {
                return repository.findById(id).map(existing -> {
                        if (entity.getUsername() != null)
                                existing.setUsername(entity.getUsername());
                        if (entity.getFullName() != null)
                                existing.setFullName(entity.getFullName());
                        if (entity.getEmail() != null)
                                existing.setEmail(entity.getEmail());
                        if (entity.getContactNumber() != null)
                                existing.setContactNumber(entity.getContactNumber());
                        if (entity.getStatus() != null)
                                existing.setStatus(entity.getStatus());
                        // Do not update password here to prevent overwriting with null
                        // If password update is needed, it should be a separate secure endpoint or
                        // handled carefully

                        return repository.save(existing);
                }).orElse(null);
        }

        @Autowired
        private com.cc.repository.StudentRepository studentRepository;

        @Autowired
        private com.cc.repository.StudentStatusHistoryRepository studentStatusHistoryRepository;

        public void delete(Long id) {
                repository.deleteById(id);
        }

        public com.cc.modals.Student updateStudentPaymentStatus(Long studentId, String paymentStatus,
                        String accountantUsername) {
                com.cc.modals.Student student = studentRepository.findById(studentId)
                                .orElseThrow(() -> new RuntimeException("Student not found with ID: " + studentId));

                student.setPaymentStatus(paymentStatus);
                student.setApplicationStatus(com.cc.modals.StudentStatus.PAYMENT_APPROVED);
                student.setUpdatedBy(accountantUsername);

                com.cc.modals.Student savedStudent = studentRepository.save(student);

                // Create status history entry
                com.cc.modals.StudentStatusHistory historyEntry = new com.cc.modals.StudentStatusHistory();
                historyEntry.setStudent(savedStudent);
                historyEntry.setStudentApplicationStatus(com.cc.modals.StudentStatus.PAYMENT_APPROVED);
                historyEntry.setUpdatedBy(accountantUsername);
                historyEntry.setUpdatedAt(java.time.LocalDateTime.now());
                studentStatusHistoryRepository.save(historyEntry);

                return savedStudent;
        }

        @org.springframework.transaction.annotation.Transactional
        public com.cc.modals.Student approveTelexPayment(Long studentId, Double receivedAmount,
                        java.time.LocalDate receivedDate, String accountantUsername) {
                com.cc.modals.Student student = studentRepository.findById(studentId)
                                .orElseThrow(() -> new RuntimeException("Student not found with ID: " + studentId));

                // Prevent duplicate action
                String currentPaymentStatus = student.getPaymentStatus();
                if ("Received".equals(currentPaymentStatus) || "Not Received".equals(currentPaymentStatus)) {
                        throw new RuntimeException(
                                        "Payment action already taken for this student. Current status: "
                                                        + currentPaymentStatus);
                }

                student.setTelexPaymentReceivedAmount(receivedAmount);
                student.setTelexPaymentReceivedDate(receivedDate);
                student.setIsTelexAccountantApproved(true);
                student.setPaymentStatus("Received");

                // Always create history entry when accountant approves payment
                com.cc.modals.StudentStatusHistory paymentHistory = new com.cc.modals.StudentStatusHistory();
                paymentHistory.setStudent(student);
                paymentHistory.setStudentApplicationStatus(com.cc.modals.StudentStatus.FEE_RECEIVED);
                paymentHistory.setUpdatedBy(accountantUsername);
                paymentHistory.setUpdatedAt(java.time.LocalDateTime.now());
                paymentHistory.setRemarks("Payment received: " + receivedAmount + " on " + receivedDate);
                studentStatusHistoryRepository.save(paymentHistory);

                // Check if Doc Person also approved - if yes, transition to TELEX_INITIATED
                // if (Boolean.TRUE.equals(student.getIsTelexDocApproved())) {
                // student.setApplicationStatus(com.cc.modals.StudentStatus.TELEX_INITIATED);

                // // History for Telex Initiated (both approvals complete)
                // com.cc.modals.StudentStatusHistory initiatedHistory = new
                // com.cc.modals.StudentStatusHistory();
                // initiatedHistory.setStudent(student);
                // initiatedHistory.setStudentApplicationStatus(com.cc.modals.StudentStatus.TELEX_INITIATED);
                // initiatedHistory.setUpdatedBy("System"); // Auto-transition when both
                // approvals complete
                // initiatedHistory.setUpdatedAt(java.time.LocalDateTime.now());
                // studentStatusHistoryRepository.save(initiatedHistory);
                // }

                com.cc.modals.Student savedStudent = studentRepository.save(student);
                return savedStudent;
        }

        @org.springframework.transaction.annotation.Transactional
        public com.cc.modals.Student markTelexPaymentNotReceived(Long studentId, String accountantUsername) {
                com.cc.modals.Student student = studentRepository.findById(studentId)
                                .orElseThrow(() -> new RuntimeException("Student not found with ID: " + studentId));

                // Prevent duplicate action
                String currentPaymentStatus = student.getPaymentStatus();
                if ("Received".equals(currentPaymentStatus) || "Not Received".equals(currentPaymentStatus)) {
                        throw new RuntimeException(
                                        "Payment action already taken for this student. Current status: "
                                                        + currentPaymentStatus);
                }

                student.setIsTelexAccountantApproved(false);
                student.setPaymentStatus("Not Received");

                // History
                com.cc.modals.StudentStatusHistory history = new com.cc.modals.StudentStatusHistory();
                history.setStudent(student);
                history.setStudentApplicationStatus(student.getApplicationStatus()); // Status might not change, or it
                                                                                     // could be
                                                                                     // something like PAYMENT_REJECTED
                history.setUpdatedBy(accountantUsername);
                history.setUpdatedAt(java.time.LocalDateTime.now());
                history.setRemarks("Telex payment marked as NOT RECEIVED");
                studentStatusHistoryRepository.save(history);

                return studentRepository.save(student);
        }

        @org.springframework.transaction.annotation.Transactional
        public com.cc.modals.Student confirmVisaFee(Long studentId, String accountantUsername) {
                com.cc.modals.Student student = studentRepository.findById(studentId)
                                .orElseThrow(() -> new RuntimeException("Student not found with ID: " + studentId));

                student.setApplicationStatus(com.cc.modals.StudentStatus.VISA_FEE_RECEIVED);

                com.cc.modals.Student savedStudent = studentRepository.save(student);

                // History
                com.cc.modals.StudentStatusHistory history = new com.cc.modals.StudentStatusHistory();
                history.setStudent(savedStudent);
                history.setStudentApplicationStatus(com.cc.modals.StudentStatus.VISA_FEE_RECEIVED);
                history.setUpdatedBy(accountantUsername);
                history.setUpdatedAt(java.time.LocalDateTime.now());
                studentStatusHistoryRepository.save(history);

                return savedStudent;
        }

        // ======================= DASHBOARD METHODS =======================

        public com.cc.DTO.AccountantDashboardStatsDTO getDashboardStats(String username) {
                Accountant accountant = repository.findByUsername(username)
                                .orElseThrow(() -> new RuntimeException("Accountant not found: " + username));

                java.util.List<com.cc.modals.Student> students = accountant.getStudents();
                if (students == null || students.isEmpty()) {
                        return new com.cc.DTO.AccountantDashboardStatsDTO(0L, 0L, 0L, 0L, 0.0, 0.0, 0.0, 0.0);
                }

                long totalAssigned = students.size();
                long pending = students.stream().filter(s -> "Pending".equals(s.getPaymentStatus())).count();
                long received = students.stream().filter(s -> "Received".equals(s.getPaymentStatus())).count();
                long notReceived = students.stream().filter(s -> "Not Received".equals(s.getPaymentStatus())).count();

                double totalAmountReceived = students.stream()
                                .filter(s -> "Received".equals(s.getPaymentStatus())
                                                && s.getTelexPaymentReceivedAmount() != null)
                                .mapToDouble(com.cc.modals.Student::getTelexPaymentReceivedAmount)
                                .sum();

                double totalAmountPending = students.stream()
                                .filter(s -> "Pending".equals(s.getPaymentStatus()) && s.getPaidAmount() != null)
                                .mapToDouble(com.cc.modals.Student::getPaidAmount)
                                .sum();

                double totalVisaRequestedByVisaMan = students.stream()
                                .mapToDouble(s -> {
                                        double sum = 0;
                                        if (s.getVisaPaymentRequestedAmount() != null)
                                                sum += s.getVisaPaymentRequestedAmount();
                                        if (s.getVisaManServiceCharge() != null)
                                                sum += s.getVisaManServiceCharge();
                                        return sum;
                                }).sum();

                double totalAccToVisaManPaidAmount = students.stream()
                                .filter(s -> s.getAccToVisaManPaidAmount() != null)
                                .mapToDouble(com.cc.modals.Student::getAccToVisaManPaidAmount)
                                .sum();

                return new com.cc.DTO.AccountantDashboardStatsDTO(totalAssigned, pending, received, notReceived,
                                totalAmountReceived, totalAmountPending, totalVisaRequestedByVisaMan, totalAccToVisaManPaidAmount);
        }

        public java.util.List<com.cc.DTO.CollectionSummaryDTO> getCollectionSummary(String username) {
                Accountant accountant = repository.findByUsername(username)
                                .orElseThrow(() -> new RuntimeException("Accountant not found: " + username));

                java.util.List<com.cc.modals.Student> students = accountant.getStudents();
                java.util.List<com.cc.DTO.CollectionSummaryDTO> summaries = new java.util.ArrayList<>();

                java.time.LocalDate today = java.time.LocalDate.now();

                // Today
                summaries.add(calculateCollectionForPeriod(students, "TODAY", today, today));

                // This Week (Monday to Sunday)
                java.time.LocalDate startOfWeek = today.with(java.time.DayOfWeek.MONDAY);
                java.time.LocalDate endOfWeek = today.with(java.time.DayOfWeek.SUNDAY);
                summaries.add(calculateCollectionForPeriod(students, "THIS_WEEK", startOfWeek, endOfWeek));

                // This Month
                java.time.LocalDate startOfMonth = today.withDayOfMonth(1);
                java.time.LocalDate endOfMonth = today.withDayOfMonth(today.lengthOfMonth());
                summaries.add(calculateCollectionForPeriod(students, "THIS_MONTH", startOfMonth, endOfMonth));

                // This Year
                java.time.LocalDate startOfYear = today.withDayOfYear(1);
                java.time.LocalDate endOfYear = today.withDayOfYear(today.lengthOfYear());
                summaries.add(calculateCollectionForPeriod(students, "THIS_YEAR", startOfYear, endOfYear));

                return summaries;
        }

        private com.cc.DTO.CollectionSummaryDTO calculateCollectionForPeriod(
                        java.util.List<com.cc.modals.Student> students,
                        String period, java.time.LocalDate start, java.time.LocalDate end) {
                long count = students.stream()
                                .filter(s -> s.getTelexPaymentReceivedDate() != null
                                                && !s.getTelexPaymentReceivedDate().isBefore(start)
                                                && !s.getTelexPaymentReceivedDate().isAfter(end))
                                .count();

                double total = students.stream()
                                .filter(s -> s.getTelexPaymentReceivedDate() != null
                                                && !s.getTelexPaymentReceivedDate().isBefore(start)
                                                && !s.getTelexPaymentReceivedDate().isAfter(end)
                                                && s.getTelexPaymentReceivedAmount() != null)
                                .mapToDouble(com.cc.modals.Student::getTelexPaymentReceivedAmount)
                                .sum();

                return new com.cc.DTO.CollectionSummaryDTO(period, start, end, count, total);
        }

        public java.util.List<com.cc.DTO.AccountantActivityDTO> getRecentActivities(String username, int limit) {
                // Fetch recent status history entries for fee-related statuses
                org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(0,
                                limit);
                java.util.List<com.cc.modals.StudentStatusHistory> historyList = studentStatusHistoryRepository
                                .findByUpdatedByContainingOrderByUpdatedAtDesc(username, pageable);

                return historyList.stream()
                                .filter(h -> h.getRemarks() != null
                                                && (h.getRemarks().contains("Payment")
                                                                || h.getRemarks().contains("payment")))
                                .map(h -> new com.cc.DTO.AccountantActivityDTO(
                                                h.getStudent().getFullName(),
                                                h.getStudent().getApplicationId(),
                                                h.getRemarks().contains("NOT RECEIVED") ? "PAYMENT_NOT_RECEIVED"
                                                                : "PAYMENT_RECEIVED",
                                                h.getStudent().getTelexPaymentReceivedAmount(),
                                                h.getUpdatedAt(),
                                                h.getRemarks()))
                                .collect(java.util.stream.Collectors.toList());
        }

        public java.util.List<com.cc.DTO.StudentPaymentSummaryDTO> getAssignedStudents(String username) {
                Accountant accountant = repository.findByUsername(username)
                                .orElseThrow(() -> new RuntimeException("Accountant not found: " + username));

                java.util.List<com.cc.modals.Student> students = accountant.getStudents();
                if (students == null) {
                        return new java.util.ArrayList<>();
                }

                return students.stream()
                                .map(s -> new com.cc.DTO.StudentPaymentSummaryDTO(
                                                s.getId(),
                                                s.getApplicationId(),
                                                s.getFullName(),
                                                s.getEmail(),
                                                s.getContactNo(),
                                                s.getApplicationStatus() != null ? s.getApplicationStatus().toString()
                                                                : null,
                                                s.getPaymentStatus(),
                                                s.getHasPaid(),
                                                s.getPaidAmount(),
                                                s.getPaidTo() != null ? s.getPaidTo().toString() : null,
                                                s.getTtCopyPath(),
                                                s.getPaymentConfirmationPath(),
                                                s.getVisaPaymentRequestedAmount(),
                                                s.getVisaPaymentRequestedCurrency(),
                                                s.getVisaManServiceCharge() != null ? s.getVisaManServiceCharge() : 0.0,
                                                s.getVisaPaymentTxId(),
                                                s.getVisaPaymentConfirmationPath(),
                                                s.getVisaPaymentReceivedAmount(),
                                                s.getVisaPaymentReceivedDate(),
                                                s.getAction() != null ? s.getAction().toString() : null,
                                                s.getActionOnUser() != null ? s.getActionOnUser().toString() : null,
                                                s.getAccToVisaManPaidAmount(),
                                                s.getAccToVisaManPaymentDate(),
                                                s.getAccToVisaManTxId(),
                                                s.getAccToVisaManMode(),
                                                s.getAccToVisaManStatus()))
                                .collect(java.util.stream.Collectors.toList());
        }

        public com.cc.DTO.AccountantWithStudentsDTO getAccountantWithStudents(Long id) {
                Accountant accountant = repository.findById(id)
                                .orElseThrow(() -> new RuntimeException("Accountant not found with ID: " + id));

                List<com.cc.modals.Student> students = accountant.getStudents();
                List<com.cc.DTO.AccountantStudentDTO> studentDTOs = new java.util.ArrayList<>();

                if (students != null) {
                        studentDTOs = students.stream().map(student -> {
                                com.cc.modals.StudentStatusHistory lastUpdate = student.getStudentStatusHistory()
                                                .stream()
                                                .filter(history -> history.getUpdatedBy() != null
                                                                && history.getUpdatedBy()
                                                                                .equals(accountant.getUsername()))
                                                .max(java.util.Comparator.comparing(
                                                                com.cc.modals.StudentStatusHistory::getUpdatedAt))
                                                .orElse(null);

                                java.time.LocalDateTime lastUpdateDate = lastUpdate != null ? lastUpdate.getUpdatedAt()
                                                : null;
                                String lastAction = lastUpdate != null ? lastUpdate.getRemarks()
                                                : "No updates by this accountant";

                                return new com.cc.DTO.AccountantStudentDTO(
                                                student.getApplicationId(),
                                                student.getFullName(),
                                                student.getCollege() != null ? student.getCollege().getCollegeName()
                                                                : "N/A",
                                                student.getAppliedFor() != null ? student.getAppliedFor() : "N/A",
                                                student.getApplicationStatus() != null
                                                                ? student.getApplicationStatus().toString()
                                                                : "N/A",
                                                lastUpdateDate,
                                                lastAction);
                        }).collect(java.util.stream.Collectors.toList());
                }

                return new com.cc.DTO.AccountantWithStudentsDTO(
                                accountant.getId(),
                                accountant.getUsername(),
                                accountant.getFullName(),
                                accountant.getEmail(),
                                accountant.getContactNumber(),
                                accountant.getStatus(),
                                studentDTOs.size(),
                                studentDTOs);
        }
}
