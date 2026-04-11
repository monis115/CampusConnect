package com.cc.controller;

import com.cc.DTO.StudentDTO;
import com.cc.modals.Student;
import com.cc.service.StudentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/students")
public class StudentController {

    @Autowired
    private StudentService studentService;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * Add a new student record WITH FILE UPLOADS.
     * Accepts form-data with JSON student details and 4 file uploads.
     */
    @PostMapping("/add")
    public ResponseEntity<?> addStudent(
            @RequestParam("student") String studentJson,
            @RequestParam(value = "adhaarFile", required = false) MultipartFile adhaarFile,
            @RequestParam(value = "passportFile", required = false) MultipartFile passportFile,
            @RequestParam(value = "marksheetFile", required = false) MultipartFile marksheetFile,
            @RequestParam(value = "neetMarksheetFile", required = false) MultipartFile neetMarksheetFile,
            @RequestParam(value = "profileImage", required = false) MultipartFile profileImage) {

        try {
            System.out.println("LOG [/students/add] Received student JSON: " + studentJson);

            // Parse JSON string to StudentDTO
            StudentDTO studentDTO = objectMapper.readValue(studentJson, StudentDTO.class);

            // Call service with DTO and files
            Student savedStudent = studentService.addStudent(
                    studentDTO,
                    adhaarFile,
                    passportFile,
                    marksheetFile,
                    neetMarksheetFile,
                    profileImage);

            return ResponseEntity.ok(savedStudent);

        } catch (IOException e) {
            System.err.println("ERROR [/students/add] JSON Parsing Error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body("JSON Parsing Error: " + e.getMessage());
        } catch (RuntimeException e) {
            System.err.println("ERROR [/students/add] Validation Error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Validation Error: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("ERROR [/students/add] Unexpected Error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("An unexpected error occurred: " + e.getMessage());
        }
    }

    /**
     * Retrieve all registered students.
     */
    @GetMapping("/all")
    public ResponseEntity<List<Student>> getAllStudents() {
        List<Student> students = studentService.getAllStudents();
        return ResponseEntity.ok(students);
    }

    /**
     * Retrieve all students with pagination.
     * Default size 19 as requested.
     */
    @GetMapping("/paginated")
    public ResponseEntity<org.springframework.data.domain.Page<Student>> getAllStudentsPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "19") int size) {
        org.springframework.data.domain.Page<Student> students = studentService.getAllStudentsPaginated(page, size);
        return ResponseEntity.ok(students);
    }

    /**
     * Retrieve a student by their unique identifier.
     */
    @GetMapping("/{studentId}")
    public ResponseEntity<Student> getStudentById(@PathVariable Long studentId) {
        Student student = studentService.getStudentById(studentId);
        return ResponseEntity.ok(student);
    }

    /**
     * Retrieve all students associated with a specific agent.
     */
    @GetMapping("/agent/{agentId}")
    public ResponseEntity<List<Student>> getStudentsByAgent(@PathVariable Long agentId) {
        List<Student> students = studentService.getStudentsByAgent(agentId);
        return ResponseEntity.ok(students);
    }

    @GetMapping("/admission-letters")
    public ResponseEntity<List<com.cc.DTO.StudentAdmissionDTO>> getStudentsWithAdmissionLetters() {
        List<com.cc.DTO.StudentAdmissionDTO> students = studentService.getStudentsWithAdmissionLetters();
        return ResponseEntity.ok(students);
    }

    /**
     * Upload Admission Letter for a student (by AdmissionManager).
     */
    @PostMapping("/upload-admission-letter/{studentId}")
    public ResponseEntity<?> uploadAdmissionLetter(
            @PathVariable Long studentId,
            @RequestParam("file") MultipartFile file) {

        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body("File is empty");
            }

            // Validate file type (PDF only)
            if (!file.getContentType().equals("application/pdf")) {
                return ResponseEntity.badRequest().body("Only PDF files allowed");
            }

            String fileName = "admission_" + studentId + "_" + UUID.randomUUID() + ".pdf";
            Path uploadPath = Paths.get("uploads/admission-letters");

            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            Path filePath = uploadPath.resolve(fileName);
            Files.copy(file.getInputStream(), filePath);

            Student updatedStudent = studentService.uploadAdmissionLetter(studentId, fileName);
            return ResponseEntity.ok(updatedStudent);

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("Upload Error: " + e.getMessage());
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body("File upload failed: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("An unexpected error occurred: " + e.getMessage());
        }
    }

    @GetMapping("/generated-by-manager-username/{username}")
    public ResponseEntity<?> getStudentsByAdmissionManagerUsername(@PathVariable String username) {
        try {
            List<Student> students = studentService.getStudentsByAdmissionManagerUsername(username);
            return ResponseEntity.ok(students);
        } catch (RuntimeException e) {
            // Handle the case where the manager username is not found
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("An unexpected error occurred: " + e.getMessage());
        }
    }
}