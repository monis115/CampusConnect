package com.cc.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/file")
@CrossOrigin("*")
public class FileController {

    private static final String UPLOAD_DIR = "uploads/";

    @PostMapping("/upload")
    public ResponseEntity<?> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "studentId", required = false) String studentId,
            @RequestParam(value = "type", required = false, defaultValue = "general") String type) {

        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body("File is empty");
            }

            // Create directory based on type
            String subDir = getSubDirectory(type);
            Path uploadPath = Paths.get(UPLOAD_DIR + subDir);

            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // Generate unique filename
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }

            String uniqueFilename = (studentId != null ? studentId + "_" : "") +
                    UUID.randomUUID().toString() + extension;

            // Save file
            Path filePath = uploadPath.resolve(uniqueFilename);
            Files.copy(file.getInputStream(), filePath);

            // Return response with file path (include uploads/ prefix for serving)
            Map<String, String> response = new HashMap<>();
            response.put("path", "uploads/" + subDir + "/" + uniqueFilename);
            response.put("filename", uniqueFilename);
            response.put("originalFilename", originalFilename);
            response.put("message", "File uploaded successfully");

            return ResponseEntity.ok(response);

        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error uploading file: " + e.getMessage());
        }
    }

    private String getSubDirectory(String type) {
        switch (type.toLowerCase()) {
            case "visa-payment-confirmation":
                return "visa-payment-confirmations";
            case "telex-payment":
                return "telex-payment-confirmations";
            case "passport":
                return "passport";
            case "marksheet":
                return "marksheet";
            case "profile":
                return "profile";
            case "admission-letter":
                return "admission-letters";
            default:
                return "general";
        }
    }
}
