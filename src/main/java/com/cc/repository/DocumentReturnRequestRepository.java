package com.cc.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.cc.modals.DocumentReturnRequest;

@Repository
public interface DocumentReturnRequestRepository extends JpaRepository<DocumentReturnRequest, Long> {
    DocumentReturnRequest findByStudentId(Long studentId);
}
