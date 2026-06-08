package com.cc.repository;

import com.cc.modals.BeneficiaryAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BeneficiaryAccountRepository extends JpaRepository<BeneficiaryAccount, Long> {
    List<BeneficiaryAccount> findByStatus(String status);
}
