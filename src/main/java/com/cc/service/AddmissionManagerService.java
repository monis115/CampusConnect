package com.cc.service;

import com.cc.DTO.AddmissionManagerCreateDTO;
import com.cc.DTO.UpdateAdmissionManagerDTO;
import com.cc.modals.AddmissionManager;
import com.cc.modals.Student;
import com.cc.modals.StudentStatus;
import com.cc.repository.AddmissionManagerRepository;
import com.cc.repository.CollegeRepository;
import com.cc.repository.StudentRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;

@Service
@SuppressWarnings("null")
public class AddmissionManagerService {

    @Autowired
    private AddmissionManagerRepository managerRepo;

    @Autowired
    private CollegeRepository collegeRepo;

    @Autowired
    private StudentRepository studentRepo;

    final Random random = new Random();

    @Autowired
    private UserService userService;

    @Autowired
    private com.cc.repository.UniversityPersonRepository universityPersonRepo;

    @Autowired
    private com.cc.repository.StudentStatusHistoryRepository statusHistoryRepo;

    @Autowired
    private DocPersonService docPersonService;

    // ===================== CREATE =====================
    @Transactional
    public AddmissionManager createAddmissionManager(AddmissionManagerCreateDTO dto) {
        AddmissionManager manager = new AddmissionManager();
        manager.setFullName(dto.getFullName());
        manager.setEmail(dto.getEmail());
        manager.setContactNumber(dto.getContactNumber());

        // Generate unique username
        manager.setUsername(generateUniqueUsername(dto.getContactNumber()));

        // Hash password (plain is phone number)
        manager.setPassword((dto.getContactNumber()));

        // Save manager FIRST → gets ID
        AddmissionManager savedManager = managerRepo.save(manager);

        // Now assign colleges (safe: manager has ID)
        if (dto.getCollegeIds() != null && !dto.getCollegeIds().isEmpty()) {
            for (Long collegeId : dto.getCollegeIds()) {
                collegeRepo.findById(collegeId).ifPresent(college -> {
                    college.setAdmissionManager(savedManager);
                    savedManager.getColleges().add(college);
                });
            }
        }

        // Create User entry
        com.cc.modals.User user = new com.cc.modals.User();
        user.setUsername(savedManager.getUsername());
        user.setPassword(savedManager.getPassword());
        user.setRole("admission_manager");
        userService.createUser(user);

        return savedManager;
    }

    // ===================== UPDATE =====================
    @Transactional
    public AddmissionManager updateManager(Long id, UpdateAdmissionManagerDTO dto) {
        return managerRepo.findById(id).map(existing -> {
            existing.setFullName(dto.getFullName());
            existing.setEmail(dto.getEmail());
            existing.setContactNumber(dto.getContactNumber());
            existing.setRole(dto.getRole());
            existing.setStatus(dto.getStatus());

            // Unlink old colleges
            existing.getColleges().forEach(c -> c.setAdmissionManager(null));
            existing.getColleges().clear();

            // Link new colleges
            if (dto.getCollegeIds() != null) {
                dto.getCollegeIds().forEach(cid -> collegeRepo.findById(cid).ifPresent(college -> {
                    college.setAdmissionManager(existing);
                    existing.getColleges().add(college);
                }));
            }

            return managerRepo.save(existing);
        }).orElse(null);
    }

    // ===================== DELETE =====================
    @Transactional
    public void deleteManager(Long id) {
        managerRepo.findById(id).ifPresent(manager -> {
            manager.getColleges().forEach(c -> c.setAdmissionManager(null));
            manager.getColleges().clear();
            managerRepo.delete(manager);
        });
    }

    // ===================== READ =====================
    public List<AddmissionManager> getAllManagers() {
        return managerRepo.findAll();
    }

    public AddmissionManager getManagerById(Long id) {
        return managerRepo.findById(id).orElse(null);
    }

    public Optional<AddmissionManager> getAdmissionManagerByUserName(String username) {
        return managerRepo.findByUsername(username);
    }

    public List<AddmissionManager> getManagersWithStudents() {
        return managerRepo.findByStudentsIsNotEmpty();
    }

    // Inside StudentService.java (or AddmissionManagerService.java if you inject
    // StudentRepository)

    public List<Student> getApprovedStudentsForManager(AddmissionManager manager) {
        // Corrected: Use findByAdmissionManagerAndApplicationStatus
        return studentRepo.findByAdmissionManagerAndApplicationStatus(
                manager,
                StudentStatus.APPROVED);
    }

