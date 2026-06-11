package com.cc.service;

import com.cc.modals.AccountantCredit;
import com.cc.modals.CreditInstallment;
import com.cc.modals.CreditRepayment;
import com.cc.modals.SubAccount;
import com.cc.repository.AccountantCreditRepository;
import com.cc.repository.CreditInstallmentRepository;
import com.cc.repository.CreditRepaymentRepository;
import com.cc.repository.SubAccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class AccountantCreditService {

    @Autowired
    private AccountantCreditRepository accountantCreditRepository;

    @Autowired
    private CreditInstallmentRepository creditInstallmentRepository;

    @Autowired
    private CreditRepaymentRepository creditRepaymentRepository;

    @Autowired
    private SubAccountService subAccountService;
    
    @Autowired
    private SubAccountRepository subAccountRepository;

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

        // Initialize outstanding amount
        credit.setOutstandingAmount(credit.getAmount());
        credit.setStatus("ACTIVE");

        if (credit.getSubAccountId() != null) {
            String description = "Credit/Loan Received via " + credit.getMethod();
            if ("FRIEND".equalsIgnoreCase(credit.getMethod())) {
                description += " (" + credit.getFriendName() + ")";
            } else if ("LOAN".equalsIgnoreCase(credit.getMethod())) {
                description += " (" + credit.getBankName() + ")";
            } else if ("CREDIT_CARD".equalsIgnoreCase(credit.getMethod())) {
                description += " (" + credit.getCardInfo() + ")";
            }
            // Inflow - increases sub account balance
            subAccountService.updateBalance(credit.getSubAccountId(), credit.getAmount(), description, "CREDIT-IN-" + System.currentTimeMillis(), "LOAN");
        }

        AccountantCredit savedCredit = accountantCreditRepository.save(credit);
        generateRepaymentSchedule(savedCredit);
        
        return savedCredit;
    }

    private void generateRepaymentSchedule(AccountantCredit credit) {
        if ("INSTALLMENTS".equals(credit.getRepaymentStructure()) && credit.getLoanTenureMonths() != null && credit.getLoanTenureMonths() > 0) {
            double installmentAmt = credit.getInstallmentAmount() != null ? credit.getInstallmentAmount() : (credit.getTotalRepaymentAmount() != null ? credit.getTotalRepaymentAmount() / credit.getLoanTenureMonths() : credit.getAmount() / credit.getLoanTenureMonths());
            LocalDate nextDueDate = credit.getDueDate() != null ? credit.getDueDate() : (credit.getCreditDate() != null ? credit.getCreditDate().plusMonths(1) : LocalDate.now().plusMonths(1));
            
            for (int i = 0; i < credit.getLoanTenureMonths(); i++) {
                CreditInstallment installment = new CreditInstallment();
                installment.setCredit(credit);
                installment.setInstallmentNum(i + 1);
                installment.setInstallmentAmount(installmentAmt);
                installment.setDueDate(nextDueDate);
                installment.setStatus("PENDING");
                creditInstallmentRepository.save(installment);
                
                nextDueDate = nextDueDate.plusMonths(1);
            }
        }
    }

    public CreditRepayment recordRepayment(Long creditId, Long subAccountId, Double amount, String notes) {
        Optional<AccountantCredit> creditOpt = accountantCreditRepository.findById(creditId);
        if (creditOpt.isEmpty()) {
            throw new IllegalArgumentException("Credit not found");
        }
        
        AccountantCredit credit = creditOpt.get();
        if (amount > credit.getOutstandingAmount()) {
            throw new IllegalArgumentException("Repayment amount cannot exceed outstanding balance");
        }

        SubAccount subAccount = null;
        if (subAccountId != null) {
            Optional<SubAccount> subAccountOpt = subAccountRepository.findById(subAccountId);
            if (subAccountOpt.isPresent()) {
                subAccount = subAccountOpt.get();
            } else {
                throw new IllegalArgumentException("SubAccount not found");
            }
        }

        // Record repayment
        CreditRepayment repayment = new CreditRepayment();
        repayment.setCredit(credit);
        repayment.setSubAccount(subAccount);
        repayment.setAmount(amount);
        repayment.setNotes(notes);
        repayment.setPaymentDate(LocalDateTime.now());
        creditRepaymentRepository.save(repayment);

        // Update outstanding and total repaid
        credit.setAmountRepaid(credit.getAmountRepaid() + amount);
        credit.setOutstandingAmount(credit.getOutstandingAmount() - amount);
        
        // Outflow - decreases sub account balance
        if (subAccount != null) {
            String description = "Repayment for Credit/Loan ID: " + credit.getId();
            // Since updateBalance typically takes positive amount and determines credit/debit based on transactionType inside (wait, SubAccountService updateBalance uses a positive amount to ADD, so we need to SUBTRACT). Let's pass negative amount!
            // Wait, does updateBalance add or subtract? Let's check SubAccountService.
            subAccountService.updateBalance(subAccount.getId(), -amount, description, "CREDIT-REPAY-" + System.currentTimeMillis(), "LOAN_REPAYMENT");
        }

        // Update status
        if (credit.getOutstandingAmount() <= 0) {
            credit.setStatus("CLOSED");
        } else {
            credit.setStatus("PARTIALLY_PAID");
        }
        accountantCreditRepository.save(credit);

        // Update installments logic
        updateInstallmentsForRepayment(credit, amount);

        return repayment;
    }

    private void updateInstallmentsForRepayment(AccountantCredit credit, Double repaymentAmount) {
        List<CreditInstallment> installments = creditInstallmentRepository.findByCreditIdOrderByDueDateAsc(credit.getId());
        double remainingToAllocate = repaymentAmount;

        for (CreditInstallment inst : installments) {
            if (!"PAID".equals(inst.getStatus()) && remainingToAllocate > 0) {
                double pendingForInst = inst.getInstallmentAmount() - inst.getPaidAmount();
                if (remainingToAllocate >= pendingForInst) {
                    inst.setPaidAmount(inst.getInstallmentAmount());
                    inst.setStatus("PAID");
                    inst.setPaidDate(LocalDateTime.now());
                    remainingToAllocate -= pendingForInst;
                } else {
                    inst.setPaidAmount(inst.getPaidAmount() + remainingToAllocate);
                    remainingToAllocate = 0;
                }
                creditInstallmentRepository.save(inst);
            }
        }
    }

    public List<CreditRepayment> getCreditTransactions(Long creditId) {
        return creditRepaymentRepository.findByCreditIdOrderByPaymentDateDesc(creditId);
    }
    
    public List<CreditInstallment> getCreditInstallments(Long creditId) {
        return creditInstallmentRepository.findByCreditIdOrderByDueDateAsc(creditId);
    }
}
