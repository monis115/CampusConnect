package com.cc.service;

import com.cc.modals.*;
import com.cc.repository.*;
import com.cc.service.engine.AmortizationEngine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
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
    private LoanLedgerRepository loanLedgerRepository;

    @Autowired
    private LoanTimelineEventRepository timelineEventRepository;

    @Autowired
    private SubAccountService subAccountService;
    
    @Autowired
    private SubAccountRepository subAccountRepository;

    @Autowired
    private AmortizationEngine amortizationEngine;

    public List<AccountantCredit> getAllCreditsForAccountant(String username) {
        return accountantCreditRepository.findByAccountantUsernameOrderByCreditDateDesc(username);
    }

    public List<AccountantCredit> getActiveCreditsForAccountant(String username) {
        return accountantCreditRepository.findByAccountantUsernameAndStatusOrderByCreditDateDesc(username, "ACTIVE");
    }

    @Transactional
    public AccountantCredit addCredit(AccountantCredit credit, String username) {
        credit.setAccountantUsername(username);
        
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

        if (credit.getTotalRepaymentAmount() != null && credit.getTotalRepaymentAmount().compareTo(BigDecimal.ZERO) > 0) {
            credit.setOutstandingAmount(credit.getTotalRepaymentAmount());
        } else {
            credit.setOutstandingAmount(credit.getAmount());
        }
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
            subAccountService.updateBalance(credit.getSubAccountId(), credit.getAmount().doubleValue(), description, "CREDIT-IN-" + System.currentTimeMillis(), "LOAN");
        }

        // Generate Schedule before saving to set installmentAmount and totalRepaymentAmount
        List<CreditInstallment> schedule = null;
        if ("INSTALLMENTS".equals(credit.getRepaymentStructure()) && credit.getLoanTenureMonths() != null && credit.getLoanTenureMonths() > 0) {
            LocalDate startDate = credit.getDueDate() != null ? credit.getDueDate() : (credit.getCreditDate() != null ? credit.getCreditDate() : LocalDate.now());
            schedule = amortizationEngine.generateSchedule(credit, startDate);
        }

        AccountantCredit savedCredit = accountantCreditRepository.save(credit);

        if (schedule != null) {
            for (CreditInstallment inst : schedule) {
                inst.setCredit(savedCredit);
                creditInstallmentRepository.save(inst);
            }
        }

        // Timeline Event
        LoanTimelineEvent event = new LoanTimelineEvent();
        event.setCredit(savedCredit);
        event.setEventType("LOAN_CREATED");
        event.setDescription("Loan recorded with principal " + savedCredit.getAmount());
        timelineEventRepository.save(event);

        // Ledger Entry
        LoanLedger ledger = new LoanLedger();
        ledger.setCredit(savedCredit);
        ledger.setTransactionType("CREDIT");
        ledger.setPrincipalComponent(savedCredit.getAmount());
        ledger.setTotalAmount(savedCredit.getAmount());
        ledger.setBalanceBefore(BigDecimal.ZERO);
        ledger.setBalanceAfter(savedCredit.getAmount());
        ledger.setRecordedBy(username);
        loanLedgerRepository.save(ledger);
        
        return savedCredit;
    }

    @Transactional
    public LoanLedger recordRepayment(Long creditId, Long subAccountId, BigDecimal amount, String notes, String username) {
        Optional<AccountantCredit> creditOpt = accountantCreditRepository.findById(creditId);
        if (creditOpt.isEmpty()) {
            throw new IllegalArgumentException("Credit not found");
        }
        
        AccountantCredit credit = creditOpt.get();
        if (amount.compareTo(credit.getOutstandingAmount()) > 0) {
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

        BigDecimal balanceBefore = credit.getOutstandingAmount();
        BigDecimal balanceAfter = balanceBefore.subtract(amount);

        // Ledger Entry
        LoanLedger ledger = new LoanLedger();
        ledger.setCredit(credit);
        ledger.setTransactionType("DEBIT");
        ledger.setTotalAmount(amount);
        ledger.setBalanceBefore(balanceBefore);
        ledger.setBalanceAfter(balanceAfter);
        ledger.setRecordedBy(username);

        // Outflow - decreases sub account balance
        if (subAccount != null) {
            String description = "Repayment for Credit/Loan ID: " + credit.getId() + (notes != null ? " - " + notes : "");
            subAccountService.updateBalance(subAccount.getId(), -amount.doubleValue(), description, "CREDIT-REPAY-" + System.currentTimeMillis(), "LOAN_REPAYMENT");
        }

        // Update installments logic (Waterfall)
        BigDecimal principalComponent = updateInstallmentsForRepayment(credit, amount);
        
        // Finalize Ledger
        ledger.setPrincipalComponent(principalComponent);
        ledger.setInterestComponent(amount.subtract(principalComponent));
        loanLedgerRepository.save(ledger);

        // Update outstanding and total repaid
        credit.setAmountRepaid(credit.getAmountRepaid().add(amount));
        credit.setOutstandingAmount(balanceAfter);
        
        if (credit.getOutstandingAmount().compareTo(BigDecimal.ZERO) <= 0) {
            credit.setStatus("CLOSED");
            LoanTimelineEvent event = new LoanTimelineEvent();
            event.setCredit(credit);
            event.setEventType("CLOSED");
            event.setDescription("Loan fully repaid and closed.");
            timelineEventRepository.save(event);
        } else {
            credit.setStatus("ACTIVE");
        }
        accountantCreditRepository.save(credit);

        // Timeline Event
        LoanTimelineEvent event = new LoanTimelineEvent();
        event.setCredit(credit);
        event.setEventType("EMI_PAID");
        event.setDescription("Repayment of " + amount + " recorded.");
        timelineEventRepository.save(event);

        return ledger;
    }

    private BigDecimal updateInstallmentsForRepayment(AccountantCredit credit, BigDecimal repaymentAmount) {
        List<CreditInstallment> installments = creditInstallmentRepository.findByCreditIdOrderByDueDateAsc(credit.getId());
        BigDecimal remainingToAllocate = repaymentAmount;
        BigDecimal totalPrincipalCleared = BigDecimal.ZERO;

        for (CreditInstallment inst : installments) {
            if (!"PAID".equals(inst.getStatus()) && remainingToAllocate.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal totalDueForInst = inst.getInstallmentAmount();
                if (inst.getPenaltyAmount() != null) {
                    totalDueForInst = totalDueForInst.add(inst.getPenaltyAmount());
                }
                BigDecimal currentPaid = inst.getPaidAmount() != null ? inst.getPaidAmount() : BigDecimal.ZERO;
                BigDecimal pendingForInst = totalDueForInst.subtract(currentPaid);
                
                if (pendingForInst.compareTo(BigDecimal.ZERO) <= 0) {
                    inst.setStatus("PAID");
                    if (inst.getPaidDate() == null) inst.setPaidDate(LocalDateTime.now());
                    creditInstallmentRepository.save(inst);
                    continue;
                }

                if (remainingToAllocate.compareTo(pendingForInst) >= 0) {
                    inst.setPaidAmount(totalDueForInst);
                    inst.setStatus("PAID");
                    inst.setPaidDate(LocalDateTime.now());
                    
                    // Simple logic for principal component extraction in waterfall
                    totalPrincipalCleared = totalPrincipalCleared.add(inst.getPrincipalPortion());
                    
                    remainingToAllocate = remainingToAllocate.subtract(pendingForInst);
                } else {
                    inst.setPaidAmount(currentPaid.add(remainingToAllocate));
                    
                    // Proportional principal clearing
                    BigDecimal ratio = remainingToAllocate.divide(totalDueForInst, 4, java.math.RoundingMode.HALF_UP);
                    totalPrincipalCleared = totalPrincipalCleared.add(inst.getPrincipalPortion().multiply(ratio));
                    
                    remainingToAllocate = BigDecimal.ZERO;
                }
                creditInstallmentRepository.save(inst);
            }
        }
        return totalPrincipalCleared;
    }

    @Autowired
    private com.cc.service.engine.ForeclosureEngine foreclosureEngine;

    @Autowired
    private com.cc.service.engine.PrepaymentEngine prepaymentEngine;

    @Transactional
    public LoanLedger processForeclosure(Long creditId, BigDecimal foreclosureChargePercentage, Long subAccountId, String username) {
        AccountantCredit credit = accountantCreditRepository.findById(creditId)
                .orElseThrow(() -> new IllegalArgumentException("Credit not found"));
        
        List<CreditInstallment> installments = creditInstallmentRepository.findByCreditIdOrderByDueDateAsc(creditId);
        
        com.cc.service.engine.ForeclosureEngine.ForeclosureResult result = foreclosureEngine.processForeclosure(credit, installments, foreclosureChargePercentage);
        
        LoanLedger ledger = result.getLedgerEntry();
        ledger.setRecordedBy(username);
        
        for (CreditInstallment inst : result.getUpdatedInstallments()) {
            creditInstallmentRepository.save(inst);
        }
        
        accountantCreditRepository.save(result.getUpdatedCredit());
        loanLedgerRepository.save(ledger);

        // Debit source account for the settlement amount
        if (subAccountId != null) {
            String description = "Foreclosure settlement for Loan CR-" + credit.getId();
            subAccountService.updateBalance(subAccountId, -result.getSettlementAmount().doubleValue(), description, "FORECLOSE-" + System.currentTimeMillis(), "LOAN_FORECLOSURE");
        }

        // Timeline Event
        LoanTimelineEvent event = new LoanTimelineEvent();
        event.setCredit(credit);
        event.setEventType("FORECLOSED");
        event.setDescription("Loan foreclosed with settlement amount " + result.getSettlementAmount());
        timelineEventRepository.save(event);

        return ledger;
    }

    @Transactional
    public LoanLedger processPrepayment(Long creditId, BigDecimal amount, com.cc.service.engine.PrepaymentEngine.PrepaymentStrategy strategy, String username) {
        AccountantCredit credit = accountantCreditRepository.findById(creditId)
                .orElseThrow(() -> new IllegalArgumentException("Credit not found"));
        
        List<CreditInstallment> installments = creditInstallmentRepository.findByCreditIdOrderByDueDateAsc(creditId);
        
        com.cc.service.engine.PrepaymentEngine.PrepaymentResult result = prepaymentEngine.processPrepayment(credit, installments, amount, strategy, LocalDate.now());
        
        LoanLedger ledger = result.getLedgerEntry();
        ledger.setRecordedBy(username);

        // Delete old pending installments
        for (CreditInstallment inst : installments) {
            if ("PENDING".equals(inst.getStatus())) {
                creditInstallmentRepository.delete(inst);
            }
        }

        // Save new pending schedule
        for (CreditInstallment inst : result.getNewPendingSchedule()) {
            creditInstallmentRepository.save(inst);
        }

        accountantCreditRepository.save(result.getUpdatedCredit());
        loanLedgerRepository.save(ledger);

        // Timeline Event
        LoanTimelineEvent event = new LoanTimelineEvent();
        event.setCredit(credit);
        event.setEventType("EMI_PAID"); // Using EMI_PAID as general payment event or PARTIAL_PAYMENT
        event.setDescription("Partial prepayment of " + amount + " recorded. Strategy: " + strategy);
        timelineEventRepository.save(event);

        return ledger;
    }

    public List<LoanLedger> getCreditTransactions(Long creditId) {
        return loanLedgerRepository.findByCreditIdOrderByTimestampAsc(creditId);
    }
    
    @Transactional
    public List<CreditInstallment> getCreditInstallments(Long creditId) {
        List<CreditInstallment> existing = creditInstallmentRepository.findByCreditIdOrderByDueDateAsc(creditId);
        if (!existing.isEmpty()) {
            return existing;
        }

        // Auto-generate installments if credit has INSTALLMENTS structure but no rows exist yet
        Optional<AccountantCredit> creditOpt = accountantCreditRepository.findById(creditId);
        if (creditOpt.isEmpty()) {
            return existing;
        }

        AccountantCredit credit = creditOpt.get();
        if ("INSTALLMENTS".equals(credit.getRepaymentStructure())
                && credit.getLoanTenureMonths() != null
                && credit.getLoanTenureMonths() > 0) {
            LocalDate startDate = credit.getDueDate() != null ? credit.getDueDate()
                    : (credit.getCreditDate() != null ? credit.getCreditDate() : LocalDate.now());
            List<CreditInstallment> schedule = amortizationEngine.generateSchedule(credit, startDate);
            for (CreditInstallment inst : schedule) {
                inst.setCredit(credit);
                creditInstallmentRepository.save(inst);
            }
            accountantCreditRepository.save(credit); // persist any fields the engine updated (installmentAmount, totalRepaymentAmount)
            return creditInstallmentRepository.findByCreditIdOrderByDueDateAsc(creditId);
        }

        return existing;
    }
    
    @Transactional
    public AccountantCredit updateCreditDetails(Long creditId, com.cc.DTO.CreditDetailsUpdateRequest request) {
        AccountantCredit credit = accountantCreditRepository.findById(creditId)
                .orElseThrow(() -> new IllegalArgumentException("Credit not found"));

        if (request.getPurpose() != null) {
            credit.setPurpose(request.getPurpose());
        }
        if (request.getLateFeeType() != null) {
            credit.setLateFeeType(request.getLateFeeType());
        }
        if (request.getLateFeeValue() != null) {
            credit.setLateFeeValue(request.getLateFeeValue());
        }
        if (request.getFixedLateFine() != null) {
            credit.setFixedLateFine(request.getFixedLateFine());
        }

        return accountantCreditRepository.save(credit);
    }

    public List<LoanTimelineEvent> getTimelineEvents(Long creditId) {
        return timelineEventRepository.findByCreditIdOrderByEventDateDesc(creditId);
    }
}
