package com.cc.repository;

import com.cc.modals.CompanyLogo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CompanyLogoRepository extends JpaRepository<CompanyLogo, Long> {
    // Optional: Custom query if needed, e.g., findByName
    CompanyLogo findByName(String name);
}