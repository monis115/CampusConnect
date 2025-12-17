package com.cc.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.cc.DTO.UniversityPersonRequest;
import com.cc.DTO.UniversityPersonDTO;
import com.cc.DTO.UniversityPersonSummaryDTO;
import com.cc.DTO.UniversityPersonStudentDTO;
import com.cc.modals.UniversityPerson;
import com.cc.modals.StudentStatusHistory;
import com.cc.service.UniversityPersonService;

@RestController
@RequestMapping("/api/universityPerson")

public class UniversityPersonController {

    private final UniversityPersonService service;

    public UniversityPersonController(UniversityPersonService service) {
        this.service = service;
    }

    // -------------------- CREATE --------------------

    @PostMapping
    public ResponseEntity<UniversityPerson> create(@RequestBody UniversityPersonRequest request) {
        return ResponseEntity.ok(service.create(request));
    }

    // -------------------- UPDATE --------------------

    @PutMapping("/{id}")
    public ResponseEntity<UniversityPerson> update(
            @PathVariable Long id,
            @RequestBody UniversityPersonRequest request) {

        return ResponseEntity.ok(service.update(id, request));
    }

    // -------------------- READ --------------------

    @GetMapping("/{id}")
    public ResponseEntity<UniversityPerson> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @GetMapping
    public ResponseEntity<List<UniversityPerson>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

    @GetMapping("/username/{username}")
    public ResponseEntity<UniversityPerson> getByUserName(@PathVariable String username) {
        return ResponseEntity.ok(service.getByUserName(username));
    }

