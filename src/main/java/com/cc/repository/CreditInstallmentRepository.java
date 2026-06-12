package com.cc.repository;

import com.cc.modals.CreditInstallment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CreditInstallmentRepository extends JpaRepository<CreditInstallment, Long> {
    List<CreditInstallment> findByCreditIdOrderByDueDateAsc(Long creditId);
    List<CreditInstallment> findByStatus(String status);
    long countByCreditIdAndStatus(Long creditId, String status);
    
    // For aging and forecasting
    List<CreditInstallment> findByCreditAccountantUsernameAndStatusIn(String username, List<String> statuses);
    List<CreditInstallment> findByCreditAccountantUsernameOrderByDueDateAsc(String username);
    
    // For alerts
    List<CreditInstallment> findByStatusAndDueDate(String status, java.time.LocalDate dueDate);
    List<CreditInstallment> findByStatusAndDueDateBefore(String status, java.time.LocalDate dueDate);
    List<CreditInstallment> findByStatusInAndDueDateBefore(List<String> statuses, java.time.LocalDate dueDate);
}
