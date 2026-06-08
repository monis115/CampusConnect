package com.cc.repository;

import com.cc.modals.FundReleaseConfirmation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FundReleaseConfirmationRepository extends JpaRepository<FundReleaseConfirmation, Long> {

    List<FundReleaseConfirmation> findByVisaManagerUsernameOrderByCreatedAtDesc(String visaManagerUsername);
    
    Optional<FundReleaseConfirmation> findByFundRelease_ReleaseId(Long releaseId);
}
