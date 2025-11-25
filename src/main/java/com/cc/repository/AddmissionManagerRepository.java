package com.cc.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.cc.modals.AddmissionManager;

@Repository
public interface AddmissionManagerRepository extends JpaRepository<AddmissionManager, Long> {

    Optional<AddmissionManager> findByUsername(String username);

    Optional<AddmissionManager> findByEmail(String email);

    List<AddmissionManager> findByStudentsIsNotEmpty();

    // ADD THIS METHOD - Fetch manager with all related data eagerly
    @Query("SELECT DISTINCT m FROM AddmissionManager m " +
            "LEFT JOIN FETCH m.colleges " +
            "WHERE m.username = :username")
    Optional<AddmissionManager> findByUsernameWithDetails(@Param("username") String username);

    // Find manager by college ID
    Optional<AddmissionManager> findByCollegesId(Long collegeId);
}