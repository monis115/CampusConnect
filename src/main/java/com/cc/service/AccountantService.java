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

    public Accountant update(Long id, Accountant entity) {
        if (repository.existsById(id)) {
            entity.setId(id);
            return repository.save(entity);
        }
        return null;
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
    public com.cc.modals.Student approveTelexPayment(Long studentId, Double receivedAmount, String accountantUsername) {
        com.cc.modals.Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found with ID: " + studentId));

        student.setTelexPaymentReceivedAmount(receivedAmount);
        student.setIsTelexAccountantApproved(true);

        student.setAction(action.ASSIGN_TELEX_MAN);
        student.setActionOnUser(actionOnUser.admission_manager);

        // Always create history entry when accountant approves payment
        com.cc.modals.StudentStatusHistory paymentHistory = new com.cc.modals.StudentStatusHistory();
        paymentHistory.setStudent(student);
        paymentHistory.setStudentApplicationStatus(com.cc.modals.StudentStatus.TELEX_FEE_RECIEVED);
        paymentHistory.setUpdatedBy(accountantUsername);
        paymentHistory.setUpdatedAt(java.time.LocalDateTime.now());
        studentStatusHistoryRepository.save(paymentHistory);

        // Check if Doc Person also approved - if yes, transition to TELEX_INITIATED
        if (Boolean.TRUE.equals(student.getIsTelexDocApproved())) {
            student.setApplicationStatus(com.cc.modals.StudentStatus.TELEX_INITIATED);

            // History for Telex Initiated (both approvals complete)
            com.cc.modals.StudentStatusHistory initiatedHistory = new com.cc.modals.StudentStatusHistory();
            initiatedHistory.setStudent(student);
            initiatedHistory.setStudentApplicationStatus(com.cc.modals.StudentStatus.TELEX_INITIATED);
            initiatedHistory.setUpdatedBy("System"); // Auto-transition when both approvals complete
            initiatedHistory.setUpdatedAt(java.time.LocalDateTime.now());
            studentStatusHistoryRepository.save(initiatedHistory);
        }

        com.cc.modals.Student savedStudent = studentRepository.save(student);
        return savedStudent;
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
}
