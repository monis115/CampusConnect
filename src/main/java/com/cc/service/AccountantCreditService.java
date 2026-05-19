package com.cc.service;

import com.cc.modals.AccountantCredit;
import com.cc.repository.AccountantCreditRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AccountantCreditService {

    @Autowired
    private AccountantCreditRepository accountantCreditRepository;

    @Autowired
    private com.cc.service.SubAccountService subAccountService;

    public List<AccountantCredit> getAllCreditsForAccountant(String username) {
        return accountantCreditRepository.findByAccountantUsernameOrderByCreditDateDesc(username);
    }

    public List<AccountantCredit> getActiveCreditsForAccountant(String username) {
        return accountantCreditRepository.findByAccountantUsernameAndStatusOrderByCreditDateDesc(username, "ACTIVE");
    }

    public AccountantCredit addCredit(AccountantCredit credit, String username) {
        credit.setAccountantUsername(username);
        
        // Basic validation based on method
        if (credit.getMethod() == null) {
            throw new IllegalArgumentException("Credit method is required");
        }

        switch (credit.getMethod().toUpperCase()) {
            case "FRIEND":
                if (credit.getFriendName() == null || credit.getFriendName().trim().isEmpty()) {
                    throw new IllegalArgumentException("Friend name is required for FRIEND method");
                }
                break;
            case "LOAN":
                if (credit.getBankName() == null || credit.getBankName().trim().isEmpty()) {
                    throw new IllegalArgumentException("Bank name is required for LOAN method");
                }
                break;
            case "CREDIT_CARD":
                if (credit.getCardInfo() == null || credit.getCardInfo().trim().isEmpty()) {
                    throw new IllegalArgumentException("Card info is required for CREDIT_CARD method");
                }
                break;
            default:
                throw new IllegalArgumentException("Invalid credit method: " + credit.getMethod());
        }

        if (credit.getSubAccountId() != null) {
            String description = "Credit Added via " + credit.getMethod();
            if ("FRIEND".equalsIgnoreCase(credit.getMethod())) {
                description += " (" + credit.getFriendName() + ")";
            } else if ("LOAN".equalsIgnoreCase(credit.getMethod())) {
                description += " (" + credit.getBankName() + ")";
            } else if ("CREDIT_CARD".equalsIgnoreCase(credit.getMethod())) {
                description += " (" + credit.getCardInfo() + ")";
            }
            subAccountService.updateBalance(credit.getSubAccountId(), credit.getAmount(), description, "CREDIT-" + System.currentTimeMillis());
        }

        return accountantCreditRepository.save(credit);
    }
}
