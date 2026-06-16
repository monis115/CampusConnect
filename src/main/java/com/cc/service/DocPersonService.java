package com.cc.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.cc.modals.DocPerson;
import com.cc.modals.action;
import com.cc.modals.actionOnUser;
import com.cc.repository.DocPersonRepository;
import java.util.List;
import java.util.Optional;

@Service
@SuppressWarnings("null")
public class DocPersonService {

    @Autowired
    private DocPersonRepository repository;

    @Autowired
    private UserService userService;

    public DocPerson create(DocPerson entity) {
        DocPerson savedEntity = repository.save(entity);

        com.cc.modals.User user = new com.cc.modals.User();
        user.setUsername(savedEntity.getUsername());
        user.setPassword(savedEntity.getPassword());
        user.setRole("doc_manager");
        userService.createUser(user);

        return savedEntity;
    }

    public List<DocPerson> getAll() {
        return repository.findAll();
    }

    public java.util.Map<String, Object> checkAvailability() {
        List<DocPerson> allDocPersons = repository.findAll();

        if (allDocPersons.isEmpty()) {
            // No DocPerson exists - can create
            return java.util.Map.of(
                    "canCreate", true,
                    "message", "No Doc Person exists. You can create one.",
                    "existingDocPersons", java.util.Collections.emptyList());
        }

        // Check if any active DocPerson exists
        boolean hasActiveDocPerson = allDocPersons.stream()
                .anyMatch(dp -> "Active".equalsIgnoreCase(dp.getStatus()));

        // Build list of existing doc persons with basic info
        List<java.util.Map<String, Object>> existingList = allDocPersons.stream()
                .map(dp -> java.util.Map.<String, Object>of(
                        "id", dp.getId(),
                        "username", dp.getUsername(),
                        "fullName", dp.getFullName(),
                        "status", dp.getStatus()))
                .collect(java.util.stream.Collectors.toList());

        if (hasActiveDocPerson) {
            // Active DocPerson exists - cannot create
            return java.util.Map.of(
                    "canCreate", false,
                    "message", "An active Doc Person already exists.",
                    "existingDocPersons", existingList);
        } else {
            // All DocPersons are inactive - can create
            return java.util.Map.of(
                    "canCreate", true,
                    "message", "All existing Doc Persons are inactive. You can create a new one.",
                    "existingDocPersons", existingList);
        }
    }

    public Optional<DocPerson> getById(Long id) {
        return repository.findById(id);
    }

    public java.util.Optional<DocPerson> getOne() {
        return repository.findAll().stream()
                .filter(dp -> "Active".equalsIgnoreCase(dp.getStatus()))
                .findFirst();
    }

    public java.util.Optional<DocPerson> getByUsername(String username) {
        return repository.findByUsername(username);
    }

