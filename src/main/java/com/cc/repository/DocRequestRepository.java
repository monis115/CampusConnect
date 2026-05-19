package com.cc.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.cc.modals.DocRequest;

@Repository
public interface DocRequestRepository extends JpaRepository<DocRequest, Long> {

    List<DocRequest> findByRequestedByOrderByCreatedAtDesc(String requestedBy);

    List<DocRequest> findByStatusOrderByCreatedAtDesc(String status);

    List<DocRequest> findByCollegeIdAndStatusOrderByCreatedAtDesc(Long collegeId, String status);

    List<DocRequest> findByCollegeIdOrderByCreatedAtDesc(Long collegeId);

    List<DocRequest> findByStudentIdOrderByCreatedAtDesc(Long studentId);

    List<DocRequest> findByStatusInOrderByCreatedAtDesc(List<String> statuses);
}
