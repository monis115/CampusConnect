package com.cc.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Paths;

import com.cc.modals.CompanyLogo;
import com.cc.repository.CompanyLogoRepository;

@Service // <-- ADD THIS: Makes it a Spring-managed bean
public class ConfigService {

    @Autowired
    private CompanyLogoRepository companyLogoRepository;

    // New method: Call this once (e.g., via Postman) to store the logo
    @Transactional
    public CompanyLogo storeLogo(MultipartFile logoFile) throws IOException {
        if (logoFile.getSize() > 5 * 1024 * 1024) { // 5MB limit
            throw new RuntimeException("File too large: Max 5MB");
        }
        try {
            CompanyLogo logo = new CompanyLogo(
                    "RARE Education Logo",
                    logoFile.getBytes(),
                    logoFile.getContentType());
            return companyLogoRepository.save(logo);
        } catch (Exception e) {
            throw new RuntimeException("Failed to store logo: " + e.getMessage());
        }
    }

    public void exportLogoToFile() throws IOException {
        CompanyLogo logo = companyLogoRepository.findByName("RARE Education Logo");
        if (logo != null && logo.getLogoData() != null) {
            Files.write(Paths.get("logo.png"), logo.getLogoData());
            System.out.println("Logo exported to logo.png in project root!");
        } else {
            System.out.println("No logo found in DB.");
        }
    }
    // ═══════════════════════════════════════════════════════════════════════════
    // QR CODE MANAGEMENT
    // ═══════════════════════════════════════════════════════════════════════════

    private static final String QR_CODE_DIR = "uploads/config";
    private static final String QR_CODE_FILENAME = "confirmation_qr.png";

    public String storeQrCode(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new RuntimeException("File is empty");
        }

        Path uploadPath = Paths.get(QR_CODE_DIR);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        Path filePath = uploadPath.resolve(QR_CODE_FILENAME);
        Files.copy(file.getInputStream(), filePath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);

        return filePath.toString();
    }

    @SuppressWarnings("null")
    public org.springframework.core.io.Resource getQrCodeResource() throws java.net.MalformedURLException {
        Path filePath = Paths.get(QR_CODE_DIR).resolve(QR_CODE_FILENAME);
        if (!Files.exists(filePath)) {
            throw new RuntimeException("QR Code not found");
        }
        return new org.springframework.core.io.UrlResource(filePath.toUri());
    }
}