package com.cc.repository;

import com.cc.modals.FundRelease;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FundReleaseRepository extends JpaRepository<FundRelease, Long> {
    
    List<FundRelease> findByStudent_Id(Long studentId);

    List<FundRelease> findByStudent_VisaMan_UsernameOrderByCreatedAtDesc(String username);

    Optional<FundRelease> findTopByStudent_IdOrderByCreatedAtDesc(Long studentId);
}
