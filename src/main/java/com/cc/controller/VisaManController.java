package com.cc.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.cc.modals.VisaMan;
import com.cc.service.VisaManService;

@RestController
@RequestMapping("/api/visa-man")
@CrossOrigin("*")
public class VisaManController {

    @Autowired
    private VisaManService service;

    // Create
    @PostMapping
    public ResponseEntity<VisaMan> create(@RequestBody VisaMan entity) {
        return ResponseEntity.ok(service.create(entity));
    }

    // Read All
    @GetMapping
    public ResponseEntity<List<VisaMan>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

    // Get One Username (First Available)
    @GetMapping("/username-only")
    public ResponseEntity<java.util.Map<String, String>> getOneUsername() {
        return service.getAll().stream()
                .map(VisaMan::getUsername)
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
    public ResponseEntity<VisaMan> getById(@PathVariable Long id) {
        Optional<VisaMan> entity = service.getById(id);
        return entity.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Read by Username
    @GetMapping("/username/{username}")
    public ResponseEntity<VisaMan> getByUsername(@PathVariable String username) {
        Optional<VisaMan> entity = service.getByUsername(username);
        return entity.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Get One (First Available)
    @GetMapping("/get-one")
    public ResponseEntity<VisaMan> getOne() {
        Optional<VisaMan> entity = service.getOne();
        return entity.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Update
    @PutMapping("/{id}")
    public ResponseEntity<VisaMan> update(@PathVariable Long id, @RequestBody VisaMan entity) {
        VisaMan updated = service.update(id, entity);
        return updated != null ? ResponseEntity.ok(updated) : ResponseEntity.notFound().build();
    }

    // Make Inactive (Soft Delete)
    @PutMapping("/{id}/status/{status}")
    public ResponseEntity<VisaMan> updateStatus(@PathVariable Long id, @PathVariable String status) {
        Optional<VisaMan> optionalEntity = service.getById(id);
        if (optionalEntity.isPresent()) {
            VisaMan entity = optionalEntity.get();
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

    @PostMapping("/submit-passport/{studentId}")
    public ResponseEntity<?> submitPassport(
            @PathVariable Long studentId,
            @RequestBody java.util.Map<String, String> request) {
        try {
            String visaManUsername = request.get("visaManUsername");
            if (visaManUsername == null || visaManUsername.isEmpty()) {
                return ResponseEntity.badRequest().body("visaManUsername is required");
            }
            return ResponseEntity.ok(service.submitPassport(studentId, visaManUsername));
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error submitting passport: " + e.getMessage());
        }
    }

    @PostMapping("/stamp-visa/{studentId}")
    public ResponseEntity<?> stampVisa(
            @PathVariable Long studentId,
            @RequestBody java.util.Map<String, String> request) {
        try {
            String visaManUsername = request.get("visaManUsername");
            if (visaManUsername == null || visaManUsername.isEmpty()) {
                return ResponseEntity.badRequest().body("visaManUsername is required");
            }
            return ResponseEntity.ok(service.stampVisa(studentId, visaManUsername));
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error stamping visa: " + e.getMessage());
        }
    }
}
