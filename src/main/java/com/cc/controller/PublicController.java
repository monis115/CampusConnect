package com.cc.controller;

import com.cc.DTO.StudentApplicationTrackingDTO;
import com.cc.DTO.AgentPublicProfileDTO;
import com.cc.service.StudentService;
import com.cc.service.AgentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/public")
public class PublicController {

    @Autowired
    private StudentService studentService;

    @Autowired
    private AgentService agentService;

    @GetMapping("/track-application")
    public ResponseEntity<?> trackApplication(@RequestParam("query") String query) {
        try {
            StudentApplicationTrackingDTO result = studentService.trackApplication(query);
            if (result == null) {
                return ResponseEntity.status(404)
                        .body("No application found with the provided Application ID or Email.");
            }
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("An error occurred while tracking the application: " + e.getMessage());
        }
    }

    // ======================= PUBLIC AGENT PROFILE (for student referral link)
    // =======================
    @GetMapping("/agent/{userId}")
    public ResponseEntity<?> getAgentPublicProfile(@PathVariable String userId) {
        try {
            AgentPublicProfileDTO profile = agentService.getAgentPublicProfile(userId);
            if (profile == null) {
                return ResponseEntity.status(404).body("Agent not found with userId: " + userId);
            }
            return ResponseEntity.ok(profile);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("Error fetching agent profile: " + e.getMessage());
        }
    }

    // ======================= FULL STUDENT INFO BY PASSPORT (public)
    // =======================
    @GetMapping("/student-by-passport")
    public ResponseEntity<?> getStudentByPassport(@RequestParam("passportNo") String passportNo) {
        try {
            if (passportNo == null || passportNo.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Passport number is required.");
            }
            com.cc.DTO.StudentFullInfoDTO result = studentService.getFullStudentInfoByPassport(passportNo.trim());
            if (result == null) {
                return ResponseEntity.status(404)
                        .body("No student found with passport number: " + passportNo);
            }
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("Error fetching student info: " + e.getMessage());
        }
    }
}
