package com.cc.repository;

import com.cc.modals.CreditRepayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CreditRepaymentRepository extends JpaRepository<CreditRepayment, Long> {
    List<CreditRepayment> findByCreditIdOrderByPaymentDateDesc(Long creditId);
}
