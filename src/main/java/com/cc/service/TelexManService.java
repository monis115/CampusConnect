package com.cc.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.cc.modals.TelexMan;
import com.cc.repository.TelexManRepository;
import java.util.List;
import java.util.Optional;

@Service
@SuppressWarnings("null")
public class TelexManService {

    // Trigger Re-index

    @Autowired
    private TelexManRepository repository;

    @Autowired
    private UserService userService;

    public TelexMan create(TelexMan entity) {
        TelexMan savedEntity = repository.save(entity);

        com.cc.modals.User user = new com.cc.modals.User();
        user.setUsername(savedEntity.getUsername());
        user.setPassword(savedEntity.getPassword());
        user.setRole("telex_manager");
        userService.createUser(user);

        return savedEntity;
    }

    public List<TelexMan> getAll() {
        return repository.findAll();
    }

    public Optional<TelexMan> getById(Long id) {
        return repository.findById(id);
    }

    public java.util.Optional<TelexMan> getOne() {
        return repository.findAll().stream()
                .filter(t -> "Active".equalsIgnoreCase(t.getStatus()))
                .findFirst();
    }

    public java.util.Map<String, Object> checkAvailability() {
        List<TelexMan> allTelexMen = repository.findAll();

        if (allTelexMen.isEmpty()) {
            return java.util.Map.of(
                    "canCreate", true,
                    "message", "No Telex Man exists. You can create one.",
                    "existingTelexMen", java.util.Collections.emptyList());
        }

        boolean hasActiveTelexMan = allTelexMen.stream()
                .anyMatch(t -> "Active".equalsIgnoreCase(t.getStatus()));

        List<java.util.Map<String, Object>> existingList = allTelexMen.stream()
                .map(t -> java.util.Map.<String, Object>of(
                        "id", t.getId(),
                        "username", t.getUsername(),
                        "fullName", t.getFullName(),
                        "status", t.getStatus()))
                .collect(java.util.stream.Collectors.toList());

        if (hasActiveTelexMan) {
            return java.util.Map.of(
                    "canCreate", false,
                    "message", "An active Telex Man already exists.",
                    "existingTelexMen", existingList);
        } else {
            return java.util.Map.of(
                    "canCreate", true,
                    "message", "All existing Telex Men are inactive. You can create a new one.",
                    "existingTelexMen", existingList);
        }
    }

    public Optional<TelexMan> getByUsername(String username) {
        return repository.findByUsername(username);
    }

    public TelexMan update(Long id, TelexMan entity) {
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
    public com.cc.modals.Student submitToUniversity(Long studentId, String telexManUsername) {
        com.cc.modals.Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found with ID: " + studentId));

        student.setApplicationStatus(com.cc.modals.StudentStatus.TELEX_APPLICATION_SUBMITTED_TO_UNIVERSITY);

        com.cc.modals.Student savedStudent = studentRepository.save(student);

        // History
        com.cc.modals.StudentStatusHistory history = new com.cc.modals.StudentStatusHistory();
        history.setStudent(savedStudent);
        history.setStudentApplicationStatus(com.cc.modals.StudentStatus.TELEX_APPLICATION_SUBMITTED_TO_UNIVERSITY);
        history.setUpdatedBy(telexManUsername);
        history.setUpdatedAt(java.time.LocalDateTime.now());
        studentStatusHistoryRepository.save(history);

        return savedStudent;
    }

    @org.springframework.transaction.annotation.Transactional
    public com.cc.modals.Student submitToMEA(Long studentId, String telexManUsername) {
        com.cc.modals.Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found with ID: " + studentId));

        student.setApplicationStatus(com.cc.modals.StudentStatus.TELEX_APPLICATION_SUBMITTED_TO_MEA);

        com.cc.modals.Student savedStudent = studentRepository.save(student);

        // History
        com.cc.modals.StudentStatusHistory history = new com.cc.modals.StudentStatusHistory();
        history.setStudent(savedStudent);
        history.setStudentApplicationStatus(com.cc.modals.StudentStatus.TELEX_APPLICATION_SUBMITTED_TO_MEA);
        history.setUpdatedBy(telexManUsername);
        history.setUpdatedAt(java.time.LocalDateTime.now());
        studentStatusHistoryRepository.save(history);

        return savedStudent;
    }

    @org.springframework.transaction.annotation.Transactional
    public com.cc.modals.Student issueTelexNumber(Long studentId, String telexNumber, java.time.LocalDate issueDate,
            String telexManUsername) {
        com.cc.modals.Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found with ID: " + studentId));

        student.setTelexNumber(telexNumber);
        student.setTelexIssueDate(issueDate);
        student.setApplicationStatus(com.cc.modals.StudentStatus.TELEX_NUMBER_ISSUED);

        student.setAction(com.cc.modals.action.NEED_TO_REQUEST_VISA_STAMPING);
        student.setActionOnUser(com.cc.modals.actionOnUser.agent);

        com.cc.modals.Student savedStudent = studentRepository.save(student);

        // History
        com.cc.modals.StudentStatusHistory history = new com.cc.modals.StudentStatusHistory();
        history.setStudent(savedStudent);
        history.setStudentApplicationStatus(com.cc.modals.StudentStatus.TELEX_NUMBER_ISSUED);
        history.setUpdatedBy(telexManUsername);
        history.setUpdatedAt(java.time.LocalDateTime.now());
        studentStatusHistoryRepository.save(history);

        return savedStudent;
    }
}
