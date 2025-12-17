package com.cc.controller;

import com.cc.DTO.UniversityCoordinatorDTO;
import com.cc.modals.College;
import com.cc.service.CollegeService;
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
}
