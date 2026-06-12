package com.cc.controller;

import com.cc.modals.AccountantCredit;
import com.cc.repository.AccountantCreditRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/accountant/liability-dashboard")
public class LiabilityDashboardController {

    @Autowired
    private AccountantCreditRepository accountantCreditRepository;

    @Autowired
    private com.cc.repository.CreditInstallmentRepository creditInstallmentRepository;

    @GetMapping("/{username}/summary")
    public ResponseEntity<?> getDashboardSummary(@PathVariable String username) {
        List<AccountantCredit> allCredits = accountantCreditRepository.findByAccountantUsernameOrderByCreditDateDesc(username);
        
        BigDecimal totalBorrowed = BigDecimal.ZERO;
        BigDecimal totalOutstanding = BigDecimal.ZERO;
        BigDecimal totalRepaid = BigDecimal.ZERO;
        int activeLoans = 0;
        int criticalLoans = 0;

        Map<String, BigDecimal> purposeDistribution = new HashMap<>();

        for (AccountantCredit credit : allCredits) {
            totalBorrowed = totalBorrowed.add(credit.getAmount() != null ? credit.getAmount() : BigDecimal.ZERO);
            totalOutstanding = totalOutstanding.add(credit.getOutstandingAmount() != null ? credit.getOutstandingAmount() : BigDecimal.ZERO);
            totalRepaid = totalRepaid.add(credit.getAmountRepaid() != null ? credit.getAmountRepaid() : BigDecimal.ZERO);
            
            if ("ACTIVE".equals(credit.getStatus())) {
                activeLoans++;
                if ("CRITICAL".equals(credit.getHealthScore())) {
                    criticalLoans++;
                }

                String purpose = credit.getPurpose() != null ? credit.getPurpose() : "Other";
                BigDecimal outstanding = credit.getOutstandingAmount() != null ? credit.getOutstandingAmount() : BigDecimal.ZERO;
                purposeDistribution.put(purpose, purposeDistribution.getOrDefault(purpose, BigDecimal.ZERO).add(outstanding));
            }
        }

        // Aging and Forecast Logic
        List<com.cc.modals.CreditInstallment> pendingInstallments = creditInstallmentRepository.findByCreditAccountantUsernameAndStatusIn(username, java.util.Arrays.asList("PENDING", "OVERDUE"));
        
        Map<String, BigDecimal> agingBuckets = new HashMap<>();
        agingBuckets.put("0-30", BigDecimal.ZERO);
        agingBuckets.put("31-60", BigDecimal.ZERO);
        agingBuckets.put("61-90", BigDecimal.ZERO);
        agingBuckets.put("90+", BigDecimal.ZERO);

        Map<String, BigDecimal> cashFlowForecast = new HashMap<>();
        cashFlowForecast.put("next30", BigDecimal.ZERO);
        cashFlowForecast.put("next60", BigDecimal.ZERO);
        cashFlowForecast.put("next90", BigDecimal.ZERO);

        java.time.LocalDate today = java.time.LocalDate.now();

        for (com.cc.modals.CreditInstallment inst : pendingInstallments) {
            BigDecimal remaining = inst.getInstallmentAmount().subtract(inst.getPaidAmount() != null ? inst.getPaidAmount() : BigDecimal.ZERO);
            if (remaining.compareTo(BigDecimal.ZERO) <= 0) continue;

            if (inst.getDueDate().isBefore(today)) {
                // Overdue - calculate aging
                long daysOverdue = java.time.temporal.ChronoUnit.DAYS.between(inst.getDueDate(), today);
                if (daysOverdue <= 30) agingBuckets.put("0-30", agingBuckets.get("0-30").add(remaining));
                else if (daysOverdue <= 60) agingBuckets.put("31-60", agingBuckets.get("31-60").add(remaining));
                else if (daysOverdue <= 90) agingBuckets.put("61-90", agingBuckets.get("61-90").add(remaining));
                else agingBuckets.put("90+", agingBuckets.get("90+").add(remaining));
            } else {
                // Future - calculate forecast
                long daysUntilDue = java.time.temporal.ChronoUnit.DAYS.between(today, inst.getDueDate());
                if (daysUntilDue <= 30) cashFlowForecast.put("next30", cashFlowForecast.get("next30").add(remaining));
                else if (daysUntilDue <= 60) cashFlowForecast.put("next60", cashFlowForecast.get("next60").add(remaining));
                else if (daysUntilDue <= 90) cashFlowForecast.put("next90", cashFlowForecast.get("next90").add(remaining));
            }
        }

        Map<String, Object> summary = new HashMap<>();
        summary.put("totalBorrowed", totalBorrowed);
        summary.put("totalOutstanding", totalOutstanding);
        summary.put("totalRepaid", totalRepaid);
        summary.put("activeLoansCount", activeLoans);
        summary.put("criticalLoansCount", criticalLoans);
        summary.put("purposeDistribution", purposeDistribution);
        summary.put("agingAnalysis", agingBuckets);
        summary.put("cashFlowForecast", cashFlowForecast);

        // Credit Card Analytics
        BigDecimal totalCardLimit = BigDecimal.ZERO;
        BigDecimal totalCardOutstanding = BigDecimal.ZERO;
        for (AccountantCredit credit : allCredits) {
            if ("CREDIT_CARD".equals(credit.getMethod()) && "ACTIVE".equals(credit.getStatus())) {
                totalCardLimit = totalCardLimit.add(credit.getCardLimit() != null ? credit.getCardLimit() : BigDecimal.ZERO);
                totalCardOutstanding = totalCardOutstanding.add(credit.getOutstandingAmount() != null ? credit.getOutstandingAmount() : BigDecimal.ZERO);
            }
        }
        
        BigDecimal limitUtilization = BigDecimal.ZERO;
        if (totalCardLimit.compareTo(BigDecimal.ZERO) > 0) {
            limitUtilization = totalCardOutstanding.multiply(new BigDecimal("100")).divide(totalCardLimit, 2, java.math.RoundingMode.HALF_UP);
        }
        
        Map<String, Object> creditCardAnalytics = new HashMap<>();
        creditCardAnalytics.put("totalCardLimit", totalCardLimit);
        creditCardAnalytics.put("totalCardOutstanding", totalCardOutstanding);
        creditCardAnalytics.put("limitUtilization", limitUtilization);
        
        summary.put("creditCardAnalytics", creditCardAnalytics);

        return ResponseEntity.ok(summary);
    }

    @GetMapping("/{username}/calendar")
    public ResponseEntity<?> getCalendarInstallments(@PathVariable String username) {
        List<com.cc.modals.CreditInstallment> pendingInstallments = creditInstallmentRepository.findByCreditAccountantUsernameAndStatusIn(username, java.util.Arrays.asList("PENDING", "OVERDUE"));
        // Sort by due date
        pendingInstallments.sort((a, b) -> a.getDueDate().compareTo(b.getDueDate()));
        return ResponseEntity.ok(pendingInstallments);
    }
}
