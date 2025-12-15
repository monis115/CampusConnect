package com.cc.controller;

import com.cc.modals.College;
import com.cc.service.CollegeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
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
}
