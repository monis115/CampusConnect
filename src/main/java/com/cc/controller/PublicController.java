package com.cc.controller;

import com.cc.DTO.StudentApplicationTrackingDTO;
import com.cc.service.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/public")
public class PublicController {

    @Autowired
    private StudentService studentService;

    @GetMapping("/track-application")
    public ResponseEntity<?> trackApplication(@RequestParam("query") String query) {
        try {
            StudentApplicationTrackingDTO result = studentService.trackApplication(query);
            if (result == null) {
                return ResponseEntity.status(404)
                        .body("No application found with the provided Application ID or Email.");
            }
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("An error occurred while tracking the application: " + e.getMessage());
        }
    }
}
