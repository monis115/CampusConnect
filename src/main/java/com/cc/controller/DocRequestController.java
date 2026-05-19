package com.cc.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.cc.modals.DocRequest;
import com.cc.modals.Student;
import com.cc.modals.StudentStatus;
import com.cc.modals.StudentStatusHistory;
import com.cc.repository.DocRequestRepository;
import com.cc.repository.StudentRepository;
import com.cc.repository.StudentStatusHistoryRepository;
import com.cc.service.S3StorageService;
import com.cc.service.UserService;

@RestController
@RequestMapping("/api/doc-requests")
public class DocRequestController {

    private final DocRequestRepository docRequestRepo;
    private final StudentRepository studentRepo;
    private final StudentStatusHistoryRepository historyRepo;
    private final S3StorageService storageService;
    private final UserService userService;

    public DocRequestController(
            DocRequestRepository docRequestRepo,
            StudentRepository studentRepo,
            StudentStatusHistoryRepository historyRepo,
            S3StorageService storageService,
            UserService userService) {
        this.docRequestRepo = docRequestRepo;
        this.studentRepo = studentRepo;
        this.historyRepo = historyRepo;
        this.storageService = storageService;
        this.userService = userService;
    }

    // -------------------- AGENT: Create Request --------------------

    @PostMapping
    public ResponseEntity<?> createRequest(@RequestBody Map<String, Object> payload) {
        try {
            Long studentId = Long.valueOf(payload.get("studentId").toString());
            String docType = payload.get("docType").toString();
            String customDocName = payload.containsKey("customDocName") ? (String) payload.get("customDocName") : null;
            String requestedBy = payload.get("requestedBy").toString();
            String requestedByName = payload.containsKey("requestedByName") ? (String) payload.get("requestedByName") : requestedBy;
            String remarks = payload.containsKey("remarks") ? (String) payload.get("remarks") : null;

            Student student = studentRepo.findById(studentId)
                    .orElseThrow(() -> new RuntimeException("Student not found"));

            DocRequest req = new DocRequest();
            req.setStudent(student);
            req.setCollege(student.getCollege());
            req.setDocType(docType);
            req.setCustomDocName(customDocName);
            req.setStatus("PENDING");
            req.setRequestedBy(requestedBy);
            req.setRequestedByName(requestedByName);
            req.setRemarks(remarks);

            DocRequest saved = docRequestRepo.save(req);

            // Add status history
            addHistory(student, requestedBy, requestedByName,
                    "Doc Requested: " + getDocDisplayName(docType, customDocName));

            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error creating doc request: " + e.getMessage());
        }
    }

    // -------------------- AGENT: Get My Requests --------------------

