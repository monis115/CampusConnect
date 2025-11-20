package com.cc.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.cc.modals.Accountant;
import com.cc.service.AccountantService;

@RestController
@RequestMapping("/api/accountant")
@CrossOrigin("*")
public class AccountantController {

    @Autowired
    private AccountantService service;

    // Create
    @PostMapping
    public ResponseEntity<Accountant> create(@RequestBody Accountant entity) {
        return ResponseEntity.ok(service.create(entity));
    }

    // Read All
    @GetMapping
    public ResponseEntity<List<Accountant>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

    // Read by ID
    @GetMapping("/{id}")
    public ResponseEntity<Accountant> getById(@PathVariable Long id) {
        Optional<Accountant> entity = service.getById(id);
        return entity.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Read by Username
    @GetMapping("/username/{username}")
    public ResponseEntity<Accountant> getByUsername(@PathVariable String username) {
        Optional<Accountant> entity = service.getByUsername(username);
        return entity.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Update
    @PutMapping("/{id}")
    public ResponseEntity<Accountant> update(@PathVariable Long id, @RequestBody Accountant entity) {
        Accountant updated = service.update(id, entity);
        return updated != null ? ResponseEntity.ok(updated) : ResponseEntity.notFound().build();
    }

    // Make Inactive (Soft Delete)
    @PutMapping("/{id}/status/{status}")
    public ResponseEntity<Accountant> updateStatus(@PathVariable Long id, @PathVariable String status) {
        Optional<Accountant> optionalEntity = service.getById(id);
        if (optionalEntity.isPresent()) {
            Accountant entity = optionalEntity.get();
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
}
