package com.cc.scheduler;

import com.cc.modals.CreditInstallment;
import com.cc.repository.CreditInstallmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
public class CreditScheduler {

    @Autowired
    private CreditInstallmentRepository creditInstallmentRepository;

    @Autowired
    private com.cc.repository.AccountantCreditRepository accountantCreditRepository;

    @Autowired
    private com.cc.repository.LoanLedgerRepository loanLedgerRepository;

    @Autowired
    private com.cc.repository.LoanTimelineEventRepository timelineEventRepository;

    @Autowired
    private com.cc.service.engine.InterestAccrualEngine interestAccrualEngine;

    @Scheduled(cron = "0 0 0 * * *") // Runs once daily at midnight
    public void executeDailyBankingJobs() {
        markOverdueInstallments();
        processDailyInterestAccrual();
        updateHealthScores();
    }

    private void markOverdueInstallments() {
        List<CreditInstallment> installments = creditInstallmentRepository.findAll();
        LocalDate today = LocalDate.now();

        for (CreditInstallment inst : installments) {
            boolean isPending = "PENDING".equals(inst.getStatus());
            boolean isOverdue = "OVERDUE".equals(inst.getStatus());

            if ((isPending || isOverdue) && inst.getDueDate() != null && inst.getDueDate().isBefore(today)) {

                // If it was pending, change status and log timeline event
                if (isPending) {
                    inst.setStatus("OVERDUE");
                    com.cc.modals.LoanTimelineEvent event = new com.cc.modals.LoanTimelineEvent();
                    event.setCredit(inst.getCredit());
                    event.setEventType("EMI_MISSED");
                    event.setDescription("Installment #" + inst.getInstallmentNum() + " marked as overdue.");
                    timelineEventRepository.save(event);
                }

                // Calculate dynamic penalty based on Credit configuration
                java.math.BigDecimal totalPenalty = java.math.BigDecimal.ZERO;
                com.cc.modals.AccountantCredit credit = inst.getCredit();

                if (credit != null) {
                    // 1. One-Time Fixed Fine
                    if (credit.getFixedLateFine() != null) {
                        totalPenalty = totalPenalty.add(credit.getFixedLateFine());
                    }

                    // 2. Recurring Daily Penalty (Percentage)
                    if (credit.getLateFeeValue() != null
                            && credit.getLateFeeValue().compareTo(java.math.BigDecimal.ZERO) > 0) {
                        long daysOverdue = java.time.temporal.ChronoUnit.DAYS.between(inst.getDueDate(), today);
                        if (daysOverdue > 0) {
                            java.math.BigDecimal dailyPercent = credit.getLateFeeValue()
                                    .divide(java.math.BigDecimal.valueOf(100), 4, java.math.RoundingMode.HALF_UP);
                            java.math.BigDecimal dailyPenaltyAmount = inst.getInstallmentAmount()
                                    .multiply(dailyPercent);
                            java.math.BigDecimal totalDailyPenalty = dailyPenaltyAmount
                                    .multiply(java.math.BigDecimal.valueOf(daysOverdue));
                            totalPenalty = totalPenalty.add(totalDailyPenalty);
                        }
                    }
                }

                java.math.BigDecimal oldPenalty = inst.getPenaltyAmount() != null ? inst.getPenaltyAmount()
                        : java.math.BigDecimal.ZERO;
                java.math.BigDecimal newPenalty = totalPenalty.setScale(2, java.math.RoundingMode.HALF_UP);

                if (newPenalty.compareTo(oldPenalty) > 0) {
                    java.math.BigDecimal penaltyDifference = newPenalty.subtract(oldPenalty);

                    if (credit != null) {
                        if (credit.getOutstandingAmount() != null) {
                            credit.setOutstandingAmount(credit.getOutstandingAmount().add(penaltyDifference));
                        }
                        if (credit.getTotalRepaymentAmount() != null) {
                            credit.setTotalRepaymentAmount(credit.getTotalRepaymentAmount().add(penaltyDifference));
                        }
                        accountantCreditRepository.save(credit);
                    }
                }

                inst.setPenaltyAmount(newPenalty);

                creditInstallmentRepository.save(inst);
            }
        }
    }

    private void processDailyInterestAccrual() {
        List<com.cc.modals.AccountantCredit> activeCredits = accountantCreditRepository.findByStatus("ACTIVE");
        for (com.cc.modals.AccountantCredit credit : activeCredits) {
            com.cc.modals.LoanLedger ledger = interestAccrualEngine.calculateDailyAccrual(credit);
            if (ledger != null) {
                ledger.setRecordedBy("SYSTEM_SCHEDULER");
                loanLedgerRepository.save(ledger);
            }
        }
    }

    private void updateHealthScores() {
        List<com.cc.modals.AccountantCredit> activeCredits = accountantCreditRepository.findByStatus("ACTIVE");
        for (com.cc.modals.AccountantCredit credit : activeCredits) {
            long overdueCount = creditInstallmentRepository.countByCreditIdAndStatus(credit.getId(), "OVERDUE");

            String newHealth = "HEALTHY";
            if (overdueCount >= 3) {
                newHealth = "CRITICAL";
            } else if (overdueCount > 0) {
                newHealth = "ATTENTION";
            }

            if (!newHealth.equals(credit.getHealthScore())) {
                credit.setHealthScore(newHealth);
                accountantCreditRepository.save(credit);
            }
        }
    }
}
