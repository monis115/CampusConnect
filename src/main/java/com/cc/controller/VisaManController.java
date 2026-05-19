package com.cc.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.cc.modals.VisaMan;
import com.cc.modals.StudentStatusHistory;
import com.cc.DTO.VisaManDTO;
import com.cc.DTO.VisaManSummaryDTO;
import com.cc.DTO.VisaManStudentDTO;
import com.cc.service.VisaManService;

@RestController
@RequestMapping("/api/visa-man")
public class VisaManController {

    @Autowired
    private VisaManService service;

    @Autowired
    private com.cc.repository.PassbookVisaManAccountantRepository passbookVisaManAccountantRepository;

    @GetMapping("/passbook/{username}")
    public ResponseEntity<List<com.cc.modals.PassbookVisaManAccountant>> getVisaManPassbook(@PathVariable String username) {
        return ResponseEntity.ok(passbookVisaManAccountantRepository.findByRequesterName(username));
    }

    // Create
    @PostMapping
    public ResponseEntity<VisaMan> create(@RequestBody VisaMan entity) {
        return ResponseEntity.ok(service.create(entity));
    }

    // Read All
    @GetMapping
    public ResponseEntity<List<VisaMan>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

    // Get all active VisaMan with username and student count
    @GetMapping("/usernames")
    public ResponseEntity<List<java.util.Map<String, Object>>> getAllActiveUsernames() {
        List<java.util.Map<String, Object>> result = service.getAll().stream()
                .filter(v -> "Active".equalsIgnoreCase(v.getStatus()))
                .map(v -> java.util.Map.<String, Object>of(
                        "username", v.getUsername(),
                        "studentCount", v.getStudents() != null ? v.getStudents().size() : 0))
                .collect(java.util.stream.Collectors.toList());
        return ResponseEntity.ok(result);
    }

