package com.cc.controller;

import com.cc.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/email")
@CrossOrigin(origins = "*")
public class EmailController {

    @Autowired
    private EmailService emailService;

    /**
     * Send a simple text email.
     * 
     * Request body example:
     * {
     * "to": "recipient@example.com",
     * "subject": "Hello",
     * "content": "This is the email body"
     * }
     */
    @PostMapping("/send")
    public ResponseEntity<?> sendEmail(@RequestBody Map<String, String> request) {
        try {
            String to = request.get("to");
            String subject = request.get("subject");
            String content = request.get("content");

            if (to == null || subject == null || content == null) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "Missing required fields: to, subject, content"));
            }

            emailService.sendEmail(to, subject, content);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Email sent successfully to " + to));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "message", "Failed to send email: " + e.getMessage()));
        }
    }

    /**
     * Send an HTML email.
     * 
     * Request body example:
     * {
     * "to": "recipient@example.com",
     * "subject": "Hello",
     * "htmlContent": "
     * <h1>Welcome!</h1>
     * <p>
     * This is HTML content
     * </p>
     * "
     * }
     */
    @PostMapping("/send-html")
    public ResponseEntity<?> sendHtmlEmail(@RequestBody Map<String, String> request) {
        try {
            String to = request.get("to");
            String subject = request.get("subject");
            String htmlContent = request.get("htmlContent");

            if (to == null || subject == null || htmlContent == null) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "Missing required fields: to, subject, htmlContent"));
            }

            emailService.sendHtmlEmail(to, subject, htmlContent);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "HTML Email sent successfully to " + to));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "message", "Failed to send HTML email: " + e.getMessage()));
        }
    }
}