    public DocPerson update(Long id, DocPerson entity) {
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
            return repository.save(existing);
        }).orElse(null);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }

    @Autowired
    private com.cc.repository.DocumentReturnRequestRepository documentReturnRequestRepository;

    public com.cc.modals.DocumentReturnRequest getDocumentReturnRequest(Long studentId) {
        com.cc.modals.DocumentReturnRequest request = documentReturnRequestRepository.findByStudentId(studentId);
        if (request == null) {
            throw new RuntimeException("Document return request not found for student ID: " + studentId);
        }
        return request;
    }

    @Autowired
    private com.cc.repository.StudentRepository studentRepository;

    @Autowired
    private com.cc.repository.StudentStatusHistoryRepository studentStatusHistoryRepository;

    public com.cc.modals.Student receiveDocs(Long studentId, java.time.LocalDate receivedDate,
            String docPersonUsername,
            Boolean received12thMarksheet,
            Boolean received10thMarksheet,
            Boolean receivedNeetScorecard,
            Boolean receivedPassport,
            Boolean receivedAadharCard,
            Boolean receivedMedicalReport,
            Boolean receivedOtherDocuments,
            String receivedOtherDocumentsRemarks,
            String physicalStatus) {
        com.cc.modals.Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found with ID: " + studentId));

        // Assign DocPerson if not already assigned
        if (student.getDocPerson() == null) {
            DocPerson docPerson = repository.findByUsername(docPersonUsername)
                    .orElseThrow(() -> new RuntimeException("DocPerson not found with username: " + docPersonUsername));
            student.setDocPerson(docPerson);
            student.setDocPersonAssignedDate(java.time.LocalDate.now());
        }

        student.setDocReceivedDate(receivedDate);

        // Set document receipt flags
        if (received12thMarksheet != null)
            student.setReceived12thMarksheet(received12thMarksheet);
        if (received10thMarksheet != null)
            student.setReceived10thMarksheet(received10thMarksheet);
        if (receivedNeetScorecard != null)
            student.setReceivedNeetScorecard(receivedNeetScorecard);
        if (receivedPassport != null)
            student.setReceivedPassport(receivedPassport);
        if (receivedAadharCard != null)
            student.setReceivedAadharCard(receivedAadharCard);
        if (receivedMedicalReport != null)
            student.setReceivedMedicalReport(receivedMedicalReport);
        if (receivedOtherDocuments != null)
            student.setReceivedOtherDocuments(receivedOtherDocuments);
        if (receivedOtherDocumentsRemarks != null)
            student.setReceivedOtherDocumentsRemarks(receivedOtherDocumentsRemarks);

        // Only move to Verify stage if documents were actually received
        if (!"NOT_RECEIVED".equals(physicalStatus)) {
            student.setAction(action.VERIFY_DOCS);
            student.setActionOnUser(actionOnUser.doc_manager);
        }

        // Build remarks with received documents
        StringBuilder docsReceived = new StringBuilder();
        if (Boolean.TRUE.equals(received12thMarksheet))
            docsReceived.append("12th Marksheet, ");
        if (Boolean.TRUE.equals(received10thMarksheet))
            docsReceived.append("10th Marksheet, ");
        if (Boolean.TRUE.equals(receivedNeetScorecard))
            docsReceived.append("NEET Scorecard, ");
        if (Boolean.TRUE.equals(receivedPassport))
            docsReceived.append("Passport, ");
        String docsReceivedStr = docsReceived.length() > 0 ? docsReceived.substring(0, docsReceived.length() - 2)
                : "None specified";

        // History
        com.cc.modals.StudentStatusHistory history = new com.cc.modals.StudentStatusHistory();
        history.setStudent(student);
        history.setStudentApplicationStatus(student.getApplicationStatus());
        history.setUpdatedBy("Received by " + docPersonUsername + "Docs " + docsReceivedStr);
        history.setUpdatedByName(userService.resolveFullName(docPersonUsername));
        history.setUpdatedAt(java.time.LocalDateTime.now());
        history.setRemarks("Documents received on " + receivedDate + ". Docs: " + docsReceivedStr);
        studentStatusHistoryRepository.save(history);

        return studentRepository.save(student);
    }

    @org.springframework.transaction.annotation.Transactional
    public com.cc.modals.Student verifyTelexDocs(Long studentId, boolean approved, String comment,
            String docPersonUsername) {
        com.cc.modals.Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found with ID: " + studentId));

        student.setIsTelexDocApproved(approved);
        student.setDocPersonComment(comment);

        if (approved) {
            // DocPerson approved - set status to TELEX_INITIATED
            student.setApplicationStatus(com.cc.modals.StudentStatus.TELEX_INITIATED);
            student.setAction(action.ASSIGN_TELEX_MAN);
            student.setActionOnUser(actionOnUser.admission_manager);

            // Enable parallel return workflow
            student.setReadyForDocReturn(false);

            // History for Telex Initiated
            com.cc.modals.StudentStatusHistory history = new com.cc.modals.StudentStatusHistory();
            history.setStudent(student);
            history.setStudentApplicationStatus(com.cc.modals.StudentStatus.TELEX_INITIATED);
            history.setUpdatedBy("Verified by " + docPersonUsername);
            history.setUpdatedByName(userService.resolveFullName(docPersonUsername));
            history.setUpdatedAt(java.time.LocalDateTime.now());
            history.setRemarks(comment != null ? comment : "Documents verified and approved");
            studentStatusHistoryRepository.save(history);
        } else {
            // If rejected, set action back to agent
            student.setAction(action.CONFIRM_DOCS);
            student.setActionOnUser(actionOnUser.agent);
            student.setReadyForDocReturn(false);

            // Create history entry for rejection
            com.cc.modals.StudentStatusHistory history = new com.cc.modals.StudentStatusHistory();
            history.setStudent(student);
            history.setStudentApplicationStatus(student.getApplicationStatus());
            history.setUpdatedBy(docPersonUsername);
            history.setUpdatedByName(userService.resolveFullName(docPersonUsername));
            history.setUpdatedAt(java.time.LocalDateTime.now());
            history.setRemarks("Document verification rejected: " + (comment != null ? comment : "No comment"));
            studentStatusHistoryRepository.save(history);
        }

        return studentRepository.save(student);
    }

    @org.springframework.transaction.annotation.Transactional
    public com.cc.modals.Student confirmCollection(Long studentId, String docPersonUsername) {
        com.cc.modals.Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found with ID: " + studentId));

        // Validate state
        if (student.getAction() != action.COLLECT_DOCS && !Boolean.TRUE.equals(student.getReadyForDocReturn())) {
            throw new RuntimeException(
                    "Student is not in 'Collect Docs' state. Current action: " + student.getAction());
        }

        // Update action to indicate documents are now with Doc Person, ready for return
        student.setAction(action.INITIATE_DOC_RETURN);
        student.setReadyForDocReturn(false);
        student.setActionOnUser(actionOnUser.doc_manager);

        // History
        com.cc.modals.StudentStatusHistory history = new com.cc.modals.StudentStatusHistory();
        history.setStudent(student);
        history.setStudentApplicationStatus(student.getApplicationStatus());
        history.setUpdatedBy("Collected by " + docPersonUsername);
        history.setUpdatedByName(userService.resolveFullName(docPersonUsername));
        history.setUpdatedAt(java.time.LocalDateTime.now());
        history.setRemarks("Documents collected from Visa Manager. Ready to return to student.");
        studentStatusHistoryRepository.save(history);

        return studentRepository.save(student);
    }

    @org.springframework.transaction.annotation.Transactional
    public com.cc.modals.Student returnDocs(Long studentId, com.cc.modals.DocDeliveryMethod method, String trackingId,
            java.time.LocalDate returnedDate, String docPersonUsername) {
        com.cc.modals.Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found with ID: " + studentId));

        student.setDocReturnMethod(method);
        student.setDocReturnTrackingId(trackingId);
        student.setDocReturnedDate(returnedDate);

        // Mark return as complete
        student.setReadyForDocReturn(false);
        student.setAction(action.DOCS_RETURNED);
        student.setActionOnUser(actionOnUser.agent); // Handed back to Agent/Student

        // History
        com.cc.modals.StudentStatusHistory history = new com.cc.modals.StudentStatusHistory();
        history.setStudent(student);
        history.setStudentApplicationStatus(student.getApplicationStatus());
        history.setUpdatedBy("Returned by " + docPersonUsername);
        history.setUpdatedByName(userService.resolveFullName(docPersonUsername));
        history.setUpdatedAt(java.time.LocalDateTime.now());
        history.setRemarks("Documents returned via " + method + " on " + returnedDate);
        studentStatusHistoryRepository.save(history);

        return studentRepository.save(student);
    }

    public com.cc.DTO.DocPersonWithStudentsDTO getDocPersonWithStudents(Long id) {
        DocPerson docPerson = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("DocPerson not found with ID: " + id));

        List<com.cc.modals.Student> students = docPerson.getStudents();
        java.util.List<com.cc.DTO.DocPersonStudentDTO> studentDTOs = new java.util.ArrayList<>();

        if (students != null) {
            studentDTOs = students.stream().map(student -> {
                com.cc.modals.StudentStatusHistory lastUpdate = student.getStudentStatusHistory().stream()
                        .filter(history -> history.getUpdatedBy() != null
                                && history.getUpdatedBy().contains(docPerson.getUsername()))
                        .max(java.util.Comparator.comparing(com.cc.modals.StudentStatusHistory::getUpdatedAt))
                        .orElse(null);

                java.time.LocalDateTime lastUpdateDate = lastUpdate != null ? lastUpdate.getUpdatedAt() : null;
                String lastAction = lastUpdate != null ? lastUpdate.getRemarks() : "No updates by this DocPerson";

                return new com.cc.DTO.DocPersonStudentDTO(
                        student.getApplicationId(),
                        student.getFullName(),
                        student.getCollege() != null ? student.getCollege().getCollegeName() : "N/A",
                        student.getAppliedFor() != null ? student.getAppliedFor() : "N/A",
                        student.getApplicationStatus() != null ? student.getApplicationStatus().toString() : "N/A",
                        lastUpdateDate,
                        lastAction);
            }).collect(java.util.stream.Collectors.toList());
        }

        return new com.cc.DTO.DocPersonWithStudentsDTO(
                docPerson.getId(),
                docPerson.getUsername(),
                docPerson.getFullName(),
                docPerson.getEmail(),
                docPerson.getContactNumber(),
                docPerson.getStatus(),
                studentDTOs.size(),
                studentDTOs);
    }

    // ======================= REPORTING & STATISTICS =======================

    @Autowired
    private com.cc.service.EmailService emailService;

    public com.cc.DTO.DocPersonStatsDTO getStats(String username, String period) {
        java.time.LocalDate today = java.time.LocalDate.now();
        java.time.LocalDate start;
        java.time.LocalDate end = today;
        java.time.LocalDateTime startDateTime;
        java.time.LocalDateTime endDateTime = java.time.LocalDateTime.now();

        switch (period.toLowerCase()) {
            case "today":
            case "daily":
                start = today;
                startDateTime = today.atStartOfDay();
                break;
            case "month":
            case "monthly":
            case "this_month":
                start = today.withDayOfMonth(1);
                startDateTime = start.atStartOfDay();
                break;
            case "year":
            case "yearly":
            case "this_year":
                start = today.withDayOfYear(1);
                startDateTime = start.atStartOfDay();
                break;
            default: // All time
                start = java.time.LocalDate.of(2000, 1, 1);
                startDateTime = start.atStartOfDay();
                break;
        }

        // 1. Total Assigned (Active in period context)
        long totalAssigned;
        if ("all".equalsIgnoreCase(period)) {
            totalAssigned = studentRepository.countByDocPerson_Username(username);
        } else {
            totalAssigned = studentRepository.countByDocPerson_UsernameAndDocPersonAssignedDateBetween(username, start,
                    end);
        }

        // 2. Received Docs
        long receivedVal = studentRepository.countByDocPerson_UsernameAndDocReceivedDateBetween(username, start, end);

        // 3. Verified Docs
        long verifiedVal = studentRepository.countByDocPersonAndVerifiedDocsDateRange(username, startDateTime,
                endDateTime);

        // 4. Collected Docs
        long collectedVal = studentRepository.countByDocPersonAndCollectedDocsDateRange(username, startDateTime,
                endDateTime);

        // 5. Returned Docs
        long returnedVal = studentRepository.countByDocPerson_UsernameAndDocReturnedDateBetween(username, start, end);

        return new com.cc.DTO.DocPersonStatsDTO(period, totalAssigned, receivedVal, verifiedVal, collectedVal,
                returnedVal);
    }

    public void sendReportEmail(String period, String username, String email) {
        try {
            com.cc.DTO.DocPersonStatsDTO stats = getStats(username, period);

            String subject = "Doc Manager Report (" + period + ") - " + username;
            String content = buildEmailContent(stats, username, period);

            emailService.sendEmail(email, subject, content);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to send report email: " + e.getMessage());
        }
    }

    private String buildEmailContent(com.cc.DTO.DocPersonStatsDTO stats, String username, String period) {
        StringBuilder sb = new StringBuilder();
        sb.append("Dear ").append(username).append(",\n\n");
        sb.append("Here is your ").append(period).append(" report for Doc Manager activities:\n\n");
        sb.append("Period: ").append(period).append("\n");
        sb.append("--------------------------------------------------\n");
        sb.append("Total Students Assigned: ").append(stats.getTotalAssignedStudents()).append("\n");
        sb.append("Documents Received:      ").append(stats.getDocumentsReceived()).append("\n");
        sb.append("Documents Verified:      ").append(stats.getDocumentsVerified()).append("\n");
        sb.append("Documents Collected:     ").append(stats.getDocumentsCollected()).append("\n");
        sb.append("Documents Returned:      ").append(stats.getDocumentsReturned()).append("\n");
        sb.append("--------------------------------------------------\n\n");
        sb.append("Best Regards,\nCampusConnect System");
        return sb.toString();
    }
}