    // Get One Username (First Available)
    @GetMapping("/username-only")
    public ResponseEntity<java.util.Map<String, String>> getOneUsername() {
        return service.getAll().stream()
                .map(VisaMan::getUsername)
                .findFirst()
                .map(username -> {
                    java.util.Map<String, String> response = new java.util.HashMap<>();
                    response.put("username", username);
                    return ResponseEntity.ok(response);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // Read by ID
    @GetMapping("/{id}")
    public ResponseEntity<VisaMan> getById(@PathVariable Long id) {
        Optional<VisaMan> entity = service.getById(id);
        return entity.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Read by Username
    @GetMapping("/username/{username}")
    public ResponseEntity<VisaMan> getByUsername(@PathVariable String username) {
        Optional<VisaMan> entity = service.getByUsername(username);
        return entity.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Get One (First Available)
    @GetMapping("/get-one")
    public ResponseEntity<VisaMan> getOne() {
        Optional<VisaMan> entity = service.getOne();
        return entity.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Update
    @PutMapping("/{id}")
    public ResponseEntity<VisaMan> update(@PathVariable Long id, @RequestBody VisaMan entity) {
        VisaMan updated = service.update(id, entity);
        return updated != null ? ResponseEntity.ok(updated) : ResponseEntity.notFound().build();
    }

    // Make Inactive (Soft Delete)
    @PutMapping("/{id}/status/{status}")
    public ResponseEntity<VisaMan> updateStatus(@PathVariable Long id, @PathVariable String status) {
        Optional<VisaMan> optionalEntity = service.getById(id);
        if (optionalEntity.isPresent()) {
            VisaMan entity = optionalEntity.get();
            entity.setStatus(status);
            return ResponseEntity.ok(service.update(id, entity));
        }
        return ResponseEntity.notFound().build();
    }

    // Delete (Hard Delete)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/submit-passport/{studentId}")
    public ResponseEntity<?> submitPassport(
            @PathVariable Long studentId,
            @RequestBody java.util.Map<String, String> request) {
        try {
            String visaManUsername = request.get("visaManUsername");
            if (visaManUsername == null || visaManUsername.isEmpty()) {
                return ResponseEntity.badRequest().body("visaManUsername is required");
            }
            return ResponseEntity.ok(service.submitPassport(studentId, visaManUsername));
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error submitting passport: " + e.getMessage());
        }
    }

    @PostMapping("/stamp-visa/{studentId}")
    public ResponseEntity<?> stampVisa(
            @PathVariable Long studentId,
            @RequestBody java.util.Map<String, String> request) {
        try {
            String visaManUsername = request.get("visaManUsername");
            if (visaManUsername == null || visaManUsername.isEmpty()) {
                return ResponseEntity.badRequest().body("visaManUsername is required");
            }
            return ResponseEntity.ok(service.stampVisa(studentId, visaManUsername));
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error stamping visa: " + e.getMessage());
        }
    }

    // ======================= MANAGEMENT CARD ENDPOINTS =======================

    /**
     * Get all VisaMan details WITHOUT students (for management card)
     */
    @GetMapping("/all-details")
    public ResponseEntity<List<VisaManDTO>> getAllWithoutStudents() {
        List<VisaManDTO> result = service.getAll().stream()
                .map(v -> new VisaManDTO(
                        v.getId(),
                        v.getUsername(),
                        v.getFullName(),
                        v.getEmail(),
                        v.getContactNumber(),
                        v.getStatus(),
                        v.getRole(),
                        v.getStudents() != null ? v.getStudents().size() : 0))
                .collect(java.util.stream.Collectors.toList());
        return ResponseEntity.ok(result);
    }

    /**
     * Partial update for VisaMan (username, fullName, status, email, contactNumber)
     */
    @PatchMapping("/{id}")
    public ResponseEntity<?> partialUpdate(
            @PathVariable Long id,
            @RequestBody java.util.Map<String, Object> updates) {
        try {
            Optional<VisaMan> optionalEntity = service.getById(id);
            if (optionalEntity.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            VisaMan entity = optionalEntity.get();

            if (updates.containsKey("username")) {
                entity.setUsername((String) updates.get("username"));
            }
            if (updates.containsKey("fullName")) {
                entity.setFullName((String) updates.get("fullName"));
            }
            if (updates.containsKey("status")) {
                entity.setStatus((String) updates.get("status"));
            }
            if (updates.containsKey("email")) {
                entity.setEmail((String) updates.get("email"));
            }
            if (updates.containsKey("contactNumber")) {
                entity.setContactNumber((String) updates.get("contactNumber"));
            }

            return ResponseEntity.ok(service.update(id, entity));
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error updating VisaMan: " + e.getMessage());
        }
    }

    /**
     * Get basic details of all VisaMan (username, fullName, studentCount)
     * No student data is included - just the count
     */
    @GetMapping("/list-with-count")
    public ResponseEntity<List<VisaManSummaryDTO>> getListWithCount() {
        List<VisaManSummaryDTO> result = service.getAll().stream()
                .map(v -> new VisaManSummaryDTO(
                        v.getId(),
                        v.getUsername(),
                        v.getFullName(),
                        v.getStudents() != null ? v.getStudents().size() : 0))
                .collect(java.util.stream.Collectors.toList());
        return ResponseEntity.ok(result);
    }

    /**
     * Get students for a specific VisaMan with limited fields
     * Returns: applicationId, fullName, collegeName, agentName, course,
     * applicationStatus, lastHistory
     */
    @GetMapping("/{id}/students")
    public ResponseEntity<?> getStudentsByVisaManId(@PathVariable Long id) {
        try {
            Optional<VisaMan> optionalVisaMan = service.getById(id);
            if (optionalVisaMan.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            VisaMan visaMan = optionalVisaMan.get();
            List<VisaManStudentDTO> students = visaMan.getStudents().stream()
                    .map(student -> {
                        // Get last history entry
                        VisaManStudentDTO.LastHistoryDTO lastHistoryDTO = null;
                        java.util.List<StudentStatusHistory> historyList = student.getStudentStatusHistory();
                        if (historyList != null && !historyList.isEmpty()) {
                            // Sort by updatedAt descending and get the first one
                            StudentStatusHistory lastHistory = historyList.stream()
                                    .sorted((h1, h2) -> h2.getUpdatedAt().compareTo(h1.getUpdatedAt()))
                                    .findFirst()
                                    .orElse(null);
                            if (lastHistory != null) {
                                lastHistoryDTO = new VisaManStudentDTO.LastHistoryDTO(
                                        lastHistory.getStudentApplicationStatus() != null
                                                ? lastHistory.getStudentApplicationStatus().name()
                                                : null,
                                        lastHistory.getUpdatedBy(),
                                        lastHistory.getUpdatedByName(),
                                        lastHistory.getUpdatedAt(),
                                        lastHistory.getRemarks());
                            }
                        }

                        return new VisaManStudentDTO(
                                student.getId(),
                                student.getApplicationId(),
                                student.getFullName(),
                                student.getCollege() != null ? student.getCollege().getCollegeName() : null,
                                student.getAgent() != null ? student.getAgent().getFullName() : null,
                                student.getAppliedFor(),
                                student.getApplicationStatus() != null ? student.getApplicationStatus().name() : null,
                                lastHistoryDTO);
                    })
                    .collect(java.util.stream.Collectors.toList());

            return ResponseEntity.ok(students);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error getting students: " + e.getMessage());
        }
    }
}
