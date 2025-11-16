package com.cc.service;

import com.cc.DTO.AddmissionManagerCreateDTO;
import com.cc.DTO.UpdateAdmissionManagerDTO;
import com.cc.modals.AddmissionManager;
import com.cc.modals.College;
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
import java.util.stream.Collectors;

@Service
public class AddmissionManagerService {

    @Autowired
    private AddmissionManagerRepository managerRepo;

    @Autowired
    private CollegeRepository collegeRepo;

    @Autowired
    private StudentRepository studentRepo;

     final Random random = new Random();

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
                dto.getCollegeIds().forEach(cid ->
                    collegeRepo.findById(cid).ifPresent(college -> {
                        college.setAdmissionManager(existing);
                        existing.getColleges().add(college);
                    })
                );
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

 // Inside StudentService.java (or AddmissionManagerService.java if you inject StudentRepository)

    public List<Student> getApprovedStudentsForManager(AddmissionManager manager) {
        // Corrected: Use findByAdmissionManagerAndApplicationStatus
        return studentRepo.findByAdmissionManagerAndApplicationStatus(
            manager, 
            StudentStatus.APPROVED
        );
    }

    public List<Student> getRegisteredOrUnderReviewStudentsForManager(AddmissionManager manager) {
        // Corrected: Use findByAdmissionManagerAndApplicationStatusIn
        return studentRepo.findByAdmissionManagerAndApplicationStatusIn(
            manager,
            java.util.Arrays.asList(StudentStatus.REGISTERED, StudentStatus.UNDER_REVIEW)
        );
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
}