package com.cc.repository;

import com.cc.modals.LoanTimelineEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LoanTimelineEventRepository extends JpaRepository<LoanTimelineEvent, Long> {
    List<LoanTimelineEvent> findByCreditIdOrderByEventDateDesc(Long creditId);
}
