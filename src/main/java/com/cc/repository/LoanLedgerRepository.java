package com.cc.repository;

import com.cc.modals.LoanLedger;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LoanLedgerRepository extends JpaRepository<LoanLedger, Long> {
    List<LoanLedger> findByCreditIdOrderByTimestampAsc(Long creditId);
}
