package com.cc.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.cc.modals.VisaPaymentTransaction;
import java.util.List;

@Repository
public interface VisaPaymentTransactionRepository extends JpaRepository<VisaPaymentTransaction, Long> {
    List<VisaPaymentTransaction> findByStudentIdOrderByTransactionDateDesc(Long studentId);
}
