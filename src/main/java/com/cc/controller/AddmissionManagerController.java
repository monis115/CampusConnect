package com.cc.controller;

import com.cc.DTO.AddmissionManagerCreateDTO;
import com.cc.DTO.UpdateAdmissionManagerDTO;
import com.cc.modals.AddmissionManager;
import com.cc.modals.Student;
import com.cc.service.AddmissionManagerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/managers")
@CrossOrigin("*")
public class AddmissionManagerController {

    @Autowired
    private AddmissionManagerService service;

    // CREATE
    @PostMapping("/create")
    public ResponseEntity<Map<String, Object>> createManager(@RequestBody AddmissionManagerCreateDTO dto) {
        AddmissionManager saved = service.createAddmissionManager(dto);

        Map<String, Object> response = new HashMap<>();
        response.put("id", saved.getId());
        response.put("username", saved.getUsername());
        response.put("password", dto.getContactNumber()); // plain password once
        response.put("fullName", saved.getFullName());
        response.put("email", saved.getEmail());
        response.put("contactNumber", saved.getContactNumber());
        response.put("role", saved.getRole());
        response.put("status", saved.getStatus());

        return ResponseEntity.ok(response);
    }

    // READ ALL
    @GetMapping("/all")
    public ResponseEntity<List<AddmissionManager>> getAllManagers() {
        return ResponseEntity.ok(service.getAllManagers());
    }

    // READ BY ID
    @GetMapping("/{id}")
    public ResponseEntity<AddmissionManager> getManagerById(@PathVariable Long id) {
        AddmissionManager manager = service.getManagerById(id);
        return manager != null ? ResponseEntity.ok(manager) : ResponseEntity.notFound().build();
    }

    // READ MANAGERS WITH STUDENTS
    @GetMapping("/with-students")
    public ResponseEntity<List<AddmissionManager>> getManagersWithStudents() {
        return ResponseEntity.ok(service.getManagersWithStudents());
    }

    // READ APPROVED STUDENTS
    @GetMapping("/approved-students/{username}")
    public ResponseEntity<List<Student>> getApprovedStudents(@PathVariable String username) {
        Optional<AddmissionManager> opt = service.getAdmissionManagerByUserName(username);
        if (opt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        List<Student> students = service.getApprovedStudentsForManager(opt.get());
        return ResponseEntity.ok(students);
    }

    // READ REGISTERED OR UNDER REVIEW
    @GetMapping("/students/registered-or-under-review/{username}")
    public ResponseEntity<List<Student>> getRegisteredOrUnderReviewStudents(@PathVariable String username) {
        Optional<AddmissionManager> opt = service.getAdmissionManagerByUserName(username);
        if (opt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        List<Student> students = service.getRegisteredOrUnderReviewStudentsForManager(opt.get());
        return ResponseEntity.ok(students);
    }

    // UPDATE
    @PutMapping("/update/{id}")
    public ResponseEntity<AddmissionManager> updateManager(
            @PathVariable Long id,
            @RequestBody UpdateAdmissionManagerDTO dto) {
        AddmissionManager updated = service.updateManager(id, dto);
        return updated != null ? ResponseEntity.ok(updated) : ResponseEntity.notFound().build();
    }

    // DELETE
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteManager(@PathVariable Long id) {
        service.deleteManager(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("userName/{userName}")
    public ResponseEntity<?> getManagerByUserName(@PathVariable String userName) {
        try {
            Optional<AddmissionManager> manager = service.getAdmissionManagerByUserNameWithDetails(userName);
            if (manager.isPresent()) {
                return ResponseEntity.ok(manager.get());
            }
            return ResponseEntity.status(404).body("Manager not found with username: " + userName);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error fetching manager: " + e.getMessage());
        }
    }

    // FORWARD STUDENT TO UNIVERSITY PERSON
    @PostMapping("/forward-student/{studentId}")
    public ResponseEntity<?> forwardStudentToUniversityPerson(
            @PathVariable Long studentId,
            @RequestBody Map<String, String> request) {
        try {
            String admissionManagerUsername = request.get("admissionManagerUsername");
            if (admissionManagerUsername == null || admissionManagerUsername.isEmpty()) {
                return ResponseEntity.badRequest().body("admissionManagerUsername is required");
            }

            Student updatedStudent = service.forwardStudentToUniversityPerson(studentId, admissionManagerUsername);
            return ResponseEntity.ok(updatedStudent);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error forwarding student: " + e.getMessage());
        }
    }

    // -------------------- APPROVE ADMISSION LETTER --------------------

    @PostMapping("/approve-admission-letter/{studentId}")
    public ResponseEntity<?> approveAdmissionLetter(
            @PathVariable Long studentId,
            @RequestBody Map<String, String> request) {
        try {
            String admissionManagerUsername = request.get("admissionManagerUsername");
            if (admissionManagerUsername == null || admissionManagerUsername.isEmpty()) {
                return ResponseEntity.badRequest().body("admissionManagerUsername is required");
            }

            Student updatedStudent = service.approveAdmissionLetter(studentId, admissionManagerUsername);
            return ResponseEntity.ok(updatedStudent);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error approving admission letter: " + e.getMessage());
        }
    }
}