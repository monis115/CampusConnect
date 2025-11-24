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
    private com.cc.repository.StudentRepository studentRepository;

    @Autowired
    private com.cc.repository.StudentStatusHistoryRepository studentStatusHistoryRepository;

    @org.springframework.transaction.annotation.Transactional
    public com.cc.modals.Student verifyTelexDocs(Long studentId, boolean approved, String comment,
            String docPersonUsername) {
        com.cc.modals.Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found with ID: " + studentId));

        // Assign DocPerson to student
        DocPerson docPerson = repository.findByUsername(docPersonUsername)
                .orElseThrow(() -> new RuntimeException("DocPerson not found with username: " + docPersonUsername));
        student.setDocPerson(docPerson);

        student.setIsTelexDocApproved(approved);
        student.setDocPersonComment(comment);

        if (approved) {
            // DocPerson approved - set status to TELEX_INITIATED
            student.setApplicationStatus(com.cc.modals.StudentStatus.TELEX_INITIATED);
            student.setAction(action.ASSIGN_TELEX_MAN);
            student.setActionOnUser(actionOnUser.admission_manager);

            // History for Telex Initiated
            com.cc.modals.StudentStatusHistory history = new com.cc.modals.StudentStatusHistory();
            history.setStudent(student);
            history.setStudentApplicationStatus(com.cc.modals.StudentStatus.TELEX_INITIATED);
            history.setUpdatedBy(docPersonUsername);
            history.setUpdatedAt(java.time.LocalDateTime.now());
            history.setRemarks(comment != null ? comment : "Documents verified and approved");
            studentStatusHistoryRepository.save(history);
        } else {
            // If rejected, set action back to agent
            student.setAction(action.CONFIRM_DOCS);
            student.setActionOnUser(actionOnUser.agent);

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
}
