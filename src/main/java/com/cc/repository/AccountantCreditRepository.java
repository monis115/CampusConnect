package com.cc.repository;

import com.cc.modals.AccountantCredit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AccountantCreditRepository extends JpaRepository<AccountantCredit, Long> {
    List<AccountantCredit> findByAccountantUsernameOrderByCreditDateDesc(String accountantUsername);
    List<AccountantCredit> findByAccountantUsernameAndStatusOrderByCreditDateDesc(String accountantUsername, String status);
    List<AccountantCredit> findByStatus(String status);
}
