package com.cc.repository;

import com.cc.modals.CreditInstallment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CreditInstallmentRepository extends JpaRepository<CreditInstallment, Long> {
    List<CreditInstallment> findByCreditIdOrderByDueDateAsc(Long creditId);
    List<CreditInstallment> findByStatus(String status);
}
