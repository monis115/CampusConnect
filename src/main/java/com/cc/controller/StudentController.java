package com.cc.controller;

import com.cc.DTO.StudentDTO;
import com.cc.modals.Student;
import com.cc.service.StudentService;
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
@CrossOrigin(origins = "*")
public class StudentController {

    @Autowired
    private StudentService studentService;

    /**
     * Add a new student record.
     * This endpoint accepts a StudentDTO object and performs all necessary validation,
     * including checking the agent’s access privileges for district and college assignments.
     */
    @PostMapping("/add")
    public ResponseEntity<?> addStudent(@RequestBody StudentDTO studentDTO) {
        try {
            Student savedStudent = studentService.addStudent(studentDTO);
            return ResponseEntity.ok(savedStudent);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("Validation Error: " + e.getMessage());
        } catch (Exception e) {
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

    /**
     * Upload Admission Letter for a student (by AdmissionManager), updating status to ADMISSION_LETTER_GENERATED.
     */
    @PostMapping("/upload-admission-letter/{studentId}")
    public ResponseEntity<?> uploadAdmissionLetter(@PathVariable Long studentId, 
                                                   @RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body("File is empty");
            }
            // Validate file type (e.g., PDF only)
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
    

    /**
     * Delete a student record by ID.
     */
}