package com.cc.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cc.modals.Student;
import com.cc.modals.VisaMan;
import com.cc.modals.Agent;
import com.cc.modals.College;

@RestController
@RequestMapping("/api/test2")
public class DummyDataController2 {

    @Autowired
    private com.cc.repository.StudentRepository studentRepository;

    @Autowired
    private com.cc.repository.VisaManRepository visaManRepository;

    @Autowired
    private com.cc.repository.AgentRepository agentRepository;

    @Autowired
    private com.cc.repository.CollegeRepository collegeRepository;

    @GetMapping("/create-students")
    public ResponseEntity<?> createTestStudents() {
        VisaMan visaPerson = visaManRepository.findByUsername("VM_001").orElse(null);
        if (visaPerson == null) {
            return ResponseEntity.badRequest().body("VM_001 not found");
        }
        
        Agent agent = agentRepository.findByUserId("R1125339275").orElse(null);
        if (agent == null) {
            return ResponseEntity.badRequest().body("Agent R1125339275 not found");
        }
        
        College college = collegeRepository.findById(1L).orElse(null);
        if (college == null) {
            return ResponseEntity.badRequest().body("College ID 1 not found");
        }

        for (int i = 0; i < 10; i++) {
            Student s = new Student();
            s.setFullName("Agent Test Student " + i);
            s.setApplicationId("TEST_APP_AGENT_" + System.currentTimeMillis() + "_" + i);
            s.setEmail("test_agent_" + i + "_" + System.currentTimeMillis() + "@example.com");
            s.setDob(java.time.LocalDate.of(2000, 1, 1));
            s.setAge(26);
            s.setCreatedAt(java.time.LocalDate.now());
            s.setContactNo("1234567890");
            s.setAddress("Test Address");
            s.setEmergencyContactNo("0987654321");
            s.setEmergencyRelation("Father");
            s.setIntake("Fall 2026");
            s.setMothersName("Test Mother");
            s.setFathersName("Test Father");
            s.setAdhaarCardNo("123456781234");
            s.setClass12thPassingYear("2018");
            s.setBaords("CBSE");
            s.setClass12thMarks(85.0);
            s.setMaxMarks(100.0);
            s.setHasGivenNEET(true);
            s.setAppliedFor("MBBS");
            
            s.setAction(com.cc.modals.action.REQUEST_FOR_VISA_PAYMENT_TO_ACCOUNTANT);
            s.setActionOnUser(com.cc.modals.actionOnUser.visa_manager);
            s.setApplicationStatus(com.cc.modals.StudentStatus.VISA_FEE_PENDING);
            s.setPaymentStatus("Pending");
            
            s.setVisaMan(visaPerson);
            s.setAgent(agent);
            s.setCollege(college);
            
            studentRepository.save(s);
        }
        return ResponseEntity.ok("Created 10 test students assigned to VM_001 and Agent R1125339275 in VISA_FEE_PENDING state.");
    }
}
