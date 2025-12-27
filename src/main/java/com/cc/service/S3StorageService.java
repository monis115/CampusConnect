package com.cc.service;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

/**
 * Service for handling file storage.
 * Supports both S3 (for production) and local filesystem (for development).
 * 
 * When aws.s3.enabled=true, files are uploaded to S3.
 * When aws.s3.enabled=false (default), files are saved to local uploads/
 * directory.
 */
@Service
public class S3StorageService {

    @Autowired(required = false)
    private S3Client s3Client;

    @Autowired(required = false)
    private S3Presigner s3Presigner;

    @Value("${aws.s3.bucket:}")
    private String bucketName;

    @Value("${aws.s3.enabled:false}")
    private boolean s3Enabled;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    private static final String LOCAL_UPLOAD_DIR = "uploads/";

    /**
     * Upload a file to storage (S3 or local filesystem)
     * 
     * @param file       The file to upload
     * @param folderName Subfolder name (e.g., "passport", "admission-letters")
     * @param filePrefix Prefix for the filename
     * @return The storage path (S3 key or local path)
     */
    public String uploadFile(MultipartFile file, String folderName, String filePrefix) {
        if (file == null || file.isEmpty()) {
            return null;
        }

        // Generate unique filename
        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String uniqueFilename = filePrefix + "_" + UUID.randomUUID().toString() + extension;
        String key = folderName + "/" + uniqueFilename;

        if (s3Enabled && s3Client != null) {
            return uploadToS3(file, key);
        } else {
            return uploadToLocal(file, folderName, uniqueFilename);
        }
    }

    /**
     * Upload a file (java.io.File) to storage - for generated PDFs and other files
     * 
     * @param file       The java.io.File to upload
     * @param folderName Subfolder name (e.g., "conditional-offer-letters")
     * @param filePrefix Prefix for the filename
     * @return The storage path (S3 key or local path)
     */
    public String uploadFile(java.io.File file, String folderName, String filePrefix) {
        if (file == null || !file.exists()) {
            return null;
        }

        // Generate unique filename
        String extension = "";
        String name = file.getName();
        if (name.contains(".")) {
            extension = name.substring(name.lastIndexOf("."));
        }
        String uniqueFilename = filePrefix + "_" + UUID.randomUUID().toString() + extension;
        String key = folderName + "/" + uniqueFilename;

        if (s3Enabled && s3Client != null) {
            return uploadFileToS3(file, key);
        } else {
            return uploadFileToLocal(file, folderName, uniqueFilename);
        }
    }

    /**
     * Upload java.io.File to S3
     */
    private String uploadFileToS3(java.io.File file, String key) {
        try {
            String contentType = Files.probeContentType(file.toPath());
            if (contentType == null) {
                contentType = "application/octet-stream";
            }

            PutObjectRequest putRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .contentType(contentType)
                    .build();

            s3Client.putObject(putRequest, RequestBody.fromFile(file));

            return "s3://" + bucketName + "/" + key;
        } catch (IOException e) {
            throw new RuntimeException("Failed to upload file to S3: " + e.getMessage(), e);
        }
    }

    /**
     * Upload java.io.File to local filesystem
     */
    private String uploadFileToLocal(java.io.File file, String folderName, String filename) {
        try {
            String uploadDir = LOCAL_UPLOAD_DIR + folderName;
            Path uploadPath = Paths.get(uploadDir);

            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            Path destPath = uploadPath.resolve(filename);
            Files.copy(file.toPath(), destPath, StandardCopyOption.REPLACE_EXISTING);

            return uploadDir + "/" + filename;
        } catch (IOException e) {
            throw new RuntimeException("Failed to save file locally: " + e.getMessage(), e);
        }
    }

    /**
     * Upload file to S3
     */
    private String uploadToS3(MultipartFile file, String key) {
        try {
            PutObjectRequest putRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .contentType(file.getContentType())
                    .build();

            s3Client.putObject(putRequest, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

            // Return S3 key (path within bucket)
            return "s3://" + bucketName + "/" + key;
        } catch (IOException e) {
            throw new RuntimeException("Failed to upload file to S3: " + e.getMessage(), e);
        }
    }

    /**
     * Upload file to local filesystem (for development)
     */
    private String uploadToLocal(MultipartFile file, String folderName, String filename) {
        try {
            String uploadDir = LOCAL_UPLOAD_DIR + folderName;
            Path uploadPath = Paths.get(uploadDir);

            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            Path filePath = uploadPath.resolve(filename);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // Return local path
            return uploadDir + "/" + filename;
        } catch (IOException e) {
            throw new RuntimeException("Failed to save file locally: " + e.getMessage(), e);
        }
    }

    /**
     * Get the URL to access a file.
     * For S3: generates a presigned URL.
     * For local: returns the relative path.
     * 
     * @param storagePath The storage path returned from uploadFile()
     * @return Accessible URL for the file
     */
    public String getFileUrl(String storagePath) {
        if (storagePath == null) {
            return null;
        }

        if (storagePath.startsWith("s3://")) {
            return generatePresignedUrl(storagePath);
        } else {
            // Local file - return relative URL
            return baseUrl + "/" + storagePath;
        }
    }

    /**
     * Generate a presigned URL for S3 object (valid for 1 hour)
     */
    public String generatePresignedUrl(String storagePath) {
        if (!s3Enabled || s3Presigner == null || storagePath == null) {
            return storagePath; // Return as-is if S3 not enabled
        }

        // Extract key from s3://bucket/key format
        String key = storagePath;
        if (storagePath.startsWith("s3://")) {
            key = storagePath.substring(("s3://" + bucketName + "/").length());
        }

        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();

        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofHours(1))
                .getObjectRequest(getObjectRequest)
                .build();

        URL presignedUrl = s3Presigner.presignGetObject(presignRequest).url();
        return presignedUrl.toString();
    }

    /**
     * Delete a file from storage
     * 
     * @param storagePath The storage path
     */
    public void deleteFile(String storagePath) {
        if (storagePath == null) {
            return;
        }

        if (storagePath.startsWith("s3://") && s3Enabled && s3Client != null) {
            String key = storagePath.substring(("s3://" + bucketName + "/").length());
            DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();
            s3Client.deleteObject(deleteRequest);
        } else {
            // Delete local file
            try {
                Path path = Paths.get(storagePath);
                Files.deleteIfExists(path);
            } catch (IOException e) {
                // Log but don't throw - file might not exist
                System.err.println("Could not delete local file: " + storagePath);
            }
        }
    }

    /**
     * Check if S3 is enabled
     */
    public boolean isS3Enabled() {
        return s3Enabled;
    }

    /**
     * Get the base URL for file access
     */
    public String getBaseUrl() {
        return baseUrl;
    }
}
