package com.cc.controller;

import com.cc.service.ConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;

@RestController
@RequestMapping("/api/config/qrcode")
@CrossOrigin("*")
public class QrCodeController {

    @Autowired
    private ConfigService configService;

    @PostMapping
    public ResponseEntity<?> uploadQrCode(@RequestParam("file") MultipartFile file) {
        try {
            String path = configService.storeQrCode(file);
            return ResponseEntity.ok("QR Code uploaded successfully at: " + path);
        } catch (IOException e) {
            return ResponseEntity.status(500).body("Failed to upload QR Code: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    @PutMapping
    public ResponseEntity<?> updateQrCode(@RequestParam("file") MultipartFile file) {
        // Same logic as POST since it overwrites the file
        return uploadQrCode(file);
    }

    @GetMapping
    public ResponseEntity<Resource> getQrCode() {
        try {
            Resource resource = configService.getQrCodeResource();
            String contentType = "image/png"; // Defaulting to png as per service constant

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);
        } catch (MalformedURLException e) {
            return ResponseEntity.status(500).build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
