package com.cc.service;

import com.cc.modals.AccountantCredit;
import com.cc.modals.CreditInstallment;
import com.cc.modals.LoanLedger;
import com.cc.repository.AccountantCreditRepository;
import com.cc.repository.CreditInstallmentRepository;
import com.cc.repository.LoanLedgerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class InterestAccrualScheduler {

    @Autowired
    private CreditInstallmentRepository installmentRepository;
    
    @Autowired
    private AccountantCreditRepository creditRepository;
    
    @Autowired
    private LoanLedgerRepository loanLedgerRepository;

    // Run every day at Midnight
    @Scheduled(cron = "0 0 0 * * ?")
    @Transactional
    public void accrueDailyPenaltyInterest() {
        LocalDate today = LocalDate.now();
        
        // Find all installments that are past due (PENDING or OVERDUE)
        List<String> statuses = java.util.Arrays.asList("PENDING", "OVERDUE");
        List<CreditInstallment> overdueInstallments = installmentRepository.findByStatusInAndDueDateBefore(statuses, today);
        
        for (CreditInstallment inst : overdueInstallments) {
            AccountantCredit credit = inst.getCredit();
            if (!"ACTIVE".equals(credit.getStatus())) continue;

            BigDecimal penaltyAmount = BigDecimal.ZERO;
            
            // 1. One-time Fixed Fine (Apply only when transitioning from PENDING to OVERDUE today)
            boolean justBecameOverdue = false;
            if ("PENDING".equals(inst.getStatus())) {
                inst.setStatus("OVERDUE");
                justBecameOverdue = true;
                
                if (credit.getFixedLateFine() != null && credit.getFixedLateFine().compareTo(BigDecimal.ZERO) > 0) {
                    penaltyAmount = penaltyAmount.add(credit.getFixedLateFine());
                }
            }

            // 2. Recurring Daily Penalty (Apply every day it is OVERDUE, including the first day)
            if (credit.getLateFeeValue() != null && credit.getLateFeeValue().compareTo(BigDecimal.ZERO) > 0) {
                if ("FIXED_AMOUNT".equals(credit.getLateFeeType())) {
                    penaltyAmount = penaltyAmount.add(credit.getLateFeeValue());
                } else {
                    // PERCENTAGE
                    BigDecimal dailyPercentageFee = inst.getInstallmentAmount()
                            .multiply(credit.getLateFeeValue())
                            .divide(new BigDecimal("100"), 2, java.math.RoundingMode.HALF_UP);
                    penaltyAmount = penaltyAmount.add(dailyPercentageFee);
                }
            }
            
            if (penaltyAmount.compareTo(BigDecimal.ZERO) > 0) {
                // Increase outstanding amount of the credit
                BigDecimal currentOutstanding = credit.getOutstandingAmount() != null ? credit.getOutstandingAmount() : BigDecimal.ZERO;
                credit.setOutstandingAmount(currentOutstanding.add(penaltyAmount));
                
                // Increase the specific installment amount
                inst.setInstallmentAmount(inst.getInstallmentAmount().add(penaltyAmount));
                
                // Save both
                creditRepository.save(credit);
                installmentRepository.save(inst);
                
                // Log to ledger
                LoanLedger ledger = new LoanLedger();
                ledger.setCredit(credit);
                // timestamp is auto-set
                ledger.setTransactionType("PENALTY");
                ledger.setPenaltyComponent(penaltyAmount);
                ledger.setTotalAmount(penaltyAmount);
                ledger.setBalanceBefore(currentOutstanding);
                ledger.setBalanceAfter(credit.getOutstandingAmount());
                ledger.setRecordedBy("SYSTEM");
                loanLedgerRepository.save(ledger);
            } else {
                installmentRepository.save(inst);
            }
        }
    }
}