    // -------------------- DELETE --------------------

    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.ok("Deleted successfully");
    }

    @GetMapping("/check-username")
    public ResponseEntity<Boolean> checkUserName(@RequestParam String userName) {
        boolean available = service.isUserNameAvailable(userName);
        return ResponseEntity.ok(available);
    }

    // -------------------- ADMISSION LETTER UPLOAD --------------------

    @PostMapping("/upload-admission-letter/{studentId}")
    public ResponseEntity<?> uploadAdmissionLetter(
            @PathVariable Long studentId,
            @RequestParam("file") org.springframework.web.multipart.MultipartFile file,
            @RequestParam("universityPersonUsername") String universityPersonUsername) {

        try {
            com.cc.modals.Student updatedStudent = service.uploadAdmissionLetter(studentId, universityPersonUsername,
                    file);
            return ResponseEntity.ok(updatedStudent);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error uploading admission letter: " + e.getMessage());
        }
    }

    // -------------------- CONDITIONAL OFFER LETTER UPLOAD --------------------

    @PostMapping("/upload-conditional-letter/{studentId}")
    public ResponseEntity<?> uploadConditionalOfferLetter(
            @PathVariable Long studentId,
            @RequestParam("file") org.springframework.web.multipart.MultipartFile file,
            @RequestParam("universityPersonUsername") String universityPersonUsername) {

        try {
            com.cc.modals.Student updatedStudent = service.uploadConditionalOfferLetter(studentId,
                    universityPersonUsername,
                    file);
            return ResponseEntity.ok(updatedStudent);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error uploading conditional offer letter: " + e.getMessage());
        }
    }

    // -------------------- DASHBOARD ENDPOINTS --------------------

    @GetMapping("/{username}/dashboard/stats")
    public ResponseEntity<?> getDashboardStats(@PathVariable String username) {
        try {
            return ResponseEntity.ok(service.getDashboardStats(username));
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error fetching dashboard stats: " + e.getMessage());
        }
    }

    @GetMapping("/{username}/dashboard/status-breakdown")
    public ResponseEntity<?> getStudentStatusBreakdown(@PathVariable String username) {
        try {
            return ResponseEntity.ok(service.getStudentStatusBreakdown(username));
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error fetching status breakdown: " + e.getMessage());
        }
    }

    @GetMapping("/{username}/dashboard/admitted-students")
    public ResponseEntity<?> getAdmittedStudents(@PathVariable String username) {
        try {
            return ResponseEntity.ok(service.getAdmittedStudents(username));
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error fetching admitted students: " + e.getMessage());
        }
    }

    // ======================= MANAGEMENT CARD ENDPOINTS =======================

    /**
     * Get all UniversityPersons with full details (no students list)
     * Includes college info and student count
     */
    @GetMapping("/all-details")
    public ResponseEntity<List<UniversityPersonDTO>> getAllWithDetails() {
        List<UniversityPersonDTO> result = service.getAll().stream()
                .map(up -> new UniversityPersonDTO(
                        up.getId(),
                        up.getUserName(),
                        up.getStatus(),
                        up.getRole(),
                        up.getCollege() != null ? up.getCollege().getId() : null,
                        up.getCollege() != null ? up.getCollege().getCollegeName() : null,
                        up.getStudents() != null ? up.getStudents().size() : 0))
                .collect(java.util.stream.Collectors.toList());
        return ResponseEntity.ok(result);
    }

    /**
     * Partial update for UniversityPerson (userName, status, role, collegeId)
     */
    @PatchMapping("/{id}")
    public ResponseEntity<?> partialUpdate(
            @PathVariable Long id,
            @RequestBody java.util.Map<String, Object> updates) {
        try {
            UniversityPerson existing = service.getById(id);
            if (existing == null) {
                return ResponseEntity.notFound().build();
            }

            if (updates.containsKey("userName")) {
                existing.setUserName((String) updates.get("userName"));
            }
            if (updates.containsKey("status")) {
                existing.setStatus((String) updates.get("status"));
            }
            if (updates.containsKey("role")) {
                existing.setRole((String) updates.get("role"));
            }
            if (updates.containsKey("collegeId")) {
                // Update college assignment - this is the key part for 1:1 relationship
                Object collegeIdObj = updates.get("collegeId");
                if (collegeIdObj != null) {
                    Long collegeId = Long.valueOf(collegeIdObj.toString());
                    // Use the service to update with college
                    UniversityPersonRequest updateRequest = new UniversityPersonRequest();
                    updateRequest.setUserName(existing.getUserName());
                    updateRequest.setPassword(existing.getPassword());
                    updateRequest.setStatus(existing.getStatus());
                    updateRequest.setRole(existing.getRole());
                    updateRequest.setCollegeId(collegeId);
                    return ResponseEntity.ok(service.update(id, updateRequest));
                }
            }

            // For updates without college change, create request from existing
            UniversityPersonRequest updateRequest = new UniversityPersonRequest();
            updateRequest.setUserName(existing.getUserName());
            updateRequest.setPassword(existing.getPassword());
            updateRequest.setStatus(existing.getStatus());
            updateRequest.setRole(existing.getRole());
            updateRequest.setCollegeId(existing.getCollege() != null ? existing.getCollege().getId() : null);

            return ResponseEntity.ok(service.update(id, updateRequest));
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error updating UniversityPerson: " + e.getMessage());
        }
    }

    /**
     * Get basic details of all UniversityPersons (userName, collegeName,
     * studentCount)
     */
    @GetMapping("/list-with-count")
    public ResponseEntity<List<UniversityPersonSummaryDTO>> getListWithCount() {
        List<UniversityPersonSummaryDTO> result = service.getAll().stream()
                .map(up -> new UniversityPersonSummaryDTO(
                        up.getId(),
                        up.getUserName(),
                        up.getCollege() != null ? up.getCollege().getCollegeName() : null,
                        up.getStudents() != null ? up.getStudents().size() : 0))
                .collect(java.util.stream.Collectors.toList());
        return ResponseEntity.ok(result);
    }

    /**
     * Get students for a specific UniversityPerson with limited fields
     * Returns: applicationId, fullName, applicationStatus, admissionLetterUploaded,
     * lastHistory
     */
    @GetMapping("/{id}/students")
    public ResponseEntity<?> getStudentsByUniversityPersonId(@PathVariable Long id) {
        try {
            UniversityPerson up = service.getById(id);
            if (up == null) {
                return ResponseEntity.notFound().build();
            }

            java.util.List<UniversityPersonStudentDTO> students = up.getStudents() != null
                    ? up.getStudents().stream()
                            .map(student -> {
                                // Get last history entry
                                UniversityPersonStudentDTO.LastHistoryDTO lastHistoryDTO = null;
                                java.util.List<StudentStatusHistory> historyList = student.getStudentStatusHistory();
                                if (historyList != null && !historyList.isEmpty()) {
                                    StudentStatusHistory lastHistory = historyList.stream()
                                            .sorted((h1, h2) -> h2.getUpdatedAt().compareTo(h1.getUpdatedAt()))
                                            .findFirst()
                                            .orElse(null);
                                    if (lastHistory != null) {
                                        lastHistoryDTO = new UniversityPersonStudentDTO.LastHistoryDTO(
                                                lastHistory.getStudentApplicationStatus() != null
                                                        ? lastHistory.getStudentApplicationStatus().name()
                                                        : null,
                                                lastHistory.getUpdatedBy(),
                                                lastHistory.getUpdatedAt(),
                                                lastHistory.getRemarks());
                                    }
                                }

                                // Check if admission letter is uploaded
                                String admissionLetterUploaded = (student.getAdmissionLetterPathInternal() != null
                                        && !student.getAdmissionLetterPathInternal().isEmpty()) ? "Yes" : "No";

                                return new UniversityPersonStudentDTO(
                                        student.getId(),
                                        student.getApplicationId(),
                                        student.getFullName(),
                                        student.getApplicationStatus() != null ? student.getApplicationStatus().name()
                                                : null,
                                        admissionLetterUploaded,
                                        lastHistoryDTO);
                            })
                            .collect(java.util.stream.Collectors.toList())
                    : java.util.Collections.emptyList();

            return ResponseEntity.ok(students);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error getting students: " + e.getMessage());
        }
    }
}
