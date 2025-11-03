package com.cc.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.cc.modals.AddmissionManager;

import java.util.Optional;

@Repository
public interface AddmissionManagerRepository extends JpaRepository<AddmissionManager, Long> {

    Optional<AddmissionManager> findByUsername(String username);

    Optional<AddmissionManager> findByEmail(String email);
}
