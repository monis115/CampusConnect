package com.cc.controller;

import com.cc.DTO.UniversityCoordinatorDTO;
import com.cc.DTO.CollegeWithUniversityPersonDTO;
import com.cc.modals.College;
import com.cc.modals.UniversityPerson;
import com.cc.service.CollegeService;
import com.cc.repository.UniversityPersonRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/colleges")
public class CollegeController {

    @Autowired
    private CollegeService collegeService;

    @GetMapping
    public List<College> getAllColleges() {
        return collegeService.getAllColleges();
    }

    @GetMapping("/{id}")
    public Optional<College> getCollegeById(@PathVariable Long id) {
        return collegeService.getCollegeById(id);
    }

    @PostMapping
    public College createCollege(@RequestBody College college) {
        return collegeService.createCollege(college);
    }

    @PutMapping("/{id}")
    public College updateCollege(@PathVariable Long id, @RequestBody College college) {
        return collegeService.updateCollege(id, college);
    }

    @DeleteMapping("/{id}")
    public String deleteCollege(@PathVariable Long id) {
        collegeService.deleteCollege(id);
        return "College deleted successfully with ID: " + id;
    }

    @GetMapping("/search/{collegeName}")
    public College getCollegeByName(@PathVariable String collegeName) {
        return collegeService.findByCollegeName(collegeName);
    }

    @GetMapping("/{id}/university-coordinator")
    public ResponseEntity<?> getUniversityCoordinator(@PathVariable Long id) {
        UniversityCoordinatorDTO coordinator = collegeService.getUniversityCoordinator(id);
        if (coordinator == null) {
            return ResponseEntity.ok(Map.of(
                    "available", true,
                    "message", "No University Coordinator assigned for this college"));
        }
        // Check if coordinator is active
        boolean isActive = "active".equalsIgnoreCase(coordinator.getStatus());
        return ResponseEntity.ok(Map.of(
                "available", !isActive,
                "coordinator", coordinator));
    }

    @Autowired
    private UniversityPersonRepository universityPersonRepository;

    /**
     * Get all colleges with their assigned UniversityPerson (coordinator)
     * Frontend-friendly: shows which colleges have coordinators and which don't
     */
    @GetMapping("/with-coordinators")
    public ResponseEntity<List<CollegeWithUniversityPersonDTO>> getAllCollegesWithCoordinators() {
        List<College> colleges = collegeService.getAllColleges();

        List<CollegeWithUniversityPersonDTO> result = colleges.stream()
                .map(college -> {
                    // Find UniversityPerson assigned to this college
                    Optional<UniversityPerson> upOptional = universityPersonRepository.findByCollegeId(college.getId());

                    CollegeWithUniversityPersonDTO.CoordinatorDTO coordinatorDTO = null;
                    boolean hasCoordinator = false;

                    if (upOptional.isPresent()) {
                        UniversityPerson up = upOptional.get();
                        hasCoordinator = true;
                        coordinatorDTO = new CollegeWithUniversityPersonDTO.CoordinatorDTO(
                                up.getId(),
                                up.getUserName(),
                                up.getStatus(),
                                up.getStudents() != null ? up.getStudents().size() : 0);
                    }

                    return new CollegeWithUniversityPersonDTO(
                            college.getId(),
                            college.getCollegeName(),
                            college.getCity(),
                            college.getCountry(),
                            hasCoordinator,
                            coordinatorDTO);
                })
                .collect(java.util.stream.Collectors.toList());

        return ResponseEntity.ok(result);
    }

    // ======================= PERFORMANCE REPORTS =======================

    /**
     * Get performance report for a single college/university
     */
    @GetMapping("/reports/{id}")
    public ResponseEntity<?> getCollegeReport(
            @PathVariable Long id,
            @RequestParam(defaultValue = "daily") String period) {
        try {
            return ResponseEntity.ok(collegeService.generateCollegeReport(id, period));
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error generating report: " + e.getMessage());
        }
    }

    /**
     * Get performance reports for all colleges/universities
     */
    @GetMapping("/reports/all")
    public ResponseEntity<?> getAllCollegesReport(@RequestParam(defaultValue = "daily") String period) {
        try {
            return ResponseEntity.ok(collegeService.generateAllCollegesReport(period));
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error generating reports: " + e.getMessage());
        }
    }

    /**
     * Email report for a single college/university
     */
    @PostMapping("/reports/{id}/email")
    public ResponseEntity<?> emailCollegeReport(
            @PathVariable Long id,
            @RequestParam(defaultValue = "daily") String period,
            @RequestParam String email) {
        try {
            collegeService.sendCollegeReportByEmail(id, period, email);
            return ResponseEntity.ok("Report sent successfully to " + email);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error sending report: " + e.getMessage());
        }
    }

    /**
     * Email reports for all colleges/universities
     */
    @PostMapping("/reports/all/email")
    public ResponseEntity<?> emailAllCollegesReport(
            @RequestParam(defaultValue = "daily") String period,
            @RequestParam String email) {
        try {
            collegeService.sendAllCollegesReportEmail(period, email);
            return ResponseEntity.ok("All reports sent successfully to " + email);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error sending reports: " + e.getMessage());
        }
    }
}
