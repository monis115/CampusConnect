package com.cc.service;

import com.cc.DTO.StudentDTO;
import com.cc.modals.*;
import com.cc.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // <-- 1. ADD THIS IMPORT

import java.time.LocalDate;
import java.util.*;

@Service
public class StudentService {

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private AgentRepository agentRepository;

    @Autowired
    private CollegeRepository collegeRepository;

    @Autowired
    private LocationRepository locationRepository;

    // 🔹 Add Student with Validation
    @Transactional // <-- 2. ADD THIS ANNOTATION
    public Student addStudent(StudentDTO dto) {
        
        // Step 1: Fetch Agent
        Agent agent = agentRepository.findByUserId(dto.getUserNameAgent())
                .orElseThrow(() -> new RuntimeException("Agent not found with username: " + dto.getUserNameAgent()));

        // Step 2: Validate Location Access
        Location studentDistrict = locationRepository.findById(dto.getDistrictId().longValue())
                .orElseThrow(() -> new RuntimeException("District not found with ID: " + dto.getDistrictId()));

        boolean hasAccessToDistrict = agent.getAccessibleLocations().stream()
                .anyMatch(loc -> Objects.equals(loc.getId(), studentDistrict.getId()));

        if (!hasAccessToDistrict) {
            throw new RuntimeException("Access denied: Agent does not have permission for district " + studentDistrict.getDistrict());
        }

        // Step 3: Validate College Access
        List<College> assignedColleges = new ArrayList<>();
        
        if (dto.getCollegeIds() == null || dto.getCollegeIds().isEmpty()) {
            throw new RuntimeException("Validation Error: At least one college ID must be provided.");
        }

        for (Long collegeId : dto.getCollegeIds()) {
            College college = collegeRepository.findById(collegeId)
                    .orElseThrow(() -> new RuntimeException("College not found with ID: " + collegeId));

            boolean hasAccessToCollege = agent.getCollegePrivileges().stream()
                    .anyMatch(col -> Objects.equals(col.getId(), college.getId()));

            if (!hasAccessToCollege) {
                throw new RuntimeException("Access denied: Agent does not have permission for college " + college.getCollegeName());
            }

            assignedColleges.add(college);
        }

        // Step 4: Create Student Object
        Student student = new Student();
        student.setFullName(dto.getFullName());
        student.setDob(dto.getDob());
        student.setAge(dto.getAge());
        student.setEmail(dto.getEmail());
        student.setContactNo(dto.getContactNo());
        student.setAddress(dto.getAddress());
        student.setEmergencyContactNo(dto.getEmergencyContactNo());
        student.setEmergencyRelation(dto.getEmergencyRelation());
        student.setPassportNo(dto.getPassportNo());
        student.setPassportValidTill(dto.getPassportValidTill());
        student.setHighestDegree(dto.getHighestDegree());
        student.setMarks(dto.getMarks());
        student.setAppliedFor(dto.getAppliedFor());
        
        student.setCreatedAt(LocalDate.now());
        student.setApplicationStatus(dto.getApplicationStatus() != null ? dto.getApplicationStatus() : "Submitted");
        student.setPresentStatus(StudentStatus.REGISTERED);
        student.setNextStep(StudentStatus.UNDER_REVIEW);

        // Step 5: Assign College & Manager (ManyToOne)
        College primaryCollege = assignedColleges.get(0); 
        student.setCollege(primaryCollege);
        student.setAdmissionManager(primaryCollege.getAdmissionManager());

        // Step 6: Link Agent
        student.setAgent(agent);

        // === THIS IS THE TWO-STEP SAVE FIX ===
        
        // Step 7: Save the Student FIRST (without preferences)
        Student savedStudent = studentRepository.save(student);

        // Step 8: FLUSH the persistence context
        // This forces Hibernate to run the INSERT SQL immediately.
        // 'savedStudent' is now guaranteed to have its database-generated ID.
        studentRepository.flush(); // <-- 3. ADD THIS LINE

        // Step 9: Now set the ManyToMany relationship
        // We set it on the managed entity that has a valid ID.
        savedStudent.setCollegePreferences(assignedColleges);

        // Step 10: Save AGAIN (or let @Transactional handle it)
        // This will now update the join table correctly.
        return studentRepository.save(savedStudent);
    }

    // 🔹 Retrieve All Students
    public List<Student> getAllStudents() {
        return studentRepository.findAll();
    }

    // 🔹 Retrieve By Agent
    public List<Student> getStudentsByAgent(Long agentId) {
        return studentRepository.findByAgent_Id(agentId);
    }
}