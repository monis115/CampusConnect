package com.cc.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.cc.modals.AddmissionManager;

@Repository
public interface AddmissionManagerRepository extends JpaRepository<AddmissionManager, Long> {

    Optional<AddmissionManager> findByUsername(String username);

    Optional<AddmissionManager> findByEmail(String email);
    
    
    List<AddmissionManager> findByStudentsIsNotEmpty();

}
