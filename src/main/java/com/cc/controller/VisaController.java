package com.cc.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cc.modals.Student;
import com.cc.service.VisaService;

@RestController
@RequestMapping("/api/visa")
@CrossOrigin("*")
public class VisaController {

    @Autowired
    private VisaService visaService;

    // 1. Assign Visa Manager
    @PostMapping("/assign/{studentId}")
    public ResponseEntity<?> assignVisaManager(
            @PathVariable Long studentId,
            @RequestBody Map<String, String> request) {
        try {
            String managerUsername = request.get("managerUsername");
            if (managerUsername == null || managerUsername.isEmpty()) {
                return ResponseEntity.badRequest().body("managerUsername is required");
            }
            Student student = visaService.assignVisaManager(studentId, managerUsername);
            return ResponseEntity.ok(student);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error assigning Visa Manager: " + e.getMessage());
        }
    }

    // 2. Confirm Visa Fee
    @PostMapping("/fee-received/{studentId}")
    public ResponseEntity<?> confirmVisaFee(
            @PathVariable Long studentId,
            @RequestBody Map<String, String> request) {
        try {
            String managerUsername = request.get("managerUsername");
            if (managerUsername == null || managerUsername.isEmpty()) {
                return ResponseEntity.badRequest().body("managerUsername is required");
            }
            Student student = visaService.confirmVisaFee(studentId, managerUsername);
            return ResponseEntity.ok(student);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error confirming Visa Fee: " + e.getMessage());
        }
    }

    // 3. Passport Submitted
    @PostMapping("/passport-submitted/{studentId}")
    public ResponseEntity<?> submitPassport(
            @PathVariable Long studentId,
            @RequestBody Map<String, String> request) {
        try {
            String managerUsername = request.get("managerUsername");
            if (managerUsername == null || managerUsername.isEmpty()) {
                return ResponseEntity.badRequest().body("managerUsername is required");
            }
            Student student = visaService.submitPassportToEmbassy(studentId, managerUsername);
            return ResponseEntity.ok(student);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error submitting passport: " + e.getMessage());
        }
    }

    // 4. Visa Stamped
    @PostMapping("/stamped/{studentId}")
    public ResponseEntity<?> confirmVisaStamped(
            @PathVariable Long studentId,
            @RequestBody Map<String, String> request) {
        try {
            String managerUsername = request.get("managerUsername");
            if (managerUsername == null || managerUsername.isEmpty()) {
                return ResponseEntity.badRequest().body("managerUsername is required");
            }
            Student student = visaService.confirmVisaStamped(studentId, managerUsername);
            return ResponseEntity.ok(student);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error confirming Visa Stamped: " + e.getMessage());
        }
    }

    // 5. Apply for Visa
    @PostMapping("/apply/{studentId}")
    public ResponseEntity<?> applyForVisa(
            @PathVariable Long studentId,
            @RequestBody Map<String, String> request) {
        try {
            String managerUsername = request.get("managerUsername");
            if (managerUsername == null || managerUsername.isEmpty()) {
                return ResponseEntity.badRequest().body("managerUsername is required");
            }
            Student student = visaService.applyForVisa(studentId, managerUsername);
            return ResponseEntity.ok(student);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error applying for Visa: " + e.getMessage());
        }
    }
}
