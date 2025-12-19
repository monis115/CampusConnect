package com.cc.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.cc.modals.Student;
import com.cc.modals.StudentStatus;
import com.cc.modals.StudentStatusHistory;
import com.cc.modals.TelexMan;

import com.cc.modals.action;
import com.cc.modals.actionOnUser;
import com.cc.repository.StudentRepository;

@Service
@SuppressWarnings("null")
public class TelexService {

    private final StudentRepository studentRepository;
    private final TelexManService telexManService;

    @Autowired
    public TelexService(StudentRepository studentRepository, TelexManService telexManService) {
        this.studentRepository = studentRepository;
        this.telexManService = telexManService;
    }

    // 1. Assign Telex Manager
    public Student assignTelexManager(Long studentId, String managerUsername, String assignedBy) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));

        TelexMan manager = telexManService.getByUsername(managerUsername)
                .orElseThrow(() -> new RuntimeException("Telex Manager not found"));

        student.setTelexMan(manager);
        student.setApplicationStatus(StudentStatus.TELEX_INITIATED);

        student.setAction(action.UPLOAD_TELEX_DOCS);
        student.setActionOnUser(actionOnUser.telex_manager);

        addHistory(student, StudentStatus.TELEX_INITIATED, assignedBy, "Assigned to Telex Manager: " + managerUsername);

        return studentRepository.save(student);
    }

    // 2. Submit Telex Application to University
    // 2. Submit to University
    public Student submitTelexToUniversity(Long studentId, String telexManagerUsername) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));

        if (student.getTelexMan() == null || !student.getTelexMan().getUsername().equals(telexManagerUsername)) {
            throw new RuntimeException("Unauthorized: Only assigned Telex Manager can perform this action");
        }

        student.setApplicationStatus(StudentStatus.TELEX_APPLICATION_SUBMITTED_TO_UNIVERSITY);

        student.setAction(action.SUBMIT_TELEX_TO_MEA);
        student.setActionOnUser(actionOnUser.telex_manager);

        addHistory(student, StudentStatus.TELEX_APPLICATION_SUBMITTED_TO_UNIVERSITY, telexManagerUsername);
        return studentRepository.save(student);
    }

    // 3. Confirm Submission to MEA
    public Student confirmSubmissionToMEA(Long studentId, String telexManagerUsername) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));

        if (student.getTelexMan() == null || !student.getTelexMan().getUsername().equals(telexManagerUsername)) {
            throw new RuntimeException("Unauthorized: Only assigned Telex Manager can perform this action");
        }

        student.setApplicationStatus(StudentStatus.TELEX_APPLICATION_SUBMITTED_TO_MEA);
        student.setAction(action.NEED_TO_ISSUE_TELEX);
        student.setActionOnUser(actionOnUser.telex_manager);
        addHistory(student, StudentStatus.TELEX_APPLICATION_SUBMITTED_TO_MEA, telexManagerUsername);
        return studentRepository.save(student);
    }

    // 4. Issue Telex Number
    public Student issueTelexNumber(Long studentId, String telexNumber, String telexManagerUsername) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));

        if (student.getTelexMan() == null || !student.getTelexMan().getUsername().equals(telexManagerUsername)) {
            throw new RuntimeException("Unauthorized: Only assigned Telex Manager can perform this action");
        }

        student.setTelexNumber(telexNumber);
        student.setTelexIssueDate(java.time.LocalDate.now());
        student.setApplicationStatus(StudentStatus.TELEX_NUMBER_ISSUED);

        student.setAction(action.ASSIGN_TO_VISA_MAN);
        student.setActionOnUser(actionOnUser.admission_manager);

        addHistory(student, StudentStatus.TELEX_NUMBER_ISSUED, telexManagerUsername);

        return studentRepository.save(student);
    }

    // 5. Upload Telex Documents
    public Student uploadTelexDocuments(Long studentId, List<MultipartFile> files, String managerUsername) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));

        if (student.getTelexMan() == null || !student.getTelexMan().getUsername().equals(managerUsername)) {
            throw new RuntimeException("Unauthorized: Only assigned Telex Manager can perform this action");
        }

        StringBuilder paths = new StringBuilder();
        if (student.getTelexDocumentsPath() != null) {
            paths.append(student.getTelexDocumentsPath());
        }

        for (MultipartFile file : files) {
            String safeName = student.getFullName().replaceAll("\\s+", "_") + "_telex_doc";
            String path = saveUploadedFile(file, "telex-documents", safeName);
            if (path != null) {
                if (paths.length() > 0) {
                    paths.append(",");
                }
                paths.append(path);
            }
        }

        student.setAction(action.SUBMIT_TELEX_TO_UNIVERSITY);
        student.setActionOnUser(actionOnUser.telex_manager);

        student.setTelexDocumentsPath(paths.toString());
        addHistory(student, student.getApplicationStatus(), managerUsername + " (Uploaded Documents)");

        return studentRepository.save(student);
    }

    // Helper to save file
    private String saveUploadedFile(MultipartFile file, String folderName, String filePrefix) {
        if (file == null || file.isEmpty()) {
            return null;
        }

        try {
            // Create upload directory
            String uploadDir = "uploads/" + folderName;
            java.nio.file.Path uploadPath = java.nio.file.Paths.get(uploadDir);

            if (!java.nio.file.Files.exists(uploadPath)) {
                java.nio.file.Files.createDirectories(uploadPath);
            }

            // Generate unique filename
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }

            String newFilename = filePrefix + "_" + java.util.UUID.randomUUID().toString() + extension;
            java.nio.file.Path filePath = uploadPath.resolve(newFilename);

            // Save the file
            java.nio.file.Files.copy(file.getInputStream(), filePath,
                    java.nio.file.StandardCopyOption.REPLACE_EXISTING);

            // Return relative path for database
            return uploadDir + "/" + newFilename;

        } catch (java.io.IOException e) {
            throw new RuntimeException("Could not save file " + file.getOriginalFilename(), e);
        }
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
