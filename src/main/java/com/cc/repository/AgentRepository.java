package com.cc.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.cc.modals.Agent;

@Repository
public interface AgentRepository extends JpaRepository<Agent, Long> {
    Optional<Agent> findByUserId(String userId);

    boolean existsByUserId(String userId);

    boolean existsByEmail(String email);

    List<Agent> findByStatus(String status);

    @Query("SELECT a FROM Agent a WHERE SIZE(a.student) > 0")
    List<Agent> findAgentsWithStudents();

    // ADD THIS METHOD - Fetch agent with all related data eagerly
    @Query("SELECT DISTINCT a FROM Agent a " +
            "LEFT JOIN FETCH a.collegeAccess ca " +
            "LEFT JOIN FETCH ca.allowedLocations " +
            "WHERE a.userId = :userId")
    Optional<Agent> findByUserIdWithDetails(@Param("userId") String userId);
}