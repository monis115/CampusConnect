package com.cc.service;

import com.cc.modals.College;
import com.cc.repository.CollegeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CollegeService {

    @Autowired
    private CollegeRepository collegeRepository;

    public List<College> getAllColleges() {
        return collegeRepository.findAll();
    }

    public Optional<College> getCollegeById(Long id) {
        return collegeRepository.findById(id);
    }

    public College createCollege(College college) {
        return collegeRepository.save(college);
    }

    public College updateCollege(Long id, College updatedCollege) {
        return collegeRepository.findById(id).map(college -> {
            college.setCollegeName(updatedCollege.getCollegeName());
            college.setCity(updatedCollege.getCity());
            college.setCountry(updatedCollege.getCountry());
            return collegeRepository.save(college);
        }).orElse(null);
    }

    public void deleteCollege(Long id) {
        collegeRepository.deleteById(id);
    }

    public College findByCollegeName(String collegeName) {
        return collegeRepository.findByCollegeName(collegeName);
    }
}
