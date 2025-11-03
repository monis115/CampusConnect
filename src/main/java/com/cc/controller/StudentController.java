package com.cc.controller;

import com.cc.DTO.StudentDTO;
import com.cc.modals.Student;
import com.cc.modals.StudentStatus;
import com.cc.service.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
 

    /**
     * Retrieve all students associated with a specific agent.
     */
    @GetMapping("/agent/{agentId}")
    public ResponseEntity<List<Student>> getStudentsByAgent(@PathVariable Long agentId) {
        List<Student> students = studentService.getStudentsByAgent(agentId);
        return ResponseEntity.ok(students);
    }



    /**
     * Delete a student record by ID.
     */
  
}
