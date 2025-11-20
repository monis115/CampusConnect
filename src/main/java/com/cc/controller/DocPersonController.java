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
@CrossOrigin("*")
public class DocPersonController {

    @Autowired
    private DocPersonService service;

    // Create
    @PostMapping
    public ResponseEntity<DocPerson> create(@RequestBody DocPerson entity) {
        return ResponseEntity.ok(service.create(entity));
    }

    // Read All
    @GetMapping
    public ResponseEntity<List<DocPerson>> getAll() {
        return ResponseEntity.ok(service.getAll());
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
}
