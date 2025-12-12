package com.cc.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cc.DTO.AgentRegistrationDTO;
import com.cc.modals.Agent;
import com.cc.service.AgentService;

@RestController
@RequestMapping("/api/agents")
@CrossOrigin("*")
public class AgentController {

    @Autowired
    private AgentService agentService;

    // ======================= SELF REGISTRATION =======================
    @PostMapping("/register")
    public Agent registerAgent(@RequestBody AgentRegistrationDTO agent) {
        System.out.println(agent);
        return agentService.selfAgentRegistration(agent);
    }

    // ======================= ADMIN REGISTRATION =======================
    @PostMapping("/admin/register")
    public Agent registerAgentByAdmin(@RequestBody AgentRegistrationDTO agent) {
        return agentService.registerAgent(agent);
    }

    // ======================= GET ALL AGENTS =======================
    @GetMapping
    public List<Agent> getAllAgents() {
        return agentService.getAllAgents();
    }

    // ======================= GET PENDING AGENTS =======================
    @GetMapping("/pending")
    public List<Agent> getPendingAgents() {
        return agentService.getPendingAgents();
    }

    // ======================= GET AGENT BY ID =======================
    @GetMapping("/{id}")
    public Optional<Agent> getAgentById(@PathVariable Long id) {
        return agentService.getAgentById(id);
    }

    // ======================= GET AGENT BY USER ID =======================

    // ======================= APPROVE PENDING AGENT =======================
    @PostMapping("/approve/{agentId}")
    public Agent approveAgent(@PathVariable Long agentId, @RequestBody AgentRegistrationDTO dto) {
        return agentService.approveAgent(agentId, dto.getCollegeAccessList());
    }

    // ======================= UPDATE AGENT =======================
    @PutMapping("/{id}")
    public Agent updateAgent(@PathVariable Long id, @RequestBody AgentRegistrationDTO dto) {
        return agentService.updateAgent(id, dto);
    }

    // ======================= DELETE AGENT =======================
    @DeleteMapping("/{id}")
    public String deleteAgent(@PathVariable Long id) {
        agentService.deleteAgent(id);
        return "Agent deleted successfully with ID: " + id;
    }

    @GetMapping("/active")
    public List<com.cc.DTO.AgentSummaryDTO> getActiveAgents() {
        return agentService.getActiveAgents();
    }

    // ======================= GET ALL AGENTS WITH FULL DETAILS
    // =======================
    @GetMapping("/all-details")
    public ResponseEntity<List<com.cc.DTO.AgentDetailsDTO>> getAllAgentsDetails() {
        try {
            return ResponseEntity.ok(agentService.getAllAgentsDetails());
        } catch (Exception e) {
            return ResponseEntity.status(500).body(null);
        }
    }

    // Add this endpoint to AgentController.java (e.g., after the getActiveAgents()
    // method)

    @GetMapping("/with-students")
    public List<com.cc.DTO.AgentSummaryDTO> getAgentsWithStudents() {
        return agentService.getAgentsWithStudents();
    }

