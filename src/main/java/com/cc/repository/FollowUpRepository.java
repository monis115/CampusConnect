package com.cc.repository;

import com.cc.modals.FollowUp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FollowUpRepository extends JpaRepository<FollowUp, Long> {
    List<FollowUp> findByTaskIdOrderByCreatedAtAsc(Long taskId);
}
