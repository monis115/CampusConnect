package com.cc.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.cc.modals.AdditionalDocRequest;

@Repository
public interface AdditionalDocRequestRepository extends JpaRepository<AdditionalDocRequest, Long> {

    List<AdditionalDocRequest> findByStudentIdOrderByCreatedAtDesc(Long studentId);

    List<AdditionalDocRequest> findByAssignedToAgentOrderByCreatedAtDesc(String agentUsername);

    List<AdditionalDocRequest> findByAssignedToAgentAndStatusOrderByCreatedAtDesc(String agentUsername, String status);

}
