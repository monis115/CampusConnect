package com.cc.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.cc.modals.TelexMan;
import com.cc.modals.StudentStatusHistory;
import com.cc.DTO.TelexManDTO;
import com.cc.DTO.TelexManSummaryDTO;
import com.cc.DTO.TelexManStudentDTO;
import com.cc.service.TelexManService;

@RestController
@RequestMapping("/api/telex-man")
public class TelexManController {

    @Autowired
    private TelexManService service;

    // Create
    @PostMapping
    public ResponseEntity<TelexMan> create(@RequestBody TelexMan entity) {
        return ResponseEntity.ok(service.create(entity));
    }

    // Read All
    @GetMapping
    public ResponseEntity<List<TelexMan>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

    // Get all TelexMan with username and status only
    @GetMapping("/summary")
    public ResponseEntity<List<java.util.Map<String, String>>> getSummary() {
        List<java.util.Map<String, String>> summary = service.getAll().stream()
                .map(t -> java.util.Map.of(
                        "username", t.getUsername(),
                        "status", t.getStatus()))
                .collect(java.util.stream.Collectors.toList());
        return ResponseEntity.ok(summary);
    }

    // Get all TelexMan details WITHOUT students (for management card)
    @GetMapping("/all-details")
    public ResponseEntity<List<TelexManDTO>> getAllWithoutStudents() {
        List<TelexManDTO> result = service.getAll().stream()
                .map(t -> new TelexManDTO(
                        t.getId(),
                        t.getUsername(),
                        t.getFullName(),
                        t.getEmail(),
                        t.getContactNumber(),
                        t.getStatus(),
                        t.getRole(),
                        t.getStudents() != null ? t.getStudents().size() : 0))
                .collect(java.util.stream.Collectors.toList());
        return ResponseEntity.ok(result);
    }

