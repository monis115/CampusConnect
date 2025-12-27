package com.cc.controller;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.cc.service.S3StorageService;

@RestController
@RequestMapping("/api/file")
public class FileController {

    private static final String UPLOAD_DIR = "uploads/";

    @Autowired
    private S3StorageService s3StorageService;

    /**
     * Serve a file - handles both S3 paths and local paths.
     * For S3 paths: redirects to presigned URL
     * For local paths: serves the file directly
     * 
     * @param path The file path (can be s3://... or uploads/...)
     */
    @GetMapping("/serve")
    public ResponseEntity<?> serveFile(@RequestParam("path") String path) {
        if (path == null || path.isEmpty()) {
            return ResponseEntity.badRequest().body("Path is required");
        }

        try {
            if (path.startsWith("s3://")) {
                // S3 file - redirect to presigned URL
                String presignedUrl = s3StorageService.generatePresignedUrl(path);
                return ResponseEntity.status(HttpStatus.FOUND)
                        .location(URI.create(presignedUrl))
                        .build();
            } else {
                // Local file - serve directly
                Path filePath = Paths.get(path);

                // Also try with /var/app/current/ prefix for EC2
                if (!Files.exists(filePath)) {
                    filePath = Paths.get("/var/app/current/" + path);
                }

                if (!Files.exists(filePath)) {
                    return ResponseEntity.notFound().build();
                }

                Resource resource = new UrlResource(filePath.toUri());
                String contentType = Files.probeContentType(filePath);
                if (contentType == null) {
                    contentType = "application/octet-stream";
                }

                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(contentType))
                        .header(HttpHeaders.CONTENT_DISPOSITION,
                                "inline; filename=\"" + filePath.getFileName().toString() + "\"")
                        .body(resource);
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error serving file: " + e.getMessage());
        }
    }

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
