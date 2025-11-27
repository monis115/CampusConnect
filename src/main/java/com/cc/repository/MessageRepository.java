package com.cc.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.cc.modals.Message;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    // Find messages for a specific student, ordered by timestamp
    List<Message> findByStudentIdOrderByTimestampAsc(Long studentId);

    // Find messages between a specific agent and manager for a student
    List<Message> findByAgentIdAndAdmissionManagerIdAndStudentIdOrderByTimestampAsc(Long agentId,
            Long admissionManagerId, Long studentId);

    // Find all messages for a specific manager, ordered by timestamp descending
    List<Message> findByAdmissionManagerIdOrderByTimestampDesc(Long admissionManagerId);

    // Find all messages for a specific agent, ordered by timestamp descending
    List<Message> findByAgentIdOrderByTimestampDesc(Long agentId);
}
