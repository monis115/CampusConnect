package com.cc.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.cc.modals.PassbookVisaManAccountant;

import java.util.Optional;

@Repository
public interface PassbookVisaManAccountantRepository extends JpaRepository<PassbookVisaManAccountant, Long> {
    Optional<PassbookVisaManAccountant> findByApplicationId(String applicationId);
    java.util.List<PassbookVisaManAccountant> findByRequesterName(String requesterName);
}
