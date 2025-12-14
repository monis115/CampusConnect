package com.cc.repository;

import com.cc.modals.RequestMoney;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RequestMoneyRepository extends JpaRepository<RequestMoney, Long> {
    List<RequestMoney> findByRequesterUsername(String requesterUsername);

    List<RequestMoney> findByRequesterUsernameOrderByIdDesc(String requesterUsername);

    List<RequestMoney> findByStatus(String status);
}
