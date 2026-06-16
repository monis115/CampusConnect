package com.cc.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.cc.modals.DocPerson;
import com.cc.service.DocPersonService;

@RestController
@RequestMapping("/api/doc-person")
public class DocPersonController {

    @Autowired
    private DocPersonService service;

    // Create
    @PostMapping
    public ResponseEntity<DocPerson> create(@RequestBody DocPerson entity) {
        return ResponseEntity.ok(service.create(entity));
    }

    // Read All
    @GetMapping({ "", "/all" })
    public ResponseEntity<List<DocPerson>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

    // Check if new DocPerson can be created
    @GetMapping("/check-availability")
    public ResponseEntity<java.util.Map<String, Object>> checkAvailability() {
        return ResponseEntity.ok(service.checkAvailability());
    }

    // Read by ID
    @GetMapping("/{id}")
    public ResponseEntity<DocPerson> getById(@PathVariable Long id) {
        Optional<DocPerson> entity = service.getById(id);
        return entity.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Read by Username
    @GetMapping("/username/{username}")
    public ResponseEntity<DocPerson> getByUsername(@PathVariable String username) {
        Optional<DocPerson> entity = service.getByUsername(username);
        return entity.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Get One (First Available)
    @GetMapping("/get-one")
    public ResponseEntity<DocPerson> getOne() {
        Optional<DocPerson> entity = service.getOne();
        return entity.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Update
    @PutMapping("/{id}")
    public ResponseEntity<DocPerson> update(@PathVariable Long id, @RequestBody DocPerson entity) {
        DocPerson updated = service.update(id, entity);
        return updated != null ? ResponseEntity.ok(updated) : ResponseEntity.notFound().build();
    }

    // Make Inactive (Soft Delete)
    @PutMapping("/{id}/status/{status}")
    public ResponseEntity<DocPerson> updateStatus(@PathVariable Long id, @PathVariable String status) {
        Optional<DocPerson> optionalEntity = service.getById(id);
        if (optionalEntity.isPresent()) {
            DocPerson entity = optionalEntity.get();
            entity.setStatus(status);
            return ResponseEntity.ok(service.update(id, entity));
        }
        return ResponseEntity.notFound().build();
    }

    // Delete (Hard Delete)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/receive-docs/{studentId}")
    public ResponseEntity<?> receiveDocs(
            @PathVariable Long studentId,
            @RequestBody java.util.Map<String, Object> request) {
        try {
            String receivedDateStr = (String) request.get("receivedDate");
            String docPersonUsername = (String) request.get("docPersonUsername");

            // Document checkboxes
            Boolean received12thMarksheet = (Boolean) request.get("received12thMarksheet");
            Boolean received10thMarksheet = (Boolean) request.get("received10thMarksheet");
            Boolean receivedNeetScorecard = (Boolean) request.get("receivedNeetScorecard");
            Boolean receivedPassport = (Boolean) request.get("receivedPassport");
            Boolean receivedAadharCard = (Boolean) request.get("receivedAadharCard");
            Boolean receivedMedicalReport = (Boolean) request.get("receivedMedicalReport");
            Boolean receivedOtherDocuments = (Boolean) request.get("receivedOtherDocuments");
            String receivedOtherDocumentsRemarks = (String) request.get("receivedOtherDocumentsRemarks");
            String physicalStatus = (String) request.get("physicalStatus");

            if (receivedDateStr == null) {
                return ResponseEntity.badRequest().body("receivedDate is required");
            }

            // Retrieve doc person details
            String docPersonUsername1 = (String) request.get("docPersonUsername");
            if (docPersonUsername1 == null) {
                return ResponseEntity.badRequest().body("docPersonUsername is required");
            }

            java.time.LocalDate receivedDate = java.time.LocalDate.parse(receivedDateStr);
            com.cc.modals.Student updatedStudent = service.receiveDocs(
                    studentId,
                    receivedDate,
                    docPersonUsername1,
                    received12thMarksheet,
                    received10thMarksheet,
                    receivedNeetScorecard,
                    receivedPassport,
                    receivedAadharCard,
                    receivedMedicalReport,
                    receivedOtherDocuments,
                    receivedOtherDocumentsRemarks,
                    physicalStatus);
            return ResponseEntity.ok(updatedStudent);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error receiving docs: " + e.getMessage());
        }
    }

    @PostMapping("/verify-telex-docs/{studentId}")
    public ResponseEntity<?> verifyTelexDocs(
            @PathVariable Long studentId,
            @RequestBody java.util.Map<String, Object> request) {
        try {
            Boolean approved = (Boolean) request.get("approved");
            String comment = (String) request.get("comment");
            String docPersonUsername = (String) request.get("docPersonUsername");

            if (approved == null) {
                return ResponseEntity.badRequest().body("approved status is required");
            }
            if (docPersonUsername == null || docPersonUsername.isEmpty()) {
                return ResponseEntity.badRequest().body("docPersonUsername is required");
            }

            com.cc.modals.Student updatedStudent = service.verifyTelexDocs(studentId, approved, comment,
                    docPersonUsername);
            return ResponseEntity.ok(updatedStudent);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error verifying telex docs: " + e.getMessage());
        }
    }

    @GetMapping("/return-requests/{studentId}")
    public ResponseEntity<?> getDocumentReturnRequest(@PathVariable Long studentId) {
        try {
            return ResponseEntity.ok(service.getDocumentReturnRequest(studentId));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/confirm-collection/{studentId}")
    public ResponseEntity<?> confirmCollection(
            @PathVariable Long studentId,
            @RequestBody java.util.Map<String, String> request) {
        try {
            String docPersonUsername = request.get("docPersonUsername");
            if (docPersonUsername == null || docPersonUsername.isEmpty()) {
                return ResponseEntity.badRequest().body("docPersonUsername is required");
            }
            return ResponseEntity.ok(service.confirmCollection(studentId, docPersonUsername));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error confirming collection: " + e.getMessage());
        }
    }

    @PostMapping("/return-docs/{studentId}")
    public ResponseEntity<?> returnDocs(
            @PathVariable Long studentId,
            @RequestBody java.util.Map<String, Object> request) {
        try {
            String methodStr = (String) request.get("method");
            String trackingId = (String) request.get("trackingId");
            String returnedDateStr = (String) request.get("returnedDate");
            String docPersonUsername = (String) request.get("docPersonUsername");

            if (methodStr == null) {
                return ResponseEntity.badRequest().body("method is required");
            }
            if (returnedDateStr == null) {
                return ResponseEntity.badRequest().body("returnedDate is required");
            }
            if (docPersonUsername == null || docPersonUsername.isEmpty()) {
                return ResponseEntity.badRequest().body("docPersonUsername is required");
            }

            com.cc.modals.DocDeliveryMethod method = com.cc.modals.DocDeliveryMethod.valueOf(methodStr);
            java.time.LocalDate returnedDate = java.time.LocalDate.parse(returnedDateStr);

            com.cc.modals.Student updatedStudent = service.returnDocs(studentId, method, trackingId, returnedDate,
                    docPersonUsername);
            return ResponseEntity.ok(updatedStudent);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error returning docs: " + e.getMessage());
        }
    }

    @GetMapping("/{id}/with-students")
    public ResponseEntity<?> getDocPersonWithStudents(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(service.getDocPersonWithStudents(id));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error fetching doc person details: " + e.getMessage());
        }
    }

    @GetMapping("/stats")
    public ResponseEntity<?> getStats(
            @RequestParam String username,
            @RequestParam(defaultValue = "today") String period) {
        try {
            return ResponseEntity.ok(service.getStats(username, period));
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error fetching stats: " + e.getMessage());
        }
    }

    @PostMapping("/send-report")
    public ResponseEntity<?> sendReport(@RequestBody java.util.Map<String, String> request) {
        try {
            String username = request.get("username");
            String period = request.getOrDefault("period", "Today");
            String email = request.get("email");

            if (username == null || username.isEmpty()) {
                return ResponseEntity.badRequest().body("username is required");
            }
            if (email == null || email.isEmpty()) {
                return ResponseEntity.badRequest().body("email is required");
            }

            service.sendReportEmail(period, username, email);
            return ResponseEntity
                    .ok(java.util.Collections.singletonMap("message", "Report sent successfully to " + email));
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error sending report: " + e.getMessage());
        }
    }
}
