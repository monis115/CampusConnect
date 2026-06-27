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

    // REJECT STUDENT BACK TO AGENT
    @PostMapping("/reject-student/{studentId}")
    public ResponseEntity<?> rejectStudentToAgent(
            @PathVariable Long studentId,
            @RequestBody Map<String, String> request) {
        try {
            String admissionManagerUsername = request.get("admissionManagerUsername");
            String reason = request.get("reason");

            if (admissionManagerUsername == null || admissionManagerUsername.isEmpty()) {
                return ResponseEntity.badRequest().body("admissionManagerUsername is required");
            }
            if (reason == null || reason.isEmpty()) {
                return ResponseEntity.badRequest().body("reason is required");
            }

            Student updatedStudent = service.rejectStudentToAgent(studentId, admissionManagerUsername, reason);
            return ResponseEntity.ok(updatedStudent);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error rejecting student: " + e.getMessage());
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

    // -------------------- DASHBOARD --------------------
    @GetMapping("/dashboard/stats/{username}")
    public ResponseEntity<?> getDashboardStats(@PathVariable String username) {
        try {
            com.cc.DTO.AdmissionDashboardStatsDTO stats = service.getDashboardStats(username);
            return ResponseEntity.ok(stats);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error fetching dashboard stats: " + e.getMessage());
        }
    }

    // -------------------- NEW OPTIMIZED ENDPOINTS --------------------

    // 1. Get Admission Managers with Student Count
    @GetMapping("/summary")
    public ResponseEntity<List<com.cc.DTO.AdmissionManagerSummaryDTO>> getManagersSummary() {
        return ResponseEntity.ok(service.getAllManagersWithStudentCount());
    }

    // 2. Get Students for an Admission Manager
    @GetMapping("/{id}/student-list")
    public ResponseEntity<List<com.cc.DTO.ManagerStudentDTO>> getStudentsForManager(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(service.getStudentsForManagerWithActions(id));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // -------------------- REPORTS --------------------

    // 1. Get All Managers Reports
    @GetMapping("/reports/all")
    public ResponseEntity<List<com.cc.DTO.AdmissionManagerReportDTO>> getAllManagersReports(
            @RequestParam(defaultValue = "yearly") String period) {
        return ResponseEntity.ok(service.generateAllManagersReport(period));
    }

    // 2. Get Specific Manager Report
    @GetMapping("/reports/{managerId}")
    public ResponseEntity<com.cc.DTO.AdmissionManagerReportDTO> getManagerReport(
            @PathVariable Long managerId,
            @RequestParam(defaultValue = "yearly") String period) {
        try {
            return ResponseEntity.ok(service.generateReport(managerId, period));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // 3. Send Single Manager Report via Email
    @PostMapping("/reports/{managerId}/email")
    public ResponseEntity<Map<String, String>> sendManagerReportEmail(
            @PathVariable Long managerId,
            @RequestParam(defaultValue = "yearly") String period,
            @RequestParam String email) {
        try {
            service.sendReportByEmail(managerId, period, email);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Report sent successfully to " + email);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to send email: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }

    // 4. Send All Managers Report via Email
    @PostMapping("/reports/all/email")
    public ResponseEntity<Map<String, Object>> sendAllManagersReportEmail(
            @RequestParam(defaultValue = "weekly") String period,
            @RequestParam String email) {
        try {
            service.sendAllManagersReportByEmail(period, email);
            return ResponseEntity
                    .ok(Map.of("success", true, "message", "All-managers report successfully queued for emailing."));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "Failed to send report: " + e.getMessage()));
        }
    }

    @PutMapping("/student/{studentId}/admission-plan")
    public ResponseEntity<?> updateAdmissionPlan(
            @PathVariable Long studentId,
            @RequestBody java.util.Map<String, String> payload) {
        try {
            String status = payload.get("admissionPlanStatus");
            String notes = payload.get("admissionNotes");
            com.cc.modals.Student updatedStudent = service.updateAdmissionPlan(studentId, status, notes);
            return ResponseEntity.ok(updatedStudent);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "Failed to update admission plan: " + e.getMessage()));
        }
    }

    @PutMapping("/student/bulk-assign-driver")
    public ResponseEntity<?> bulkAssignDriver(
            @RequestBody java.util.Map<String, Object> payload) {
        try {
            @SuppressWarnings("unchecked")
            java.util.List<Integer> studentIdsRaw = (java.util.List<Integer>) payload.get("studentIds");
            java.util.List<Long> studentIds = studentIdsRaw.stream().map(Integer::longValue).collect(java.util.stream.Collectors.toList());
            String driverName = (String) payload.get("pickupDriverName");
            String driverContact = (String) payload.get("pickupDriverContact");
            String vehicleInfo = (String) payload.get("pickupVehicleInfo");
            
            service.bulkAssignDriver(studentIds, driverName, driverContact, vehicleInfo);
            return ResponseEntity.ok(Map.of("success", true, "message", "Drivers assigned successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "Failed to assign drivers: " + e.getMessage()));
        }
    }
}