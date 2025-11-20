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
@CrossOrigin("*")
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
}
