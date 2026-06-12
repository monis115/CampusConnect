package com.cc.service.engine;

import com.cc.modals.AccountantCredit;
import com.cc.modals.CreditInstallment;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class AmortizationEngine {

    private static final MathContext MC = MathContext.DECIMAL64;

    /**
     * Generates a full amortization schedule based on the interest model.
     * Does NOT save the installments to the database.
     */
    public List<CreditInstallment> generateSchedule(AccountantCredit credit, LocalDate startDate) {
        if ("FLAT".equalsIgnoreCase(credit.getInterestModel())) {
            return generateFlatSchedule(credit, startDate);
        } else {
            return generateReducingSchedule(credit, startDate);
        }
    }

    private List<CreditInstallment> generateReducingSchedule(AccountantCredit credit, LocalDate startDate) {
        List<CreditInstallment> schedule = new ArrayList<>();
        
        BigDecimal principal = credit.getAmount();
        BigDecimal annualRate = credit.getInterestRate();
        int months = credit.getLoanTenureMonths() != null ? credit.getLoanTenureMonths() : 1;

        if (annualRate == null || annualRate.compareTo(BigDecimal.ZERO) == 0) {
            return generateZeroInterestSchedule(credit, startDate);
        }

        // r = Annual Rate / 12 / 100
        BigDecimal monthlyRate = annualRate.divide(BigDecimal.valueOf(1200), MC);
        
        // (1 + r)^n
        BigDecimal onePlusRToN = BigDecimal.ONE.add(monthlyRate).pow(months, MC);
        
        // EMI = P * r * (1 + r)^n / ((1 + r)^n - 1)
        BigDecimal numerator = principal.multiply(monthlyRate, MC).multiply(onePlusRToN, MC);
        BigDecimal denominator = onePlusRToN.subtract(BigDecimal.ONE, MC);
        BigDecimal emi = numerator.divide(denominator, MC).setScale(2, RoundingMode.HALF_UP);

        BigDecimal remainingPrincipal = principal;
        BigDecimal totalInterestAcc = BigDecimal.ZERO;

        for (int i = 1; i <= months; i++) {
            CreditInstallment inst = new CreditInstallment();
            inst.setCredit(credit);
            inst.setInstallmentNum(i);
            inst.setDueDate(startDate.plusMonths(i - 1));
            if (inst.getDueDate().isBefore(LocalDate.now())) {
                inst.setStatus("OVERDUE");
            } else {
                inst.setStatus("PENDING");
            }
            inst.setPaidAmount(BigDecimal.ZERO);

            BigDecimal interestPortion = remainingPrincipal.multiply(monthlyRate, MC).setScale(2, RoundingMode.HALF_UP);
            BigDecimal principalPortion = emi.subtract(interestPortion);

            // Handle rounding differences on the final installment
            if (i == months) {
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
            
            totalInterestAcc = totalInterestAcc.add(interestPortion);
            // We'll calculate remaining interest recursively after building the schedule
            
            schedule.add(inst);
        }

        // Backfill remaining interest
        BigDecimal currentRemainingInterest = totalInterestAcc;
        for (CreditInstallment inst : schedule) {
            currentRemainingInterest = currentRemainingInterest.subtract(inst.getInterestPortion());
            inst.setRemainingInterest(currentRemainingInterest.max(BigDecimal.ZERO));
        }

        credit.setInstallmentAmount(schedule.get(0).getInstallmentAmount());
        credit.setTotalRepaymentAmount(principal.add(totalInterestAcc));

        return schedule;
    }

    private List<CreditInstallment> generateFlatSchedule(AccountantCredit credit, LocalDate startDate) {
        List<CreditInstallment> schedule = new ArrayList<>();
        
        BigDecimal principal = credit.getAmount();
        BigDecimal annualRate = credit.getInterestRate();
        int months = credit.getLoanTenureMonths() != null ? credit.getLoanTenureMonths() : 1;

        if (annualRate == null || annualRate.compareTo(BigDecimal.ZERO) == 0) {
            return generateZeroInterestSchedule(credit, startDate);
        }

        // Total Interest = P * (R / 100) * (Months / 12)
        BigDecimal timeInYears = BigDecimal.valueOf(months).divide(BigDecimal.valueOf(12), MC);
        BigDecimal rateDecimal = annualRate.divide(BigDecimal.valueOf(100), MC);
        BigDecimal totalInterest = principal.multiply(rateDecimal, MC).multiply(timeInYears, MC).setScale(2, RoundingMode.HALF_UP);
        
        BigDecimal emi = principal.add(totalInterest).divide(BigDecimal.valueOf(months), MC).setScale(2, RoundingMode.HALF_UP);
        BigDecimal principalPortion = principal.divide(BigDecimal.valueOf(months), MC).setScale(2, RoundingMode.HALF_UP);
        BigDecimal interestPortion = totalInterest.divide(BigDecimal.valueOf(months), MC).setScale(2, RoundingMode.HALF_UP);

        BigDecimal remainingPrincipal = principal;
        BigDecimal remainingInterest = totalInterest;

        for (int i = 1; i <= months; i++) {
            CreditInstallment inst = new CreditInstallment();
            inst.setCredit(credit);
            inst.setInstallmentNum(i);
            inst.setDueDate(startDate.plusMonths(i - 1));
            if (inst.getDueDate().isBefore(LocalDate.now())) {
                inst.setStatus("OVERDUE");
            } else {
                inst.setStatus("PENDING");
            }
            inst.setPaidAmount(BigDecimal.ZERO);

            BigDecimal currentPrin = principalPortion;
            BigDecimal currentInt = interestPortion;

            if (i == months) {
                currentPrin = remainingPrincipal;
                currentInt = remainingInterest;
                emi = currentPrin.add(currentInt);
                remainingPrincipal = BigDecimal.ZERO;
                remainingInterest = BigDecimal.ZERO;
            } else {
                remainingPrincipal = remainingPrincipal.subtract(currentPrin);
                remainingInterest = remainingInterest.subtract(currentInt);
            }

            inst.setInstallmentAmount(emi);
            inst.setPrincipalPortion(currentPrin);
            inst.setInterestPortion(currentInt);
            inst.setRemainingPrincipal(remainingPrincipal);
            inst.setRemainingInterest(remainingInterest);

            schedule.add(inst);
        }

        credit.setInstallmentAmount(schedule.get(0).getInstallmentAmount());
        credit.setTotalRepaymentAmount(principal.add(totalInterest));

        return schedule;
    }

    private List<CreditInstallment> generateZeroInterestSchedule(AccountantCredit credit, LocalDate startDate) {
        List<CreditInstallment> schedule = new ArrayList<>();
        BigDecimal principal = credit.getAmount();
        int months = credit.getLoanTenureMonths() != null ? credit.getLoanTenureMonths() : 1;
        
        BigDecimal emi = principal.divide(BigDecimal.valueOf(months), MC).setScale(2, RoundingMode.HALF_UP);
        BigDecimal remainingPrincipal = principal;

        for (int i = 1; i <= months; i++) {
            CreditInstallment inst = new CreditInstallment();
            inst.setCredit(credit);
            inst.setInstallmentNum(i);
            inst.setDueDate(startDate.plusMonths(i - 1));
            if (inst.getDueDate().isBefore(LocalDate.now())) {
                inst.setStatus("OVERDUE");
            } else {
                inst.setStatus("PENDING");
            }
            inst.setPaidAmount(BigDecimal.ZERO);

            BigDecimal currentPrin = emi;
            if (i == months) {
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
            inst.setRemainingInterest(BigDecimal.ZERO);

            schedule.add(inst);
        }
        
        credit.setInstallmentAmount(schedule.get(0).getInstallmentAmount());
        credit.setTotalRepaymentAmount(principal);
        
        return schedule;
    }
}
