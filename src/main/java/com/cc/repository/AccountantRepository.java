package com.cc.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.cc.modals.Accountant;
import java.util.Optional;

@Repository
public interface AccountantRepository extends JpaRepository<Accountant, Long> {
    Optional<Accountant> findByUsername(String username);
}
