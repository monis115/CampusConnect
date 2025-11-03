package com.cc.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cc.DTO.AddmissionManagerCreateDTO;
import com.cc.DTO.UpdateAdmissionManagerDTO;
import com.cc.modals.AddmissionManager;
import com.cc.service.AddmissionManagerService;

@RestController
@RequestMapping("/api/managers")
@CrossOrigin("*")
public class AddmissionManagerController {

    @Autowired
    private AddmissionManagerService addmissionManagerService;

    // Create Admission Manager (send DTO in body)
    @PostMapping("/create")
    public ResponseEntity<?> createManager(@RequestBody AddmissionManagerCreateDTO dto) {
        AddmissionManager manager = new AddmissionManager();
        manager.setFullName(dto.getFullName());
        manager.setEmail(dto.getEmail());
        manager.setContactNumber(dto.getContactNumber());
        // role and status default are already set in entity

        AddmissionManager saved = addmissionManagerService.createAddmissionManager(manager, dto.getCollegeIds());

        // plain password per requirement is the phone number
        String plainPassword = dto.getContactNumber();

        // return minimal response with generated username and plain password (so caller can communicate them)
        Map<String, Object> resp = new HashMap<>();
        resp.put("id", saved.getId());
        resp.put("username", saved.getUsername());
        resp.put("password", plainPassword);
        resp.put("fullName", saved.getFullName());
        resp.put("email", saved.getEmail());
        resp.put("contactNumber", saved.getContactNumber());
        resp.put("status", saved.getStatus());
        resp.put("role", saved.getRole());

        return ResponseEntity.ok(resp);
    }

    // Other endpoints retained from earlier example
    @GetMapping("/all")
    public ResponseEntity<List<AddmissionManager>> getAllManagers() {
        return ResponseEntity.ok(addmissionManagerService.getAllManagers());
    }

    @GetMapping("/{id}")
    public ResponseEntity<AddmissionManager> getManagerById(@PathVariable Long id) {
        AddmissionManager manager = addmissionManagerService.getManagerById(id);
        return manager != null ? ResponseEntity.ok(manager) : ResponseEntity.notFound().build();
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<AddmissionManager> updateManager(
            @PathVariable Long id,
            @RequestBody UpdateAdmissionManagerDTO dto) {  // Use DTO for body (no @RequestParam)
        AddmissionManager updatedManager = new AddmissionManager();
        updatedManager.setFullName(dto.getFullName());
        updatedManager.setEmail(dto.getEmail());
        updatedManager.setContactNumber(dto.getContactNumber());
        updatedManager.setRole(dto.getRole());
        updatedManager.setStatus(dto.getStatus());
        // username/password unchanged (not in DTO)

        AddmissionManager updated = addmissionManagerService.updateManager(id, updatedManager, dto.getCollegeIds());
        return updated != null ? ResponseEntity.ok(updated) : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteManager(@PathVariable Long id) {
        addmissionManagerService.deleteManager(id);
        return ResponseEntity.noContent().build();
    }
}
