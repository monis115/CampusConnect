package com.cc.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.cc.modals.SubAccountTransaction;

import java.util.List;

@Repository
public interface SubAccountTransactionRepository extends JpaRepository<SubAccountTransaction, Long> {
    List<SubAccountTransaction> findBySubAccountIdOrderByTransactionDateDesc(Long subAccountId);
}
