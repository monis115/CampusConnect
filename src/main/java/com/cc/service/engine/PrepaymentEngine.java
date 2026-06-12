package com.cc.service.engine;

import com.cc.modals.AccountantCredit;
import com.cc.modals.CreditInstallment;
import com.cc.modals.LoanLedger;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class PrepaymentEngine {

    private static final MathContext MC = MathContext.DECIMAL64;

    public enum PrepaymentStrategy {
        REDUCE_EMI,
        REDUCE_TENURE
    }

    /**
     * Processes a partial prepayment.
     * Calculates the new schedule based on the strategy.
     */
    public PrepaymentResult processPrepayment(AccountantCredit credit, List<CreditInstallment> existingSchedule, BigDecimal prepaymentAmount, PrepaymentStrategy strategy, LocalDate prepaymentDate) {
        
        BigDecimal currentPrincipal = credit.getOutstandingAmount();
        BigDecimal newPrincipal = currentPrincipal.subtract(prepaymentAmount);
        
        if (newPrincipal.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Prepayment amount exceeds or equals outstanding principal. Use Foreclosure instead.");
        }

        // Count remaining pending installments
        long remainingMonths = existingSchedule.stream().filter(i -> "PENDING".equals(i.getStatus())).count();
        if (remainingMonths == 0) {
            throw new IllegalStateException("No pending installments to prepay against.");
        }

        List<CreditInstallment> newPendingSchedule = new ArrayList<>();
        BigDecimal annualRate = credit.getInterestRate();
        BigDecimal monthlyRate = annualRate != null ? annualRate.divide(BigDecimal.valueOf(1200), MC) : BigDecimal.ZERO;
        
        LocalDate nextDueDate = existingSchedule.stream()
                .filter(i -> "PENDING".equals(i.getStatus()))
                .map(CreditInstallment::getDueDate)
                .min(LocalDate::compareTo)
                .orElse(prepaymentDate.plusMonths(1));

        if (strategy == PrepaymentStrategy.REDUCE_EMI) {
            // Keep tenure same, recalculate EMI
            if (monthlyRate.compareTo(BigDecimal.ZERO) == 0) {
                BigDecimal newEmi = newPrincipal.divide(BigDecimal.valueOf(remainingMonths), MC).setScale(2, RoundingMode.HALF_UP);
                newPendingSchedule = generateZeroInterestSchedule(credit, newPrincipal, (int) remainingMonths, nextDueDate, newEmi);
            } else {
                BigDecimal onePlusRToN = BigDecimal.ONE.add(monthlyRate).pow((int) remainingMonths, MC);
                BigDecimal emi = newPrincipal.multiply(monthlyRate, MC).multiply(onePlusRToN, MC)
                        .divide(onePlusRToN.subtract(BigDecimal.ONE, MC), MC).setScale(2, RoundingMode.HALF_UP);
                newPendingSchedule = generateReducingSchedule(credit, newPrincipal, monthlyRate, (int) remainingMonths, nextDueDate, emi);
            }
        } else if (strategy == PrepaymentStrategy.REDUCE_TENURE) {
            // Keep EMI same, reduce tenure
            BigDecimal currentEmi = credit.getInstallmentAmount();
            if (monthlyRate.compareTo(BigDecimal.ZERO) == 0) {
                int newMonths = newPrincipal.divide(currentEmi, RoundingMode.CEILING).intValue();
                newPendingSchedule = generateZeroInterestSchedule(credit, newPrincipal, newMonths, nextDueDate, currentEmi);
            } else {
                // n = -log(1 - P*r/EMI) / log(1+r)
                BigDecimal pR = newPrincipal.multiply(monthlyRate, MC);
                BigDecimal pROverEmi = pR.divide(currentEmi, MC);
                if (pROverEmi.compareTo(BigDecimal.ONE) >= 0) {
                    throw new IllegalStateException("EMI is too small to cover interest. Loan will never close.");
                }
                double n = -Math.log(BigDecimal.ONE.subtract(pROverEmi, MC).doubleValue()) / Math.log(BigDecimal.ONE.add(monthlyRate, MC).doubleValue());
                int newMonths = (int) Math.ceil(n);
                newPendingSchedule = generateReducingSchedule(credit, newPrincipal, monthlyRate, newMonths, nextDueDate, currentEmi);
            }
        }

        // Ledger Entry
        LoanLedger ledger = new LoanLedger();
        ledger.setCredit(credit);
        ledger.setTransactionType("PARTIAL_PAYMENT");
        ledger.setPrincipalComponent(prepaymentAmount);
        ledger.setTotalAmount(prepaymentAmount);
        ledger.setBalanceBefore(currentPrincipal);
        ledger.setBalanceAfter(newPrincipal);

        credit.setOutstandingAmount(newPrincipal);
        credit.setAmountRepaid(credit.getAmountRepaid().add(prepaymentAmount));

        PrepaymentResult result = new PrepaymentResult();
        result.setLedgerEntry(ledger);
        result.setNewPendingSchedule(newPendingSchedule);
        result.setUpdatedCredit(credit);
        
        return result;
    }

    private List<CreditInstallment> generateReducingSchedule(AccountantCredit credit, BigDecimal principal, BigDecimal monthlyRate, int months, LocalDate startDate, BigDecimal emi) {
        List<CreditInstallment> schedule = new ArrayList<>();
        BigDecimal remainingPrincipal = principal;

        for (int i = 1; i <= months; i++) {
            CreditInstallment inst = new CreditInstallment();
            inst.setCredit(credit);
            inst.setInstallmentNum(i); // We might need to offset this with existing paid installments later
            inst.setDueDate(startDate.plusMonths(i - 1));
            inst.setStatus("PENDING");
            inst.setPaidAmount(BigDecimal.ZERO);

            BigDecimal interestPortion = remainingPrincipal.multiply(monthlyRate, MC).setScale(2, RoundingMode.HALF_UP);
            BigDecimal principalPortion = emi.subtract(interestPortion);

            if (i == months || remainingPrincipal.subtract(principalPortion).compareTo(BigDecimal.ZERO) <= 0) {
                principalPortion = remainingPrincipal;
                emi = principalPortion.add(interestPortion);
                remainingPrincipal = BigDecimal.ZERO;
            } else {
                remainingPrincipal = remainingPrincipal.subtract(principalPortion);
            }

            inst.setInstallmentAmount(emi);
            inst.setPrincipalPortion(principalPortion);
            inst.setInterestPortion(interestPortion);
            inst.setRemainingPrincipal(remainingPrincipal);
            
            schedule.add(inst);
            if (remainingPrincipal.compareTo(BigDecimal.ZERO) == 0) break;
        }
        return schedule;
    }

    private List<CreditInstallment> generateZeroInterestSchedule(AccountantCredit credit, BigDecimal principal, int months, LocalDate startDate, BigDecimal emi) {
        List<CreditInstallment> schedule = new ArrayList<>();
        BigDecimal remainingPrincipal = principal;

        for (int i = 1; i <= months; i++) {
            CreditInstallment inst = new CreditInstallment();
            inst.setCredit(credit);
            inst.setInstallmentNum(i);
            inst.setDueDate(startDate.plusMonths(i - 1));
            inst.setStatus("PENDING");
            inst.setPaidAmount(BigDecimal.ZERO);

            BigDecimal currentPrin = emi;
            if (i == months || remainingPrincipal.subtract(currentPrin).compareTo(BigDecimal.ZERO) <= 0) {
                currentPrin = remainingPrincipal;
                emi = currentPrin;
                remainingPrincipal = BigDecimal.ZERO;
            } else {
                remainingPrincipal = remainingPrincipal.subtract(currentPrin);
            }

            inst.setInstallmentAmount(emi);
            inst.setPrincipalPortion(currentPrin);
            inst.setInterestPortion(BigDecimal.ZERO);
            inst.setRemainingPrincipal(remainingPrincipal);
            
            schedule.add(inst);
            if (remainingPrincipal.compareTo(BigDecimal.ZERO) == 0) break;
        }
        return schedule;
    }

    public static class PrepaymentResult {
        private LoanLedger ledgerEntry;
        private List<CreditInstallment> newPendingSchedule;
        private AccountantCredit updatedCredit;

        // Getters and Setters
        public LoanLedger getLedgerEntry() { return ledgerEntry; }
        public void setLedgerEntry(LoanLedger ledgerEntry) { this.ledgerEntry = ledgerEntry; }
        public List<CreditInstallment> getNewPendingSchedule() { return newPendingSchedule; }
        public void setNewPendingSchedule(List<CreditInstallment> newPendingSchedule) { this.newPendingSchedule = newPendingSchedule; }
        public AccountantCredit getUpdatedCredit() { return updatedCredit; }
        public void setUpdatedCredit(AccountantCredit updatedCredit) { this.updatedCredit = updatedCredit; }
    }
}
