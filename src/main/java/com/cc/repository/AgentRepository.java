package com.cc.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.cc.modals.Agent;

@Repository
public interface AgentRepository extends JpaRepository<Agent, Long> {
    Optional<Agent> findByUserId(String userId);
    boolean existsByUserId(String userId);
    boolean existsByEmail(String email);
    
    List<Agent> findByStatus(String status);

}
