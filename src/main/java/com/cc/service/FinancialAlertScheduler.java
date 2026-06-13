package com.cc.service;

import com.cc.modals.AccountantCredit;
import com.cc.modals.CreditInstallment;
import com.cc.modals.FinancialAlert;
import com.cc.repository.AccountantCreditRepository;
import com.cc.repository.CreditInstallmentRepository;
import com.cc.repository.FinancialAlertRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class FinancialAlertScheduler {

    @Autowired
    private CreditInstallmentRepository installmentRepository;
    
    @Autowired
    private AccountantCreditRepository creditRepository;
    
    @Autowired
    private FinancialAlertRepository alertRepository;

    // Run every day at 8:00 AM
    @Scheduled(cron = "0 0 8 * * ?")
    @Transactional
    public void generateDailyFinancialAlerts() {
        LocalDate today = LocalDate.now();
        LocalDate tomorrow = today.plusDays(1);
        
        // 1. Due Today Alerts
        List<CreditInstallment> dueToday = installmentRepository.findByStatusAndDueDate("PENDING", today);
        for (CreditInstallment inst : dueToday) {
            createAlert(
                inst.getCredit().getAccountantUsername(),
                inst.getCredit().getId(),
                "DUE_TODAY",
                "EMI Due Today",
                "Installment of ₹" + inst.getInstallmentAmount() + " is due today for " + getLenderName(inst.getCredit()) + " (Ref: CR-" + inst.getCredit().getId() + ")."
            );
        }
        
        // 2. Overdue Alerts
        List<CreditInstallment> overdue = installmentRepository.findByStatusAndDueDateBefore("PENDING", today);
        for (CreditInstallment inst : overdue) {
            // we probably don't want to spam every day. Let's just create one if it became overdue yesterday.
            if (inst.getDueDate().equals(today.minusDays(1))) {
                createAlert(
                    inst.getCredit().getAccountantUsername(),
                    inst.getCredit().getId(),
                    "OVERDUE",
                    "EMI Overdue",
                    "Installment of ₹" + inst.getInstallmentAmount() + " due on " + inst.getDueDate() + " is now overdue for " + getLenderName(inst.getCredit()) + "."
                );
            }
        }
        
        // 3. Foreclosure Opportunity Alerts (e.g. if Cash Flow is high, but we can't easily check user balance here, 
        //    so let's alert if a loan has < 10% outstanding left).
        List<AccountantCredit> activeCredits = creditRepository.findAll();
        for (AccountantCredit credit : activeCredits) {
            if ("ACTIVE".equals(credit.getStatus()) && credit.getAmount() != null && credit.getAmount().compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal tenPercent = credit.getAmount().multiply(new BigDecimal("0.10"));
                if (credit.getOutstandingAmount() != null && credit.getOutstandingAmount().compareTo(BigDecimal.ZERO) > 0 && credit.getOutstandingAmount().compareTo(tenPercent) <= 0) {
                    // Check if alert already exists recently to avoid spam
                    // For simplicity, we just create it. In a real app we'd check if an alert was already sent.
                    boolean hasAlert = alertRepository.findByAccountantUsernameOrderByCreatedAtDesc(credit.getAccountantUsername())
                            .stream().anyMatch(a -> "FORECLOSURE".equals(a.getAlertType()) && credit.getId().equals(a.getCreditId()));
                    if (!hasAlert) {
                        createAlert(
                            credit.getAccountantUsername(),
                            credit.getId(),
                            "FORECLOSURE",
                            "Foreclosure Opportunity",
                            "Outstanding balance for " + getLenderName(credit) + " is only ₹" + credit.getOutstandingAmount() + " (< 10%). Consider foreclosing it to save on interest."
                        );
                    }
                }
            }
            
            // Credit Card Statement Generation Alert
            if ("CREDIT_CARD".equals(credit.getMethod()) && "ACTIVE".equals(credit.getStatus()) && credit.getStatementDate() != null) {
                if (credit.getStatementDate().getDayOfMonth() == tomorrow.getDayOfMonth()) {
                    createAlert(
                        credit.getAccountantUsername(),
                        credit.getId(),
                        "GENERAL",
                        "Credit Card Statement Tomorrow",
                        "Statement for Credit Card (" + credit.getCardInfo() + ") will be generated tomorrow."
                    );
                }
            }
        }
    }
    
    private String getLenderName(AccountantCredit credit) {
        if ("FRIEND".equals(credit.getMethod())) return credit.getFriendName();
        if ("CREDIT_CARD".equals(credit.getMethod())) return credit.getCardInfo();
        return credit.getBankName();
    }
    
    private void createAlert(String username, Long creditId, String type, String title, String msg) {
        // Idempotency check: don't create if exact message already exists
        boolean exists = alertRepository.findByAccountantUsernameOrderByCreatedAtDesc(username)
                .stream()
                .anyMatch(a -> a.getMessage().equals(msg) && a.getCreditId().equals(creditId));
        if (exists) return;

        FinancialAlert alert = new FinancialAlert();
        alert.setAccountantUsername(username);
        alert.setCreditId(creditId);
        alert.setAlertType(type);
        alert.setTitle(title);
        alert.setMessage(msg);
        alertRepository.save(alert);
    }
}
