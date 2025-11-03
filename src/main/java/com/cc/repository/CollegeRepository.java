package com.cc.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.cc.modals.College;

@Repository
public interface CollegeRepository extends JpaRepository<College, Long> {
    College findByCollegeName(String collegeName);
    
}