    public List<Student> getRegisteredOrUnderReviewStudentsForManager(AddmissionManager manager) {
        // Corrected: Use findByAdmissionManagerAndApplicationStatusIn
        return studentRepo.findByAdmissionManagerAndApplicationStatusIn(
                manager,
                java.util.Arrays.asList(StudentStatus.REGISTERED, StudentStatus.UNDER_REVIEW));
    }

    // ===================== UTILITY =====================
    private String generateUniqueUsername(String contactNumber) {
        String digits = contactNumber.replaceAll("\\D", "");
        String last3 = digits.length() >= 3
                ? digits.substring(digits.length() - 3)
                : String.format("%3s", digits).replace(' ', '0');
        String reversedLast3 = new StringBuilder(last3).reverse().toString();
        String yearSuffix = String.valueOf(LocalDate.now().getYear()).substring(2);

        String candidate;
        int attempts = 0;
        do {
            String rand2 = String.format("%02d", random.nextInt(100));
            candidate = "M" + yearSuffix + reversedLast3 + rand2;
            attempts++;
            if (attempts > 1000) {
                candidate = "M" + yearSuffix + reversedLast3 + (System.currentTimeMillis() % 10000);
                break;
            }
        } while (managerRepo.findByUsername(candidate).isPresent());

        return candidate;
    }

    public Optional<AddmissionManager> getAdmissionManagerByUserNameWithDetails(String username) {
        return managerRepo.findByUsernameWithDetails(username);
    }

    // ===================== FORWARD STUDENT TO UNIVERSITY PERSON
    // =====================
    @Transactional
    public Student forwardStudentToUniversityPerson(Long studentId, String admissionManagerUsername) {
        // Find the student
        Student student = studentRepo.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found with ID: " + studentId));

        // Validate that student has a college assigned
        if (student.getCollege() == null) {
            throw new RuntimeException("Student does not have a college assigned");
        }

        // Find the UniversityPerson for the student's college
        com.cc.modals.UniversityPerson universityPerson = universityPersonRepo
                .findByCollegeId(student.getCollege().getId())
                .orElseThrow(() -> new RuntimeException("No UniversityPerson found for college: " +
                        student.getCollege().getCollegeName()));

        // Update student status
        student.setApplicationStatus(StudentStatus.FORWARDED_TO_UNI);

        // Assign student to UniversityPerson
        student.setUniversityPerson(universityPerson);

        // Save the student
        Student updatedStudent = studentRepo.save(student);

        // Create status history entry
        com.cc.modals.StudentStatusHistory historyEntry = new com.cc.modals.StudentStatusHistory();
        historyEntry.setStudent(updatedStudent);
        historyEntry.setStudentApplicationStatus(StudentStatus.FORWARDED_TO_UNI);
        historyEntry.setUpdatedBy(admissionManagerUsername);
        historyEntry.setUpdatedAt(java.time.LocalDateTime.now());
        statusHistoryRepo.save(historyEntry);

        return updatedStudent;
    }

    // -------------------- APPROVE ADMISSION LETTER --------------------

    @Transactional
    public Student approveAdmissionLetter(Long studentId, String admissionManagerUsername) {

        // Find student
        Student student = studentRepo.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found with ID: " + studentId));

        // Validate admission letter exists (use internal method to bypass security
        // check)
        if (student.getAdmissionLetterPathInternal() == null || student.getAdmissionLetterPathInternal().isEmpty()) {
            throw new RuntimeException("No admission letter found for this student");
        }

        // Validate current status
        if (student.getApplicationStatus() != StudentStatus.ADMISSION_LETTER_GENERATED) {
            throw new RuntimeException("Student status must be ADMISSION_LETTER_GENERATED to approve. Current status: "
                    + student.getApplicationStatus());
        }

        // Update student status
        student.setApplicationStatus(StudentStatus.ADMISSION_LETTER_APPROVED);

        // Assign DocPerson
        List<com.cc.modals.DocPerson> docPersons = docPersonService.getAll();
        if (!docPersons.isEmpty()) {
            student.setDocPerson(docPersons.get(0));
        }

        Student updatedStudent = studentRepo.save(student);

        // Create status history entry
        com.cc.modals.StudentStatusHistory historyEntry = new com.cc.modals.StudentStatusHistory();
        historyEntry.setStudent(updatedStudent);
        historyEntry.setStudentApplicationStatus(StudentStatus.ADMISSION_LETTER_APPROVED);
        historyEntry.setUpdatedBy(admissionManagerUsername);
        historyEntry.setUpdatedAt(java.time.LocalDateTime.now());
        statusHistoryRepo.save(historyEntry);

        return updatedStudent;
    }
}