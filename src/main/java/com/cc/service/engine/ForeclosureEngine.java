package com.cc.service.engine;

import com.cc.modals.AccountantCredit;
import com.cc.modals.CreditInstallment;
import com.cc.modals.LoanLedger;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class ForeclosureEngine {

    /**
     * Executes foreclosure on a loan.
     * Calculates the final settlement amount, marks future installments as FORECLOSED,
     * updates the credit status, and generates the final ledger entry.
     */
    public ForeclosureResult processForeclosure(AccountantCredit credit, List<CreditInstallment> installments, BigDecimal foreclosureChargePercentage) {
        
        BigDecimal outstandingPrincipal = credit.getOutstandingAmount();
        BigDecimal foreclosureCharges = BigDecimal.ZERO;
        
        if (foreclosureChargePercentage != null && foreclosureChargePercentage.compareTo(BigDecimal.ZERO) > 0) {
            foreclosureCharges = outstandingPrincipal.multiply(foreclosureChargePercentage).divide(BigDecimal.valueOf(100));
        }

        // Find pending overdues (past due date, not paid)
        BigDecimal overdueAmount = BigDecimal.ZERO;
        BigDecimal penaltyAmount = BigDecimal.ZERO; // Simplified, assuming penalties are tracked on installments or ledger
        
        for (CreditInstallment inst : installments) {
            if ("OVERDUE".equals(inst.getStatus())) {
                overdueAmount = overdueAmount.add(inst.getInstallmentAmount().subtract(inst.getPaidAmount()));
            }
        }

        BigDecimal netSettlementAmount = outstandingPrincipal.add(foreclosureCharges).add(overdueAmount).add(penaltyAmount);

        // Mutate installments - mark all non-paid as FORECLOSED
        BigDecimal futureInterestSavings = BigDecimal.ZERO;
        for (CreditInstallment inst : installments) {
            if ("PENDING".equals(inst.getStatus()) || "OVERDUE".equals(inst.getStatus())) {
                inst.setStatus("FORECLOSED");
                futureInterestSavings = futureInterestSavings.add(inst.getInterestPortion());
            }
        }

        // Mutate credit
        credit.setStatus("FORECLOSED");
        credit.setOutstandingAmount(BigDecimal.ZERO);
        credit.setAmountRepaid(credit.getAmountRepaid().add(netSettlementAmount));

        // Create Ledger Entry
        LoanLedger ledger = new LoanLedger();
        ledger.setCredit(credit);
        ledger.setTransactionType("FORECLOSURE");
        ledger.setPrincipalComponent(outstandingPrincipal);
        ledger.setInterestComponent(overdueAmount); // overdues might contain interest, simplified here
        ledger.setPenaltyComponent(foreclosureCharges.add(penaltyAmount));
        ledger.setTotalAmount(netSettlementAmount);
        
        ForeclosureResult result = new ForeclosureResult();
        result.setSettlementAmount(netSettlementAmount);
        result.setOutstandingPrincipal(outstandingPrincipal);
        result.setForeclosureCharges(foreclosureCharges);
        result.setFutureInterestSavings(futureInterestSavings);
        result.setLedgerEntry(ledger);
        result.setUpdatedCredit(credit);
        result.setUpdatedInstallments(installments);

        return result;
    }

    public static class ForeclosureResult {
        private BigDecimal settlementAmount;
        private BigDecimal outstandingPrincipal;
        private BigDecimal foreclosureCharges;
        private BigDecimal futureInterestSavings;
        private LoanLedger ledgerEntry;
        private AccountantCredit updatedCredit;
        private List<CreditInstallment> updatedInstallments;

        // Getters and Setters
        public BigDecimal getSettlementAmount() { return settlementAmount; }
        public void setSettlementAmount(BigDecimal settlementAmount) { this.settlementAmount = settlementAmount; }
        public BigDecimal getOutstandingPrincipal() { return outstandingPrincipal; }
        public void setOutstandingPrincipal(BigDecimal outstandingPrincipal) { this.outstandingPrincipal = outstandingPrincipal; }
        public BigDecimal getForeclosureCharges() { return foreclosureCharges; }
        public void setForeclosureCharges(BigDecimal foreclosureCharges) { this.foreclosureCharges = foreclosureCharges; }
        public BigDecimal getFutureInterestSavings() { return futureInterestSavings; }
        public void setFutureInterestSavings(BigDecimal futureInterestSavings) { this.futureInterestSavings = futureInterestSavings; }
        public LoanLedger getLedgerEntry() { return ledgerEntry; }
        public void setLedgerEntry(LoanLedger ledgerEntry) { this.ledgerEntry = ledgerEntry; }
        public AccountantCredit getUpdatedCredit() { return updatedCredit; }
        public void setUpdatedCredit(AccountantCredit updatedCredit) { this.updatedCredit = updatedCredit; }
        public List<CreditInstallment> getUpdatedInstallments() { return updatedInstallments; }
        public void setUpdatedInstallments(List<CreditInstallment> updatedInstallments) { this.updatedInstallments = updatedInstallments; }
    }
}
