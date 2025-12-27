package com.cc.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.core.sync.RequestBody;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.cc.modals.Student;
import com.cc.modals.User;
import com.cc.repository.StudentRepository;
import com.cc.repository.UserRepository;

@RestController
@RequestMapping("/api/debug")
public class DebugController {

    @Value("${aws.s3.enabled:false}")
    private boolean s3Enabled;

    @Value("${aws.s3.bucket:unknown}")
    private String bucketName;

    @Value("${AWS_S3_ENABLED:not_found}")
    private String envS3Enabled;

    @Autowired(required = false)
    private S3Client s3Client;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/s3-config")
    public ResponseEntity<Map<String, Object>> getS3Config() {
        Map<String, Object> status = new HashMap<>();
        status.put("prop_aws_s3_enabled", s3Enabled);
        status.put("env_AWS_S3_ENABLED", envS3Enabled);
        status.put("prop_aws_s3_bucket", bucketName);
        status.put("bean_s3Client_exists", s3Client != null);

        // Check ENV vars directly
        status.put("sys_env_AWS_S3_ENABLED", System.getenv("AWS_S3_ENABLED"));
        status.put("sys_env_AWS_ACCESS_KEY_ID_exists", System.getenv("AWS_ACCESS_KEY_ID") != null);
        status.put("sys_env_AWS_SECRET_ACCESS_KEY_exists", System.getenv("AWS_SECRET_ACCESS_KEY") != null);

        return ResponseEntity.ok(status);
    }

    @GetMapping("/users")
    public ResponseEntity<List<Map<String, String>>> getAllUsers() {
        List<User> users = userRepository.findAll();
        List<Map<String, String>> result = new ArrayList<>();

        for (User user : users) {
            Map<String, String> userInfo = new HashMap<>();
            userInfo.put("username", user.getUsername());
            userInfo.put("password", user.getPassword());
            userInfo.put("role", user.getRole());
            result.add(userInfo);
        }

        return ResponseEntity.ok(result);
    }

    @PostMapping("/migrate-to-s3")
    public ResponseEntity<Map<String, Object>> migrateLocalFilesToS3() {
        Map<String, Object> result = new HashMap<>();
        List<String> migrated = new ArrayList<>();
        List<String> errors = new ArrayList<>();

        if (s3Client == null || !s3Enabled) {
            result.put("error", "S3 is not enabled or S3Client is null");
            return ResponseEntity.badRequest().body(result);
        }

        List<Student> students = studentRepository.findAll();
        int migratedCount = 0;

        for (Student student : students) {
            try {
                boolean updated = false;

                // Migrate adhaarPath
                if (student.getAdhaarPath() != null && student.getAdhaarPath().startsWith("uploads/")) {
                    String newPath = migrateFileToS3(student.getAdhaarPath());
                    if (newPath != null) {
                        student.setAdhaarPath(newPath);
                        migrated.add("Student " + student.getId() + " adhaar: " + newPath);
                        updated = true;
                    }
                }

                // Migrate passportPath
                if (student.getPassportPath() != null && student.getPassportPath().startsWith("uploads/")) {
                    String newPath = migrateFileToS3(student.getPassportPath());
                    if (newPath != null) {
                        student.setPassportPath(newPath);
                        migrated.add("Student " + student.getId() + " passport: " + newPath);
                        updated = true;
                    }
                }

                // Migrate markseetPath (note: field name has typo in original entity)
                if (student.getMarkseetPath() != null && student.getMarkseetPath().startsWith("uploads/")) {
                    String newPath = migrateFileToS3(student.getMarkseetPath());
                    if (newPath != null) {
                        student.setMarkseetPath(newPath);
                        migrated.add("Student " + student.getId() + " marksheet: " + newPath);
                        updated = true;
                    }
                }

                // Migrate neetMarksheetPath
                if (student.getNeetMarksheetPath() != null && student.getNeetMarksheetPath().startsWith("uploads/")) {
                    String newPath = migrateFileToS3(student.getNeetMarksheetPath());
                    if (newPath != null) {
                        student.setNeetMarksheetPath(newPath);
                        migrated.add("Student " + student.getId() + " neet_marksheet: " + newPath);
                        updated = true;
                    }
                }

                // Migrate profileImagePath
                if (student.getProfileImagePath() != null && student.getProfileImagePath().startsWith("uploads/")) {
                    String newPath = migrateFileToS3(student.getProfileImagePath());
                    if (newPath != null) {
                        student.setProfileImagePath(newPath);
                        migrated.add("Student " + student.getId() + " profile: " + newPath);
                        updated = true;
                    }
                }

                if (updated) {
                    studentRepository.save(student);
                    migratedCount++;
                }

            } catch (Exception e) {
                errors.add("Student " + student.getId() + ": " + e.getMessage());
            }
        }

        result.put("totalStudents", students.size());
        result.put("migratedStudents", migratedCount);
        result.put("migratedFiles", migrated);
        result.put("errors", errors);

        return ResponseEntity.ok(result);
    }

    private String migrateFileToS3(String localPath) {
        try {
            // Try multiple possible paths (relative and EC2 absolute)
            Path filePath = Paths.get(localPath);
            File file = filePath.toFile();

            // If not found, try EC2 absolute path
            if (!file.exists()) {
                filePath = Paths.get("/var/app/current/" + localPath);
                file = filePath.toFile();
            }

            if (!file.exists()) {
                System.err.println("File not found: " + localPath);
                return null; // File doesn't exist, skip
            }

            // Extract folder and filename from path like "uploads/adhaar/filename.pdf"
            String[] parts = localPath.split("/");
            if (parts.length < 3)
                return null;

            String folder = parts[1]; // e.g., "adhaar"
            String filename = parts[parts.length - 1];
            String s3Key = folder + "/" + filename;

            // Upload to S3
            PutObjectRequest putRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(s3Key)
                    .build();

            s3Client.putObject(putRequest, RequestBody.fromFile(file));

            return "s3://" + bucketName + "/" + s3Key;

        } catch (Exception e) {
            System.err.println("Error migrating file " + localPath + ": " + e.getMessage());
            return null;
        }
    }
}
