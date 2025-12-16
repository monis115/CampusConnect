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

    public Optional<DocPerson> getById(Long id) {
        return repository.findById(id);
    }

    public java.util.Optional<DocPerson> getOne() {
        return repository.findAll().stream().findFirst();
    }

    public java.util.Optional<DocPerson> getByUsername(String username) {
        return repository.findByUsername(username);
    }

    public DocPerson update(Long id, DocPerson entity) {
        if (repository.existsById(id)) {
            entity.setId(id);
            return repository.save(entity);
        }
        return null;
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
            Boolean receivedPassport) {
        com.cc.modals.Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found with ID: " + studentId));

        // Assign DocPerson if not already assigned
        if (student.getDocPerson() == null) {
            DocPerson docPerson = repository.findByUsername(docPersonUsername)
                    .orElseThrow(() -> new RuntimeException("DocPerson not found with username: " + docPersonUsername));
            student.setDocPerson(docPerson);
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

        student.setAction(action.VERIFY_DOCS);
        student.setActionOnUser(actionOnUser.doc_manager);

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
        history.setUpdatedAt(java.time.LocalDateTime.now());
        history.setRemarks("Documents returned via " + method + " on " + returnedDate);
        studentStatusHistoryRepository.save(history);

        return studentRepository.save(student);
    }
}
