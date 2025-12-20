package com.cc.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cc.modals.Student;
import com.cc.service.TelexService;

@RestController
@RequestMapping("/api/telex")
public class TelexController {

    @Autowired
    private TelexService telexService;

    // 1. Assign Telex Manager
    @PostMapping("/assign/{studentId}")
    public ResponseEntity<?> assignTelexManager(
            @PathVariable Long studentId,
            @RequestBody Map<String, String> request) {
        try {
            String managerUsername = request.get("managerUsername");
            String admissionManagerUsername = request.get("admissionManagerUsername");

            if (managerUsername == null || managerUsername.isEmpty()) {
                return ResponseEntity.badRequest().body("managerUsername is required");
            }
            // Fallback for backward compatibility if needed, or strictly require it.
            // For now, if not provided, we can default to "System" or throw error.
            // Given the user prompt asked to check "what he can" implying it is the manager
            // acting,
            // I should default to "System" if missing but ideally it should be sent.
            String assignedBy = (admissionManagerUsername != null && !admissionManagerUsername.isEmpty())
                    ? admissionManagerUsername
                    : "System";

            Student student = telexService.assignTelexManager(studentId, managerUsername, assignedBy);
            return ResponseEntity.ok(student);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error assigning Telex Manager: " + e.getMessage());
        }
    }

    // 2. Submit to University
    @PostMapping("/submit-university/{studentId}")
    public ResponseEntity<?> submitToUniversity(
            @PathVariable Long studentId,
            @RequestBody Map<String, String> request) {
        try {
            String managerUsername = request.get("managerUsername");
            if (managerUsername == null || managerUsername.isEmpty()) {
                return ResponseEntity.badRequest().body("managerUsername is required");
            }
            Student student = telexService.submitTelexToUniversity(studentId, managerUsername);
            return ResponseEntity.ok(student);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error submitting to university: " + e.getMessage());
        }
    }

    // 3. Submit to MEA
    @PostMapping("/submit-mea/{studentId}")
    public ResponseEntity<?> submitToMEA(
            @PathVariable Long studentId,
            @RequestBody Map<String, String> request) {
        try {
            String managerUsername = request.get("managerUsername");
            if (managerUsername == null || managerUsername.isEmpty()) {
                return ResponseEntity.badRequest().body("managerUsername is required");
            }
            Student student = telexService.confirmSubmissionToMEA(studentId, managerUsername);
            return ResponseEntity.ok(student);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error confirming submission to MEA: " + e.getMessage());
        }
    }

    // 4. Issue Telex Number
    @PostMapping("/issue-number/{studentId}")
    public ResponseEntity<?> issueTelexNumber(
            @PathVariable Long studentId,
            @RequestBody Map<String, String> request) {
        try {
            String managerUsername = request.get("managerUsername");
            String telexNumber = request.get("telexNumber");

            if (managerUsername == null || managerUsername.isEmpty()) {
                return ResponseEntity.badRequest().body("managerUsername is required");
            }
            if (telexNumber == null || telexNumber.isEmpty()) {
                return ResponseEntity.badRequest().body("telexNumber is required");
            }

            Student student = telexService.issueTelexNumber(studentId, telexNumber, managerUsername);
            return ResponseEntity.ok(student);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error issuing Telex Number: " + e.getMessage());
        }
    }

    // 5. Upload Telex Documents
    @PostMapping("/upload-documents/{studentId}")
    public ResponseEntity<?> uploadTelexDocuments(
            @PathVariable Long studentId,
            @org.springframework.web.bind.annotation.RequestParam("files") java.util.List<org.springframework.web.multipart.MultipartFile> files,
            @org.springframework.web.bind.annotation.RequestParam("managerUsername") String managerUsername) {
        try {
            if (managerUsername == null || managerUsername.isEmpty()) {
                return ResponseEntity.badRequest().body("managerUsername is required");
            }
            if (files == null || files.isEmpty()) {
                return ResponseEntity.badRequest().body("At least one file is required");
            }
            Student student = telexService.uploadTelexDocuments(studentId, files, managerUsername);
            return ResponseEntity.ok(student);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error uploading documents: " + e.getMessage());
        }
    }

    // 6. Get Telex Manager Stats & Report
    // URL: /api/telex/stats?managerUsername=...&date=YYYY-MM-DD (optional date)
    // If managerUsername is empty, "all", or "admin" -> returns aggregated stats
    // for ALL TelexMan
    @org.springframework.web.bind.annotation.GetMapping("/stats")
    public ResponseEntity<?> getTelexStats(
            @org.springframework.web.bind.annotation.RequestParam(required = false) String managerUsername,
            @org.springframework.web.bind.annotation.RequestParam(required = false) java.time.LocalDate date) {
        try {
            if (date == null)
                date = java.time.LocalDate.now();

            com.cc.DTO.TelexReportDTO report;

            // Check if we need global stats (admin view)
            if (managerUsername == null || managerUsername.isEmpty() ||
                    managerUsername.equalsIgnoreCase("all") || managerUsername.equalsIgnoreCase("admin")) {
                report = telexService.getGlobalTelexReport(date);
            } else {
                report = telexService.getTelexReport(managerUsername, date);
            }

            return ResponseEntity.ok(report);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error fetching stats: " + e.getMessage());
        }
    }

    // 7. Send Report via Email
    // URL: /api/telex/send-report
    // Body: { "managerUsername": "...", "date": "YYYY-MM-DD", "email": "..." }
    @PostMapping("/send-report")
    public ResponseEntity<?> sendReport(@RequestBody Map<String, String> request) {
        try {
            String managerUsername = request.get("managerUsername");
            String email = request.get("email");
            String dateStr = request.get("date"); // Optional
            String period = request.get("period"); // Optional (Daily/Monthly/Yearly)

            if (managerUsername == null || managerUsername.isEmpty()) {
                return ResponseEntity.badRequest().body("managerUsername is required");
            }
            if (email == null || email.isEmpty()) {
                return ResponseEntity.badRequest().body("email is required");
            }

            java.time.LocalDate date = (dateStr != null && !dateStr.isEmpty())
                    ? java.time.LocalDate.parse(dateStr)
                    : java.time.LocalDate.now();

            telexService.sendTelexReportEmail(managerUsername, date, email, period);
            return ResponseEntity.ok("Report sent successfully to " + email);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error sending report: " + e.getMessage());
        }
    }

    // 8. Get All TelexMan with their Stats (for summary table)
    // URL: /api/telex/all-managers-stats?period=daily|monthly|yearly (optional,
    // defaults to monthly)
    @org.springframework.web.bind.annotation.GetMapping("/all-managers-stats")
    public ResponseEntity<?> getAllManagersStats(
            @org.springframework.web.bind.annotation.RequestParam(required = false, defaultValue = "monthly") String period) {
        try {
            java.util.List<com.cc.DTO.TelexManStatsDTO> stats = telexService.getAllTelexManStats(period);
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error fetching all managers stats: " + e.getMessage());
        }
    }
}
