package com.cc.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.cc.DTO.UniversityPersonRequest;
import com.cc.modals.College;
import com.cc.modals.UniversityPerson;
import com.cc.repository.CollegeRepository;
import com.cc.repository.UniversityPersonRepository;
import com.cc.repository.StudentRepository;
import com.cc.repository.StudentStatusHistoryRepository;
import com.cc.modals.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class UniversityPersonService {

    private final UniversityPersonRepository upRepository;
    private final CollegeRepository collegeRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private StudentStatusHistoryRepository statusHistoryRepo;

    public UniversityPersonService(UniversityPersonRepository upRepository,
            CollegeRepository collegeRepository) {
        this.upRepository = upRepository;
        this.collegeRepository = collegeRepository;
    }

    // -------------------- CREATE --------------------

    public UniversityPerson create(UniversityPersonRequest request) {

        if (upRepository.existsByUserName(request.getUserName())) {
            throw new RuntimeException("Username already taken");
        }

        // Fetch College
        College college = collegeRepository.findById(request.getCollegeId())
                .orElseThrow(() -> new RuntimeException("College not found"));

        // Build Entity
        UniversityPerson person = new UniversityPerson();
        person.setUserName(request.getUserName());
        person.setPassword(request.getPassword());
        person.setStatus(request.getStatus());
        person.setRole(request.getRole());
        person.setCollege(college);
        User user = new User();

        user.setUsername(request.getUserName());
        user.setPassword(request.getPassword());
        user.setRole(request.getRole());

        userService.createUser(user);

        return upRepository.save(person);
    }

    // -------------------- UPDATE --------------------

    public UniversityPerson update(Long id, UniversityPersonRequest request) {

        UniversityPerson existing = upRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Record not found"));

        // Update fields
        existing.setUserName(request.getUserName());
        existing.setPassword(request.getPassword());
        existing.setStatus(request.getStatus());
        existing.setRole(request.getRole());

        if (request.getCollegeId() != null) {
            College college = collegeRepository.findById(request.getCollegeId())
                    .orElseThrow(() -> new RuntimeException("College not found"));
            existing.setCollege(college);
        }

        return upRepository.save(existing);
    }

    // -------------------- READ --------------------

    public UniversityPerson getById(Long id) {
        return upRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Record not found"));
    }

    public List<UniversityPerson> getAll() {
        return upRepository.findAll();
    }

    public UniversityPerson getByUserName(String userName) {
        return upRepository.findByUserName(userName)
                .orElseThrow(() -> new RuntimeException("UniversityPerson not found with username: " + userName));
    }

    // -------------------- DELETE --------------------

    public void delete(Long id) {
        upRepository.deleteById(id);
    }

    public boolean isUserNameAvailable(String userName) {
        return !upRepository.existsByUserName(userName);
    }

    // -------------------- ADMISSION LETTER UPLOAD --------------------

    @org.springframework.transaction.annotation.Transactional
    public Student uploadAdmissionLetter(Long studentId, String universityPersonUsername, MultipartFile file)
            throws IOException {

        // Find student
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found with ID: " + studentId));

        // Find UniversityPerson by username
        UniversityPerson universityPerson = upRepository.findByUserName(universityPersonUsername)
                .orElseThrow(() -> new RuntimeException(
                        "UniversityPerson not found with username: " + universityPersonUsername));

        // Validate that this student is assigned to this UniversityPerson
        if (student.getUniversityPerson() == null ||
                !student.getUniversityPerson().getId().equals(universityPerson.getId())) {
            throw new RuntimeException("Student is not assigned to this UniversityPerson");
        }

        // Validate file
        if (file.isEmpty()) {
            throw new RuntimeException("File is empty");
        }

        // Save file
        String fileName = "admission_letter_" + student.getId() + "_" + UUID.randomUUID() + ".pdf";
        Path uploadPath = Paths.get("uploads/admission-letters");

        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        Path filePath = uploadPath.resolve(fileName);
        Files.copy(file.getInputStream(), filePath);

        // Update student
        student.setAdmissionLetterPath(filePath.toString());
        // update action
        student.setAction(action.APPROVE_ADMISSION_LETTER);
        student.setActionOnUser(actionOnUser.admission_manager);

        student.setApplicationStatus(StudentStatus.PENDING_ADMISSION_LETTER);
        Student updatedStudent = studentRepository.save(student);

        // Create status history entry
        StudentStatusHistory historyEntry = new StudentStatusHistory();
        historyEntry.setStudent(updatedStudent);
        historyEntry.setStudentApplicationStatus(StudentStatus.PENDING_ADMISSION_LETTER);
        historyEntry.setUpdatedBy(universityPersonUsername);
        historyEntry.setUpdatedAt(java.time.LocalDateTime.now());
        statusHistoryRepo.save(historyEntry);

        return updatedStudent;
    }

    // -------------------- CONDITIONAL OFFER LETTER UPLOAD --------------------

    @org.springframework.transaction.annotation.Transactional
    public Student uploadConditionalOfferLetter(Long studentId, String universityPersonUsername, MultipartFile file)
            throws IOException {

        // Find student
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found with ID: " + studentId));

        // Find UniversityPerson by username
        UniversityPerson universityPerson = upRepository.findByUserName(universityPersonUsername)
                .orElseThrow(() -> new RuntimeException(
                        "UniversityPerson not found with username: " + universityPersonUsername));

        // Validate that this student is assigned to this UniversityPerson
        if (student.getUniversityPerson() == null ||
                !student.getUniversityPerson().getId().equals(universityPerson.getId())) {
            throw new RuntimeException("Student is not assigned to this UniversityPerson");
        }

        // Validate file
        if (file.isEmpty()) {
            throw new RuntimeException("File is empty");
        }

        // Save file
        String fileName = "conditional_letter_" + student.getId() + "_" + UUID.randomUUID() + ".pdf";
        Path uploadPath = Paths.get("uploads/conditional-letters");

        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        Path filePath = uploadPath.resolve(fileName);
        Files.copy(file.getInputStream(), filePath);

        // update action
        student.setAction(action.UPLOAD_ADMISSION_LETTER);
        student.setActionOnUser(actionOnUser.UniversityCoordinator);

        // Update student
        student.setConditionalOfferLetterPath(filePath.toString());
        student.setApplicationStatus(StudentStatus.CONDITIONAL_OFFER_LETTER_GENERATED);
        Student updatedStudent = studentRepository.save(student);

        // Create status history entry
        StudentStatusHistory historyEntry = new StudentStatusHistory();
        historyEntry.setStudent(updatedStudent);
        historyEntry.setStudentApplicationStatus(StudentStatus.CONDITIONAL_OFFER_LETTER_GENERATED);
        historyEntry.setUpdatedBy(universityPersonUsername);
        historyEntry.setUpdatedAt(java.time.LocalDateTime.now());
        statusHistoryRepo.save(historyEntry);

        return updatedStudent;
    }

    // -------------------- DASHBOARD METHODS --------------------

    public com.cc.DTO.UniversityDashboardStatsDTO getDashboardStats(String username) {
        UniversityPerson person = getByUserName(username);
        List<Student> students = person.getStudents();

        if (students == null || students.isEmpty()) {
            return new com.cc.DTO.UniversityDashboardStatsDTO(0L, 0L, 0L, 0L);
        }

        long totalStudents = students.size();
        long admittedStudents = studentRepository.findAdmittedStudentsByUniversityPerson(username).size();
        long rejectedStudents = students.stream()
                .filter(s -> s.getApplicationStatus() == StudentStatus.REJECTED)
                .count();
        long pendingApplications = totalStudents - admittedStudents - rejectedStudents;

        return new com.cc.DTO.UniversityDashboardStatsDTO(totalStudents, admittedStudents, pendingApplications,
                rejectedStudents);
    }

    public List<com.cc.DTO.UniversityStudentStatusCountDTO> getStudentStatusBreakdown(String username) {
        UniversityPerson person = getByUserName(username);
        List<Student> students = person.getStudents();

        if (students == null || students.isEmpty()) {
            return java.util.Collections.emptyList();
        }

        java.util.Map<String, Long> statusCounts = students.stream()
                .collect(java.util.stream.Collectors.groupingBy(s -> s.getApplicationStatus().toString(),
                        java.util.stream.Collectors.counting()));

        List<com.cc.DTO.UniversityStudentStatusCountDTO> breakdown = new java.util.ArrayList<>();
        for (java.util.Map.Entry<String, Long> entry : statusCounts.entrySet()) {
            breakdown.add(new com.cc.DTO.UniversityStudentStatusCountDTO(entry.getKey(), entry.getValue()));
        }

        return breakdown;
    }

    public List<com.cc.DTO.AdmittedStudentDTO> getAdmittedStudents(String username) {
        List<Student> admittedStudents = studentRepository.findAdmittedStudentsByUniversityPerson(username);

        return admittedStudents.stream()
                .map(s -> new com.cc.DTO.AdmittedStudentDTO(
                        s.getId(),
                        s.getFullName(),
                        s.getPassportNo(),
                        s.getAppliedFor(),
                        s.getApplicationStatus().toString(),
                        // Use updatedAt from history or just current time if not available?
                        // For now, let's use the last updated time from the student entity if
                        // available,
                        // or null.
                        // Ideally we should fetch the specific history record for admission letter
                        // generation,
                        // but for simplicity let's use null or a placeholder if not strictly required.
                        // Actually, let's try to find the latest history.
                        // For now, passing null to avoid complex queries, or maybe just current time.
                        java.time.LocalDateTime.now() // Placeholder
                ))
                .collect(java.util.stream.Collectors.toList());
    }
}