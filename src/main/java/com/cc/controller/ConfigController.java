package com.cc.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.cc.modals.CompanyLogo;
import com.cc.service.ConfigService;

@RestController
@RequestMapping("/config")  // Base path for config endpoints; adjust if needed (e.g., "/api/config")
public class ConfigController {

    @Autowired
    private ConfigService configService;

    @PostMapping("/upload-logo")
    public ResponseEntity<?> uploadLogo(@RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body("File is empty");
            }
            if (!file.getContentType().startsWith("image/")) {
                return ResponseEntity.badRequest().body("Only image files allowed");
            }
            CompanyLogo savedLogo = configService.storeLogo(file);
            return ResponseEntity.ok("Logo stored with ID: " + savedLogo.getId());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Upload failed: " + e.getMessage());
        }
    }
    
    
    @GetMapping("/export-logo")  // GET http://localhost:8080/config/export-logo
    public ResponseEntity<?> exportLogo() {
        try {
            configService.exportLogoToFile();
            return ResponseEntity.ok("Logo exported to logo.png!");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Export failed: " + e.getMessage());
        }
    }
}