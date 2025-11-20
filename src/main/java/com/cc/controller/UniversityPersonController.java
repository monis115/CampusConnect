package com.cc.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.cc.DTO.UniversityPersonRequest;
import com.cc.modals.UniversityPerson;
import com.cc.service.UniversityPersonService;

@RestController
@RequestMapping("/api/universityPerson")
public class UniversityPersonController {

    private final UniversityPersonService service;

    public UniversityPersonController(UniversityPersonService service) {
        this.service = service;
    }

    // -------------------- CREATE --------------------

    @PostMapping
    public ResponseEntity<UniversityPerson> create(@RequestBody UniversityPersonRequest request) {
        return ResponseEntity.ok(service.create(request));
    }

    // -------------------- UPDATE --------------------

    @PutMapping("/{id}")
    public ResponseEntity<UniversityPerson> update(
            @PathVariable Long id,
            @RequestBody UniversityPersonRequest request) {

        return ResponseEntity.ok(service.update(id, request));
    }

    // -------------------- READ --------------------

    @GetMapping("/{id}")
    public ResponseEntity<UniversityPerson> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @GetMapping
    public ResponseEntity<List<UniversityPerson>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

    // -------------------- DELETE --------------------

    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.ok("Deleted successfully");
    }

    @GetMapping("/check-username")
    public ResponseEntity<Boolean> checkUserName(@RequestParam String userName) {
        boolean available = service.isUserNameAvailable(userName);
        return ResponseEntity.ok(available);
    }

    // -------------------- ADMISSION LETTER UPLOAD --------------------

    @PostMapping("/upload-admission-letter/{studentId}")
    public ResponseEntity<?> uploadAdmissionLetter(
            @PathVariable Long studentId,
            @RequestParam("file") org.springframework.web.multipart.MultipartFile file,
            @RequestParam("universityPersonUsername") String universityPersonUsername) {

        try {
            com.cc.modals.Student updatedStudent = service.uploadAdmissionLetter(studentId, universityPersonUsername,
                    file);
            return ResponseEntity.ok(updatedStudent);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error uploading admission letter: " + e.getMessage());
        }
    }

}
