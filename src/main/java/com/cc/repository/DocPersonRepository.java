package com.cc.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.cc.modals.DocPerson;
import java.util.Optional;

@Repository
public interface DocPersonRepository extends JpaRepository<DocPerson, Long> {
    Optional<DocPerson> findByUsername(String username);
}
