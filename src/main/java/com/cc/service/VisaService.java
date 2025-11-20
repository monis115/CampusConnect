package com.cc.service;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cc.modals.Student;
import com.cc.modals.StudentStatus;
import com.cc.modals.StudentStatusHistory;
import com.cc.modals.VisaMan;
import com.cc.repository.StudentRepository;

@Service
@SuppressWarnings("null")
public class VisaService {

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private VisaManService visaManService;

    // 1. Assign Visa Manager
    @Transactional
    public Student assignVisaManager(Long studentId, String managerUsername) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));

        VisaMan manager = visaManService.getByUsername(managerUsername)
                .orElseThrow(() -> new RuntimeException("Visa Manager not found"));

        student.setVisaMan(manager);
        student.setApplicationStatus(StudentStatus.VISA_FEE_PENDING);

        addHistory(student, StudentStatus.VISA_FEE_PENDING, "System (Assigned to " + managerUsername + ")");

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
    public Student applyForVisa(Long studentId, String visaManagerUsername) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));

        if (student.getVisaMan() == null || !student.getVisaMan().getUsername().equals(visaManagerUsername)) {
            throw new RuntimeException("Unauthorized: Only assigned Visa Manager can perform this action");
        }

        student.setApplicationStatus(StudentStatus.VISA_APPLIED);
        addHistory(student, StudentStatus.VISA_APPLIED, visaManagerUsername);
        return studentRepository.save(student);
    }

    // 3. Submit Passport to Embassy
    public Student submitPassportToEmbassy(Long studentId, String visaManagerUsername) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));

        if (student.getVisaMan() == null || !student.getVisaMan().getUsername().equals(visaManagerUsername)) {
            throw new RuntimeException("Unauthorized: Only assigned Visa Manager can perform this action");
        }

        student.setApplicationStatus(StudentStatus.PASSPORT_SUBMITTED_TO_EMBASSY);
        addHistory(student, StudentStatus.PASSPORT_SUBMITTED_TO_EMBASSY, visaManagerUsername);
        return studentRepository.save(student);
    }

    // 4. Confirm Visa Stamped
    public Student confirmVisaStamped(Long studentId, String visaManagerUsername) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));

        if (student.getVisaMan() == null || !student.getVisaMan().getUsername().equals(visaManagerUsername)) {
            throw new RuntimeException("Unauthorized: Only assigned Visa Manager can perform this action");
        }

        student.setApplicationStatus(StudentStatus.VISA_STAMPED);
        addHistory(student, StudentStatus.VISA_STAMPED, visaManagerUsername);
        return studentRepository.save(student);
    }

    // Helper to add history
    private void addHistory(Student student, StudentStatus status, String updatedBy) {
        StudentStatusHistory history = new StudentStatusHistory();
        history.setStudent(student);
        history.setStudentApplicationStatus(status);
        history.setUpdatedBy(updatedBy);
        history.setUpdatedAt(LocalDateTime.now());
        student.getStudentStatusHistory().add(history);
    }
}
