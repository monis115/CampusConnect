package com.cc.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.cc.modals.TelexMan;
import com.cc.service.TelexManService;

@RestController
@RequestMapping("/api/telex-man")
public class TelexManController {

    @Autowired
    private TelexManService service;

    // Create
    @PostMapping
    public ResponseEntity<TelexMan> create(@RequestBody TelexMan entity) {
        return ResponseEntity.ok(service.create(entity));
    }

    // Read All
    @GetMapping
    public ResponseEntity<List<TelexMan>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

    // Get One Username (First Available)
    @GetMapping("/username-only")
    public ResponseEntity<java.util.Map<String, String>> getOneUsername() {
        return service.getAll().stream()
                .map(TelexMan::getUsername)
                .findFirst()
                .map(username -> {
                    java.util.Map<String, String> response = new java.util.HashMap<>();
                    response.put("username", username);
                    return ResponseEntity.ok(response);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // Read by ID
    @GetMapping("/{id}")
    public ResponseEntity<TelexMan> getById(@PathVariable Long id) {
        Optional<TelexMan> entity = service.getById(id);
        return entity.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Read by Username
    @GetMapping("/username/{username}")
    public ResponseEntity<TelexMan> getByUsername(@PathVariable String username) {
        Optional<TelexMan> entity = service.getByUsername(username);
        return entity.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Get One (First Available)
    @GetMapping("/get-one")
    public ResponseEntity<TelexMan> getOne() {
        Optional<TelexMan> entity = service.getOne();
        return entity.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Update
    @PutMapping("/{id}")
    public ResponseEntity<TelexMan> update(@PathVariable Long id, @RequestBody TelexMan entity) {
        TelexMan updated = service.update(id, entity);
        return updated != null ? ResponseEntity.ok(updated) : ResponseEntity.notFound().build();
    }

    // Make Inactive (Soft Delete)
    @PutMapping("/{id}/status/{status}")
    public ResponseEntity<TelexMan> updateStatus(@PathVariable Long id, @PathVariable String status) {
        Optional<TelexMan> optionalEntity = service.getById(id);
        if (optionalEntity.isPresent()) {
            TelexMan entity = optionalEntity.get();
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

    @PostMapping("/submit-to-university/{studentId}")
    public ResponseEntity<?> submitToUniversity(
            @PathVariable Long studentId,
            @RequestBody java.util.Map<String, String> request) {
        try {
            String telexManUsername = request.get("telexManUsername");
            if (telexManUsername == null || telexManUsername.isEmpty()) {
                return ResponseEntity.badRequest().body("telexManUsername is required");
            }
            return ResponseEntity.ok(service.submitToUniversity(studentId, telexManUsername));
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error submitting to university: " + e.getMessage());
        }
    }

    @PostMapping("/submit-to-mea/{studentId}")
    public ResponseEntity<?> submitToMEA(
            @PathVariable Long studentId,
            @RequestBody java.util.Map<String, String> request) {
        try {
            String telexManUsername = request.get("telexManUsername");
            if (telexManUsername == null || telexManUsername.isEmpty()) {
                return ResponseEntity.badRequest().body("telexManUsername is required");
            }
            return ResponseEntity.ok(service.submitToMEA(studentId, telexManUsername));
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error submitting to MEA: " + e.getMessage());
        }
    }

    @PostMapping("/issue-telex-number/{studentId}")
    public ResponseEntity<?> issueTelexNumber(
            @PathVariable Long studentId,
            @RequestBody java.util.Map<String, String> request) {
        try {
            String telexNumber = request.get("telexNumber");
            String issueDateStr = request.get("issueDate");
            String telexManUsername = request.get("telexManUsername");

            if (telexNumber == null || telexNumber.isEmpty()) {
                return ResponseEntity.badRequest().body("telexNumber is required");
            }
            if (issueDateStr == null || issueDateStr.isEmpty()) {
                return ResponseEntity.badRequest().body("issueDate is required");
            }
            if (telexManUsername == null || telexManUsername.isEmpty()) {
                return ResponseEntity.badRequest().body("telexManUsername is required");
            }

            java.time.LocalDate issueDate = java.time.LocalDate.parse(issueDateStr);
            return ResponseEntity.ok(service.issueTelexNumber(studentId, telexNumber, issueDate, telexManUsername));
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error issuing telex number: " + e.getMessage());
        }
    }
}
