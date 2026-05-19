package com.cc.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.cc.modals.SubAccount;
import com.cc.repository.SubAccountRepository;

@Service
public class SubAccountService {

    @Autowired
    private SubAccountRepository subAccountRepository;

    @Autowired
    private com.cc.repository.SubAccountTransactionRepository transactionRepository;

    public List<SubAccount> getAllSubAccounts() {
        return subAccountRepository.findAll();
    }

    public SubAccount getSubAccountById(Long id) {
        return subAccountRepository.findById(id).orElse(null);
    }

    public SubAccount saveSubAccount(SubAccount subAccount) {
        return subAccountRepository.save(subAccount);
    }

    public void updateBalance(Long id, Double amount, String description, String referenceId) {
        SubAccount account = getSubAccountById(id);
        if (account != null) {
            account.setBalance(account.getBalance() + amount);
            subAccountRepository.save(account);

            String type = amount >= 0 ? "CREDIT" : "DEBIT";
            com.cc.modals.SubAccountTransaction tx = new com.cc.modals.SubAccountTransaction(account, type, Math.abs(amount), description, referenceId);
            transactionRepository.save(tx);
        }
    }
}