    // Partial update for TelexMan (username, fullName, status, email,
    // contactNumber)
    @PatchMapping("/{id}")
    public ResponseEntity<?> partialUpdate(
            @PathVariable Long id,
            @RequestBody java.util.Map<String, Object> updates) {
        try {
            Optional<TelexMan> optionalEntity = service.getById(id);
            if (optionalEntity.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            TelexMan entity = optionalEntity.get();

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
            return ResponseEntity.status(500).body("Error updating TelexMan: " + e.getMessage());
        }
    }

    // Get all active TelexMan with username and student count
    @GetMapping("/usernames")
    public ResponseEntity<List<java.util.Map<String, Object>>> getAllUsernames() {
        List<java.util.Map<String, Object>> result = service.getAll().stream()
                .filter(t -> "Active".equalsIgnoreCase(t.getStatus()))
                .map(t -> java.util.Map.<String, Object>of(
                        "username", t.getUsername(),
                        "studentCount", t.getStudents() != null ? t.getStudents().size() : 0))
                .collect(java.util.stream.Collectors.toList());
        return ResponseEntity.ok(result);
    }

    // Get One Username (First Available)
    @GetMapping("/username-only")
    public ResponseEntity<java.util.Map<String, String>> getOneUsername() {
        return service.getAll().stream()
                .map(TelexMan::getUsername)
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
    public ResponseEntity<TelexMan> getById(@PathVariable Long id) {
        Optional<TelexMan> entity = service.getById(id);
        return entity.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Read by Username
    @GetMapping("/username/{username}")
    public ResponseEntity<TelexMan> getByUsername(@PathVariable String username) {
        Optional<TelexMan> entity = service.getByUsername(username);
        return entity.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Get One (First Available)
    @GetMapping("/get-one")
    public ResponseEntity<TelexMan> getOne() {
        Optional<TelexMan> entity = service.getOne();
        return entity.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Update
    @PutMapping("/{id}")
    public ResponseEntity<TelexMan> update(@PathVariable Long id, @RequestBody TelexMan entity) {
        TelexMan updated = service.update(id, entity);
        return updated != null ? ResponseEntity.ok(updated) : ResponseEntity.notFound().build();
    }

    // Make Inactive (Soft Delete)
    @PutMapping("/{id}/status/{status}")
    public ResponseEntity<TelexMan> updateStatus(@PathVariable Long id, @PathVariable String status) {
        Optional<TelexMan> optionalEntity = service.getById(id);
        if (optionalEntity.isPresent()) {
            TelexMan entity = optionalEntity.get();
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

    @PostMapping("/submit-to-university/{studentId}")
    public ResponseEntity<?> submitToUniversity(
            @PathVariable Long studentId,
            @RequestBody java.util.Map<String, String> request) {
        try {
            String telexManUsername = request.get("telexManUsername");
            if (telexManUsername == null || telexManUsername.isEmpty()) {
                return ResponseEntity.badRequest().body("telexManUsername is required");
            }
            return ResponseEntity.ok(service.submitToUniversity(studentId, telexManUsername));
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error submitting to university: " + e.getMessage());
        }
    }

    @PostMapping("/submit-to-mea/{studentId}")
    public ResponseEntity<?> submitToMEA(
            @PathVariable Long studentId,
            @RequestBody java.util.Map<String, String> request) {
        try {
            String telexManUsername = request.get("telexManUsername");
            if (telexManUsername == null || telexManUsername.isEmpty()) {
                return ResponseEntity.badRequest().body("telexManUsername is required");
            }
            return ResponseEntity.ok(service.submitToMEA(studentId, telexManUsername));
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error submitting to MEA: " + e.getMessage());
        }
    }

    @PostMapping("/issue-telex-number/{studentId}")
    public ResponseEntity<?> issueTelexNumber(
            @PathVariable Long studentId,
            @RequestBody java.util.Map<String, String> request) {
        try {
            String telexNumber = request.get("telexNumber");
            String issueDateStr = request.get("issueDate");
            String telexManUsername = request.get("telexManUsername");

            if (telexNumber == null || telexNumber.isEmpty()) {
                return ResponseEntity.badRequest().body("telexNumber is required");
            }
            if (issueDateStr == null || issueDateStr.isEmpty()) {
                return ResponseEntity.badRequest().body("issueDate is required");
            }
            if (telexManUsername == null || telexManUsername.isEmpty()) {
                return ResponseEntity.badRequest().body("telexManUsername is required");
            }

            java.time.LocalDate issueDate = java.time.LocalDate.parse(issueDateStr);
            return ResponseEntity.ok(service.issueTelexNumber(studentId, telexNumber, issueDate, telexManUsername));
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error issuing telex number: " + e.getMessage());
        }
    }

    // ======================= NEW ENDPOINTS FOR MANAGEMENT CARD
    // =======================

    /**
     * Endpoint 1: Get basic details of all TelexMan (username, fullName,
     * studentCount)
     * No student data is included - just the count
     */
    @GetMapping("/list-with-count")
    public ResponseEntity<List<TelexManSummaryDTO>> getListWithCount() {
        List<TelexManSummaryDTO> result = service.getAll().stream()
                .map(t -> new TelexManSummaryDTO(
                        t.getId(),
                        t.getUsername(),
                        t.getFullName(),
                        t.getStudents() != null ? t.getStudents().size() : 0))
                .collect(java.util.stream.Collectors.toList());
        return ResponseEntity.ok(result);
    }

    /**
     * Endpoint 2: Get students for a specific TelexMan with limited fields
     * Returns: applicationId, fullName, collegeName, agentName, course,
     * applicationStatus, lastHistory
     */
    @GetMapping("/{id}/students")
    public ResponseEntity<?> getStudentsByTelexManId(@PathVariable Long id) {
        try {
            Optional<TelexMan> optionalTelexMan = service.getById(id);
            if (optionalTelexMan.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            TelexMan telexMan = optionalTelexMan.get();
            List<TelexManStudentDTO> students = telexMan.getStudents().stream()
                    .map(student -> {
                        // Get last history entry
                        TelexManStudentDTO.LastHistoryDTO lastHistoryDTO = null;
                        List<StudentStatusHistory> historyList = student.getStudentStatusHistory();
                        if (historyList != null && !historyList.isEmpty()) {
                            // Sort by updatedAt descending and get the first one
                            StudentStatusHistory lastHistory = historyList.stream()
                                    .sorted((h1, h2) -> h2.getUpdatedAt().compareTo(h1.getUpdatedAt()))
                                    .findFirst()
                                    .orElse(null);
                            if (lastHistory != null) {
                                lastHistoryDTO = new TelexManStudentDTO.LastHistoryDTO(
                                        lastHistory.getStudentApplicationStatus() != null
                                                ? lastHistory.getStudentApplicationStatus().name()
                                                : null,
                                        lastHistory.getUpdatedBy(),
                                        lastHistory.getUpdatedAt(),
                                        lastHistory.getRemarks());
                            }
                        }

                        return new TelexManStudentDTO(
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
