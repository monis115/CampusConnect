package com.cc.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.cc.modals.VisaMan;

@Repository
public interface VisaManRepository extends JpaRepository<VisaMan, Long> {
    Optional<VisaMan> findByUsername(String username);
}
