package com.cc.service;

import com.cc.modals.SubAccountTransaction;
import com.cc.modals.SubAccount;
import com.cc.repository.SubAccountTransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TransactionService {

    @Autowired
    private SubAccountTransactionRepository subAccountTransactionRepository;

    @Autowired
    private SubAccountService subAccountService;

    @Transactional
    public void logAgentPayment(Long subAccountId, Double amount, String referenceId, String remarks) {
        logCredit(subAccountId, amount, "Agent Payment" + (remarks != null ? " - " + remarks : ""), referenceId);
    }

    @Transactional
    public void logCashSettlement(Long subAccountId, Double amount, String referenceId, String remarks) {
        logCredit(subAccountId, amount, "Cash Settlement" + (remarks != null ? " - " + remarks : ""), referenceId);
    }

    @Transactional
    public void logFundRelease(Long subAccountId, Double amount, String referenceId, String remarks) {
        logDebit(subAccountId, amount, "Fund Release" + (remarks != null ? " - " + remarks : ""), referenceId);
    }

    private void logCredit(Long subAccountId, Double amount, String description, String referenceId) {
        SubAccount subAccount = subAccountService.getSubAccountById(subAccountId);
        if (subAccount == null) {
            throw new RuntimeException("SubAccount not found for ID: " + subAccountId);
        }
        
        subAccountService.updateBalance(subAccountId, amount, description, referenceId);
    }

    private void logDebit(Long subAccountId, Double amount, String description, String referenceId) {
        SubAccount subAccount = subAccountService.getSubAccountById(subAccountId);
        if (subAccount == null) {
            throw new RuntimeException("SubAccount not found for ID: " + subAccountId);
        }

        if (subAccount.getBalance() < amount) {
            throw new RuntimeException("Insufficient balance in SubAccount: " + subAccount.getBankName());
        }
        
        subAccountService.updateBalance(subAccountId, -amount, description, referenceId);
    }
}
