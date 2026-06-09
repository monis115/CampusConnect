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

    public void internalTransfer(Long sourceId, Long destId, Double amount, String description) {
        if (amount == null || amount <= 0) {
            throw new IllegalArgumentException("Transfer amount must be greater than zero.");
        }
        if (sourceId.equals(destId)) {
            throw new IllegalArgumentException("Source and destination accounts must be different.");
        }

        SubAccount sourceAccount = getSubAccountById(sourceId);
        SubAccount destAccount = getSubAccountById(destId);

        if (sourceAccount == null || destAccount == null) {
            throw new IllegalArgumentException("Source or destination account not found.");
        }

        if ("CASH".equalsIgnoreCase(sourceAccount.getAccountType()) || "CASH".equalsIgnoreCase(sourceAccount.getBankName())) {
            throw new IllegalArgumentException("Source account cannot be a CASH account.");
        }
        if ("CASH".equalsIgnoreCase(destAccount.getAccountType()) || "CASH".equalsIgnoreCase(destAccount.getBankName())) {
            throw new IllegalArgumentException("Destination account cannot be a CASH account.");
        }

        if (sourceAccount.getBalance() < amount) {
            throw new IllegalArgumentException("Insufficient balance in source account.");
        }

        // Deduct from source
        sourceAccount.setBalance(sourceAccount.getBalance() - amount);
        subAccountRepository.save(sourceAccount);

        // Add to destination
        destAccount.setBalance(destAccount.getBalance() + amount);
        subAccountRepository.save(destAccount);

        // Record transactions
        String refId = "INT-TRF-" + System.currentTimeMillis();
        
        com.cc.modals.SubAccountTransaction txSource = new com.cc.modals.SubAccountTransaction(sourceAccount, "DEBIT", amount, description + " (To " + destAccount.getBankName() + ")", refId);
        transactionRepository.save(txSource);

        com.cc.modals.SubAccountTransaction txDest = new com.cc.modals.SubAccountTransaction(destAccount, "CREDIT", amount, description + " (From " + sourceAccount.getBankName() + ")", refId);
        transactionRepository.save(txDest);
    }
}
