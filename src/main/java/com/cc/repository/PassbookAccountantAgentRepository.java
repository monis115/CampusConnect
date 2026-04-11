package com.cc.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.cc.modals.PassbookForAccountantAgent;

import java.util.Optional;

@Repository
public interface PassbookAccountantAgentRepository extends JpaRepository<PassbookForAccountantAgent, Long> {
    Optional<PassbookForAccountantAgent> findByApplicationId(String applicationId);
}
