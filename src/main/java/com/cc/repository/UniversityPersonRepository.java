package com.cc.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.cc.modals.UniversityPerson;

@Repository
public interface UniversityPersonRepository extends JpaRepository<UniversityPerson, Long> {
    // Custom method to find a user by their username
    Optional<UniversityPerson> findByUserName(String userName);

    boolean existsByUserName(String userName);

    // Find UniversityPerson by College
    Optional<UniversityPerson> findByCollegeId(Long collegeId);

}