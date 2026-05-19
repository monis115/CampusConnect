package com.cc.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.cc.modals.SubAccount;

@Repository
public interface SubAccountRepository extends JpaRepository<SubAccount, Long> {
}
