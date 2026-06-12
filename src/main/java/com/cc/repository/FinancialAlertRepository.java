package com.cc.repository;

import com.cc.modals.FinancialAlert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FinancialAlertRepository extends JpaRepository<FinancialAlert, Long> {
    List<FinancialAlert> findByAccountantUsernameOrderByCreatedAtDesc(String username);
    List<FinancialAlert> findByAccountantUsernameAndIsReadFalseOrderByCreatedAtDesc(String username);
}
