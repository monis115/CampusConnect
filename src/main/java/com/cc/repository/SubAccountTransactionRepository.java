package com.cc.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.cc.modals.SubAccountTransaction;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Query;
import java.time.LocalDateTime;

@Repository
public interface SubAccountTransactionRepository extends JpaRepository<SubAccountTransaction, Long> {
    List<SubAccountTransaction> findBySubAccountIdOrderByTransactionDateDesc(Long subAccountId);
    Page<SubAccountTransaction> findAllByOrderByTransactionDateDesc(Pageable pageable);

    @Query("SELECT t FROM SubAccountTransaction t WHERE " +
           "(:subAccountId IS NULL OR t.subAccount.id = :subAccountId) AND " +
           "(:startDate IS NULL OR t.transactionDate >= :startDate) AND " +
           "(:endDate IS NULL OR t.transactionDate <= :endDate) " +
           "ORDER BY t.transactionDate DESC")
    Page<SubAccountTransaction> findFilteredTransactions(
        @Param("subAccountId") Long subAccountId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate,
        Pageable pageable
    );
}
