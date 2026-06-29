package com.cc.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.cc.modals.AdditionalDocRequest;
import com.cc.modals.Notification;
import com.cc.modals.Student;
import com.cc.modals.StudentStatusHistory;
import com.cc.repository.AdditionalDocRequestRepository;
import com.cc.repository.NotificationRepository;
import com.cc.repository.StudentRepository;
import com.cc.repository.StudentStatusHistoryRepository;
import com.cc.service.S3StorageService;
import com.cc.service.UserService;

@RestController
@RequestMapping("/api/additional-docs")
public class AdditionalDocRequestController {

    private final AdditionalDocRequestRepository reqRepo;
    private final StudentRepository studentRepo;
    private final StudentStatusHistoryRepository historyRepo;
    private final NotificationRepository notifRepo;
    private final S3StorageService storageService;
    private final UserService userService;

    public AdditionalDocRequestController(
            AdditionalDocRequestRepository reqRepo,
            StudentRepository studentRepo,
            StudentStatusHistoryRepository historyRepo,
            NotificationRepository notifRepo,
            S3StorageService storageService,
            UserService userService) {
        this.reqRepo = reqRepo;
        this.studentRepo = studentRepo;
        this.historyRepo = historyRepo;
        this.notifRepo = notifRepo;
        this.storageService = storageService;
        this.userService = userService;
    }

    // ----------------- UNI PERSON: Create Request -----------------

    @PostMapping("/request")
    public ResponseEntity<?> requestAdditionalDocument(@RequestBody Map<String, Object> payload) {
        try {
            Long studentId = Long.valueOf(payload.get("studentId").toString());
            String documentName = payload.get("documentName").toString();
            String requestedBy = payload.get("requestedBy").toString(); // Uni Person Username
            
            Student student = studentRepo.findById(studentId)
                    .orElseThrow(() -> new RuntimeException("Student not found"));

            String agentUsername = student.getAgent() != null ? student.getAgent().getUserId() : null;
            if (agentUsername == null) {
                return ResponseEntity.badRequest().body("Student does not have an assigned agent.");
            }

            String requestedByName = requestedBy;
            try {
                requestedByName = userService.resolveFullName(requestedBy);
            } catch (Exception e) {}

            AdditionalDocRequest req = new AdditionalDocRequest();
            req.setStudent(student);
            req.setDocumentName(documentName);
            req.setStatus("PENDING");
            req.setRequestedBy(requestedBy);
            req.setRequestedByName(requestedByName);
            req.setAssignedToAgent(agentUsername);
            req.setRemarks(payload.containsKey("remarks") ? payload.get("remarks").toString() : null);

            AdditionalDocRequest saved = reqRepo.save(req);

            // Log History
            StudentStatusHistory history = new StudentStatusHistory();
            history.setStudent(student);
            history.setStudentApplicationStatus(student.getApplicationStatus());
            history.setUpdatedBy(requestedBy);
            history.setUpdatedByName(requestedByName);
            history.setRemarks("Requested Additional Document: " + documentName);
            history.setUpdatedAt(java.time.LocalDateTime.now());
            historyRepo.save(history);

            // Notify Agent
            Notification agentNotif = new Notification();
            agentNotif.setTargetUsername(agentUsername);
            agentNotif.setSenderUsername("SYSTEM");
            agentNotif.setHeading("Additional Document Requested");
            agentNotif.setContent("University Coordinator requested document: " + documentName + " for " + student.getFullName());
            notifRepo.save(agentNotif);

            // Notify Admission Manager
            if (student.getAdmissionManager() != null) {
                Notification amNotif = new Notification();
                amNotif.setTargetUsername(student.getAdmissionManager().getUsername());
                amNotif.setSenderUsername("SYSTEM");
                amNotif.setHeading("Additional Document Requested");
                amNotif.setContent("University Coordinator requested document: " + documentName + " for " + student.getFullName());
                notifRepo.save(amNotif);
            }

            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    // ----------------- GET BY STUDENT -----------------

    @GetMapping("/student/{studentId}")
    public ResponseEntity<?> getRequestsByStudent(@PathVariable Long studentId) {
        try {
            return ResponseEntity.ok(reqRepo.findByStudentIdOrderByCreatedAtDesc(studentId));
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }

    // ----------------- GET BY AGENT -----------------

    @GetMapping("/agent/{agentUsername}")
    public ResponseEntity<?> getRequestsByAgent(@PathVariable String agentUsername) {
        try {
            return ResponseEntity.ok(reqRepo.findByAssignedToAgentOrderByCreatedAtDesc(agentUsername));
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }
    
    @GetMapping("/agent/{agentUsername}/pending")
    public ResponseEntity<?> getPendingRequestsByAgent(@PathVariable String agentUsername) {
        try {
            return ResponseEntity.ok(reqRepo.findByAssignedToAgentAndStatusOrderByCreatedAtDesc(agentUsername, "PENDING"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }

    // ----------------- AGENT: Upload Document -----------------

    @PostMapping("/{id}/upload")
    public ResponseEntity<?> uploadDocument(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file,
            @RequestParam("uploadedBy") String uploadedBy) {
        try {
            AdditionalDocRequest req = reqRepo.findById(id)
                    .orElseThrow(() -> new RuntimeException("Request not found"));

            String folderName = "additional-docs/" + req.getStudent().getId();
            String filePath = storageService.uploadFile(file, folderName, "additional-doc");

            req.setUploadedDocPath(filePath);
            req.setUploadedBy(uploadedBy);
            req.setStatus("UPLOADED");
            AdditionalDocRequest updated = reqRepo.save(req);

            String uploaderName = uploadedBy;
            try {
                uploaderName = userService.resolveFullName(uploadedBy);
            } catch (Exception e) {}

            // Log History
            StudentStatusHistory history = new StudentStatusHistory();
            history.setStudent(req.getStudent());
            history.setStudentApplicationStatus(req.getStudent().getApplicationStatus());
            history.setUpdatedBy(uploadedBy);
            history.setUpdatedByName(uploaderName);
            history.setRemarks("Uploaded Additional Document: " + req.getDocumentName());
            history.setUpdatedAt(java.time.LocalDateTime.now());
            historyRepo.save(history);

            // Notify University Coordinator
            Notification uniNotif = new Notification();
            uniNotif.setTargetUsername(req.getRequestedBy());
            uniNotif.setSenderUsername("SYSTEM");
            uniNotif.setHeading("Additional Document Uploaded");
            uniNotif.setContent("Agent uploaded document: " + req.getDocumentName() + " for " + req.getStudent().getFullName());
            notifRepo.save(uniNotif);
            
            // Notify Admission Manager
            if (req.getStudent().getAdmissionManager() != null) {
                Notification amNotif = new Notification();
                amNotif.setTargetUsername(req.getStudent().getAdmissionManager().getUsername());
                amNotif.setSenderUsername("SYSTEM");
                amNotif.setHeading("Additional Document Uploaded");
                amNotif.setContent("Agent uploaded document: " + req.getDocumentName() + " for " + req.getStudent().getFullName());
                notifRepo.save(amNotif);
            }

            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
}
