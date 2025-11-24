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
    public List<Agent> getActiveAgents() {
        return agentService.getActiveAgents();
    }

    // Add this endpoint to AgentController.java (e.g., after the getActiveAgents()
    // method)

    @GetMapping("/with-students")
    public List<Agent> getAgentsWithStudents() {
        return agentService.getAgentsWithStudents();
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
            @org.springframework.web.bind.annotation.RequestPart(value = "passport", required = true) org.springframework.web.multipart.MultipartFile passport,
            @org.springframework.web.bind.annotation.RequestPart(value = "screenshot", required = false) org.springframework.web.multipart.MultipartFile screenshot) {
        try {
            // Parse JSON string to DTO
            com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();
            objectMapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
            com.cc.DTO.TelexDetailsDTO details = objectMapper.readValue(detailsJson, com.cc.DTO.TelexDetailsDTO.class);

            String screenshotPath = null;
            if (screenshot != null && !screenshot.isEmpty()) {
                String fileName = System.currentTimeMillis() + "_" + screenshot.getOriginalFilename();
                java.nio.file.Path path = java.nio.file.Paths.get("uploads/telex-payments/" + fileName);
                java.nio.file.Files.createDirectories(path.getParent());
                java.nio.file.Files.write(path, screenshot.getBytes());
                screenshotPath = path.toString();
            }

            String passportPath = null;
            if (passport != null && !passport.isEmpty()) {
                String fileName = System.currentTimeMillis() + "_" + passport.getOriginalFilename();
                java.nio.file.Path path = java.nio.file.Paths.get("uploads/passports/" + fileName);
                java.nio.file.Files.createDirectories(path.getParent());
                java.nio.file.Files.write(path, passport.getBytes());
                passportPath = path.toString();
            }

            return ResponseEntity.ok(agentService.submitTelexDetails(studentId, details, screenshotPath, passportPath));
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
}
