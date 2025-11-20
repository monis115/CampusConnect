package com.cc.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.cc.modals.TelexMan;
import java.util.Optional;

@Repository
public interface TelexManRepository extends JpaRepository<TelexMan, Long> {
    Optional<TelexMan> findByUsername(String username);
}