    @GetMapping("/agent/{username}")
    public ResponseEntity<?> getAgentRequests(@PathVariable String username) {
        try {
            List<DocRequest> requests = docRequestRepo.findByRequestedByOrderByCreatedAtDesc(username);
            return ResponseEntity.ok(requests);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }

    // -------------------- ADMISSION MANAGER: Get Pending/All Requests --------------------

    @GetMapping("/pending")
    public ResponseEntity<?> getPendingRequests() {
        try {
            List<DocRequest> requests = docRequestRepo.findByStatusInOrderByCreatedAtDesc(
                    Arrays.asList("PENDING", "FORWARDED", "COMPLETED", "REJECTED"));
            return ResponseEntity.ok(requests);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }

    // -------------------- ADMISSION MANAGER: Forward to University --------------------

    @PutMapping("/{id}/forward")
    public ResponseEntity<?> forwardRequest(
            @PathVariable Long id,
            @RequestBody Map<String, String> payload) {
        try {
            DocRequest req = docRequestRepo.findById(id)
                    .orElseThrow(() -> new RuntimeException("Request not found"));

            String forwardedBy = payload.get("forwardedBy");
            String forwardedByName = payload.getOrDefault("forwardedByName", forwardedBy);

            req.setStatus("FORWARDED");
            req.setForwardedBy(forwardedBy);
            req.setForwardedByName(forwardedByName);
            DocRequest updated = docRequestRepo.save(req);

            // Add status history
            addHistory(req.getStudent(), forwardedBy, forwardedByName,
                    "Doc Request Forwarded to University: " + getDocDisplayName(req.getDocType(), req.getCustomDocName()));

            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error forwarding: " + e.getMessage());
        }
    }

    // -------------------- ADMISSION MANAGER: Reject Request --------------------

    @PutMapping("/{id}/reject")
    public ResponseEntity<?> rejectRequest(
            @PathVariable Long id,
            @RequestBody Map<String, String> payload) {
        try {
            DocRequest req = docRequestRepo.findById(id)
                    .orElseThrow(() -> new RuntimeException("Request not found"));

            String rejectedBy = payload.get("rejectedBy");
            String rejectedByName = payload.getOrDefault("rejectedByName", rejectedBy);
            String reason = payload.getOrDefault("reason", "");

            req.setStatus("REJECTED");
            req.setRejectionReason(reason);
            DocRequest updated = docRequestRepo.save(req);

            addHistory(req.getStudent(), rejectedBy, rejectedByName,
                    "Doc Request Rejected: " + getDocDisplayName(req.getDocType(), req.getCustomDocName()));

            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error rejecting: " + e.getMessage());
        }
    }

    // -------------------- UNIVERSITY: Get Forwarded Requests for College --------------------

    @GetMapping("/college/{collegeId}")
    public ResponseEntity<?> getCollegeRequests(@PathVariable Long collegeId) {
        try {
            List<DocRequest> requests = docRequestRepo.findByCollegeIdOrderByCreatedAtDesc(collegeId);
            return ResponseEntity.ok(requests);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }

    // -------------------- UNIVERSITY: Upload Document --------------------

    @PostMapping("/{id}/upload")
    public ResponseEntity<?> uploadDocument(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file,
            @RequestParam("uploadedBy") String uploadedBy) {
        try {
            DocRequest req = docRequestRepo.findById(id)
                    .orElseThrow(() -> new RuntimeException("Request not found"));

            // Store file
            String folderName = "doc-requests/" + req.getStudent().getId();
            String filePath = storageService.uploadFile(file, folderName, "requested-doc");

            req.setUploadedDocPath(filePath);
            req.setUploadedBy(uploadedBy);
            req.setStatus("COMPLETED");
            DocRequest updated = docRequestRepo.save(req);

            String uploaderName = uploadedBy;
            try {
                uploaderName = userService.resolveFullName(uploadedBy);
            } catch (Exception ignored) {
            }

            addHistory(req.getStudent(), uploadedBy, uploaderName,
                    "Requested Doc Uploaded: " + getDocDisplayName(req.getDocType(), req.getCustomDocName()));

            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error uploading: " + e.getMessage());
        }
    }

    // -------------------- Helpers --------------------

    private String getDocDisplayName(String docType, String customDocName) {
        if ("OTHER".equals(docType) && customDocName != null && !customDocName.isEmpty()) {
            return customDocName;
        }
        return docType.replace("_", " ").toLowerCase()
                .replaceFirst(".", (docType.charAt(0) + "").toUpperCase());
    }

    private void addHistory(Student student, String updatedBy, String updatedByName, String remarks) {
        try {
            StudentStatusHistory history = new StudentStatusHistory();
            history.setStudent(student);
            history.setStudentApplicationStatus(student.getApplicationStatus());
            history.setUpdatedBy(updatedBy);
            history.setUpdatedByName(updatedByName);
            history.setUpdatedAt(java.time.LocalDateTime.now());
            history.setRemarks(remarks);
            historyRepo.save(history);
        } catch (Exception e) {
            // Don't fail the main operation if history fails
            System.err.println("Failed to add history: " + e.getMessage());
        }
    }
}
