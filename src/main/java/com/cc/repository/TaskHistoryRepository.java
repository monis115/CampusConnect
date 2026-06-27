package com.cc.repository;

import com.cc.modals.TaskHistoryEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskHistoryRepository extends JpaRepository<TaskHistoryEntry, Long> {

    List<TaskHistoryEntry> findByTaskIdOrderByCreatedAtAsc(Long taskId);
}
