package com.cc.service.engine;

import com.cc.modals.AccountantCredit;
import com.cc.modals.LoanLedger;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

@Service
public class InterestAccrualEngine {

    private static final MathContext MC = MathContext.DECIMAL64;

    /**
     * Calculates daily interest accrual.
     * Formula: Principal * (Annual Rate / 100) / 365
     */
    public LoanLedger calculateDailyAccrual(AccountantCredit credit) {
        BigDecimal principal = credit.getOutstandingAmount();
        BigDecimal annualRate = credit.getInterestRate();

        if (principal == null || principal.compareTo(BigDecimal.ZERO) <= 0 ||
            annualRate == null || annualRate.compareTo(BigDecimal.ZERO) <= 0) {
            return null;
        }

        BigDecimal dailyRate = annualRate.divide(BigDecimal.valueOf(36500), MC);
        BigDecimal dailyInterest = principal.multiply(dailyRate, MC).setScale(4, RoundingMode.HALF_UP);

        if (dailyInterest.compareTo(BigDecimal.ZERO) <= 0) {
            return null;
        }

        LoanLedger ledger = new LoanLedger();
        ledger.setCredit(credit);
        ledger.setTransactionType("ACCRUAL");
        ledger.setInterestComponent(dailyInterest);
        ledger.setTotalAmount(dailyInterest);
        ledger.setBalanceBefore(principal);
        ledger.setBalanceAfter(principal); // Accrued interest typically tracked separately in core banking until capitalized
        
        return ledger;
    }
}