    @GetMapping("/{id}/enrolled-students")
    public ResponseEntity<List<com.cc.DTO.StudentDTO>> getStudentsByAgentId(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(agentService.getStudentsByAgentId(id));
        } catch (Exception e) {
            return ResponseEntity.status(404).body(null);
        }
    }

    // ======================= GET AGENT BY USER ID =======================
    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getAgentByUserId(@PathVariable String userId) {
        try {
            Optional<Agent> agent = agentService.getAgentByUserIdWithDetails(userId);
            if (agent.isPresent()) {
                return ResponseEntity.ok(agent.get());
            }
            return ResponseEntity.status(404).body("Agent not found with userId: " + userId);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error fetching agent: " + e.getMessage());
        }
    }

    @PostMapping("/apply-for-telex/{studentId}")
    public ResponseEntity<?> applyForTelex(
            @PathVariable Long studentId,
            @org.springframework.web.bind.annotation.RequestParam("details") String detailsJson,
            @org.springframework.web.bind.annotation.RequestPart(value = "visaPhoto", required = false) org.springframework.web.multipart.MultipartFile visaPhoto,
            @org.springframework.web.bind.annotation.RequestPart(value = "visaPassportCopy", required = false) org.springframework.web.multipart.MultipartFile visaPassportCopy,
            @org.springframework.web.bind.annotation.RequestPart(value = "ttCopy", required = false) org.springframework.web.multipart.MultipartFile ttCopy,
            @org.springframework.web.bind.annotation.RequestPart(value = "paymentConfirmation", required = false) org.springframework.web.multipart.MultipartFile paymentConfirmation) {
        try {
            // Parse JSON string to TelexApplicationDTO
            com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();
            objectMapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
            com.cc.DTO.TelexApplicationDTO details = objectMapper.readValue(detailsJson,
                    com.cc.DTO.TelexApplicationDTO.class);

            // ======================= FILE SIZE VALIDATION =======================
            final long MAX_PHOTO_SIZE = 128 * 1024; // 128 KB
            final long MAX_PASSPORT_SIZE = 256 * 1024; // 256 KB
            final long MAX_DOCUMENT_SIZE = 5 * 1024 * 1024; // 5 MB for other documents

            // Validate visa photo size and format
            if (visaPhoto != null && !visaPhoto.isEmpty()) {
                if (visaPhoto.getSize() > MAX_PHOTO_SIZE) {
                    return ResponseEntity.badRequest()
                            .body("Visa photo size exceeds maximum limit of 128KB. Current size: "
                                    + (visaPhoto.getSize() / 1024) + "KB");
                }
                String contentType = visaPhoto.getContentType();
                if (contentType == null || !contentType.equals("image/jpeg")) {
                    return ResponseEntity.badRequest()
                            .body("Visa photo must be in JPEG format");
                }
            }

            // Validate passport copy size
            if (visaPassportCopy != null && !visaPassportCopy.isEmpty()) {
                if (visaPassportCopy.getSize() > MAX_PASSPORT_SIZE) {
                    return ResponseEntity.badRequest()
                            .body("Passport copy size exceeds maximum limit of 256KB. Current size: "
                                    + (visaPassportCopy.getSize() / 1024) + "KB");
                }
            }

            // ======================= FILE UPLOADS =======================
            String visaPhotoPath = null;
            if (visaPhoto != null && !visaPhoto.isEmpty()) {
                String fileName = System.currentTimeMillis() + "_" + visaPhoto.getOriginalFilename();
                java.nio.file.Path path = java.nio.file.Paths.get("uploads/visa-photos/" + fileName);
                java.nio.file.Files.createDirectories(path.getParent());
                java.nio.file.Files.write(path, visaPhoto.getBytes());
                visaPhotoPath = path.toString();
            }

            String visaPassportCopyPath = null;
            if (visaPassportCopy != null && !visaPassportCopy.isEmpty()) {
                String fileName = System.currentTimeMillis() + "_" + visaPassportCopy.getOriginalFilename();
                java.nio.file.Path path = java.nio.file.Paths.get("uploads/visa-passports/" + fileName);
                java.nio.file.Files.createDirectories(path.getParent());
                java.nio.file.Files.write(path, visaPassportCopy.getBytes());
                visaPassportCopyPath = path.toString();
            }

            String ttCopyPath = null;
            if (ttCopy != null && !ttCopy.isEmpty()) {
                if (ttCopy.getSize() > MAX_DOCUMENT_SIZE) {
                    return ResponseEntity.badRequest()
                            .body("TT copy size exceeds maximum limit");
                }
                String fileName = System.currentTimeMillis() + "_" + ttCopy.getOriginalFilename();
                java.nio.file.Path path = java.nio.file.Paths.get("uploads/tt-copies/" + fileName);
                java.nio.file.Files.createDirectories(path.getParent());
                java.nio.file.Files.write(path, ttCopy.getBytes());
                ttCopyPath = path.toString();
            }

            String paymentConfirmationPath = null;
            if (paymentConfirmation != null && !paymentConfirmation.isEmpty()) {
                if (paymentConfirmation.getSize() > MAX_DOCUMENT_SIZE) {
                    return ResponseEntity.badRequest()
                            .body("Payment confirmation size exceeds maximum limit");
                }
                String fileName = System.currentTimeMillis() + "_" + paymentConfirmation.getOriginalFilename();
                java.nio.file.Path path = java.nio.file.Paths.get("uploads/payment-confirmations/" + fileName);
                java.nio.file.Files.createDirectories(path.getParent());
                java.nio.file.Files.write(path, paymentConfirmation.getBytes());
                paymentConfirmationPath = path.toString();
            }

            // Submit telex details with all paths
            return ResponseEntity.ok(agentService.submitTelexDetails(
                    studentId, details, visaPhotoPath, visaPassportCopyPath, ttCopyPath, paymentConfirmationPath));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Error applying for telex: " + e.getMessage());
        }
    }

    // ======================= DASHBOARD ENDPOINTS =======================
    @GetMapping("/{username}/dashboard/stats")
    public ResponseEntity<?> getAgentDashboardStats(@PathVariable String username) {
        try {
            return ResponseEntity.ok(agentService.getAgentDashboardStats(username));
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error fetching dashboard stats: " + e.getMessage());
        }
    }

    @GetMapping("/{username}/dashboard/status-breakdown")
    public ResponseEntity<?> getStudentStatusBreakdown(@PathVariable String username) {
        try {
            return ResponseEntity.ok(agentService.getStudentStatusBreakdown(username));
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error fetching status breakdown: " + e.getMessage());
        }
    }

    @GetMapping("/{username}/dashboard/activities")
    public ResponseEntity<?> getRecentActivities(@PathVariable String username) {
        try {
            return ResponseEntity.ok(agentService.getRecentActivities(username));
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error fetching recent activities: " + e.getMessage());
        }
    }

    @GetMapping("/{username}/students")
    public ResponseEntity<List<com.cc.DTO.AgentStudentDTO>> getAgentStudents(@PathVariable String username) {
        try {
            return ResponseEntity.ok(agentService.getStudentsByAgentUsername(username));
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }
}
