package com.cc.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.cc.modals.VisaMan;
import java.util.Optional;

@Repository
public interface VisaManRepository extends JpaRepository<VisaMan, Long> {
    Optional<VisaMan> findByUsername(String username);
}
