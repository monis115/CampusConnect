package com.cc.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.cc.modals.CollectionRequest;
import java.util.List;

@Repository
public interface CollectionRequestRepository extends JpaRepository<CollectionRequest, Long> {
    List<CollectionRequest> findByStudentId(Long studentId);
    List<CollectionRequest> findByAgentUsername(String agentUsername);
    List<CollectionRequest> findByCreatedBy(String createdBy);
    List<CollectionRequest> findByStatus(String status);
    List<CollectionRequest> findByStatusIn(List<String> statuses);
    List<CollectionRequest> findByStudentIdAndStatusIn(Long studentId, List<String> statuses);
}
