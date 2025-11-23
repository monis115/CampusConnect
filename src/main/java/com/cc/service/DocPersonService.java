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

        student.setIsTelexDocApproved(approved);
        student.setDocPersonComment(comment);

        if (approved) {
            // Check if Accountant also approved
            if (Boolean.TRUE.equals(student.getIsTelexAccountantApproved())) {
                student.setApplicationStatus(com.cc.modals.StudentStatus.TELEX_INITIATED);
                student.setAction(action.ASSIGN_TELEX_MAN);
                student.setActionOnUser(actionOnUser.admission_manager);

                // History for Telex Initiated
                com.cc.modals.StudentStatusHistory history = new com.cc.modals.StudentStatusHistory();
                history.setStudent(student);
                history.setStudentApplicationStatus(com.cc.modals.StudentStatus.DOCS_VARIFICATION_COMPLETE);
                history.setUpdatedBy("System");
                history.setUpdatedAt(java.time.LocalDateTime.now());
                studentStatusHistoryRepository.save(history);
            }
        } else {
            // If rejected, maybe revert status or just leave it as is but with flag false?
            // Usually if rejected, we might want to notify agent.
            // For now, we just save the state.
        }

        return studentRepository.save(student);
    }
}
