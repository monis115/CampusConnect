package com.cc.controller;

import com.cc.modals.AccountantCredit;
import com.cc.service.AccountantCreditService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/accountant/credits")
public class AccountantCreditController {

    @Autowired
    private AccountantCreditService accountantCreditService;

    @GetMapping("/{username}")
    public ResponseEntity<List<AccountantCredit>> getAllCredits(@PathVariable String username) {
        return ResponseEntity.ok(accountantCreditService.getAllCreditsForAccountant(username));
    }

    @PostMapping("/{username}")
    public ResponseEntity<AccountantCredit> addCredit(@RequestBody AccountantCredit credit,
            @PathVariable String username) {
        try {
            AccountantCredit savedCredit = accountantCreditService.addCredit(credit, username);
            return ResponseEntity.ok(savedCredit);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @PostMapping("/{creditId}/repay")
    public ResponseEntity<?> recordRepayment(
            @PathVariable Long creditId,
            @RequestParam(required = false) Long subAccountId,
            @RequestParam java.math.BigDecimal amount,
            @RequestParam(required = false) String notes,
            @RequestParam(defaultValue = "SYSTEM") String username) {
        try {
            return ResponseEntity.ok(accountantCreditService.recordRepayment(creditId, subAccountId, amount, notes, username));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/{creditId}/foreclose")
    public ResponseEntity<?> processForeclosure(
            @PathVariable Long creditId,
            @RequestParam(required = false) java.math.BigDecimal foreclosureChargePercentage,
            @RequestParam(required = false) Long subAccountId,
            @RequestParam(defaultValue = "SYSTEM") String username) {
        try {
            return ResponseEntity.ok(accountantCreditService.processForeclosure(creditId, foreclosureChargePercentage, subAccountId, username));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/{creditId}/prepay")
    public ResponseEntity<?> processPrepayment(
            @PathVariable Long creditId,
            @RequestParam java.math.BigDecimal amount,
            @RequestParam com.cc.service.engine.PrepaymentEngine.PrepaymentStrategy strategy,
            @RequestParam(defaultValue = "SYSTEM") String username) {
        try {
            return ResponseEntity.ok(accountantCreditService.processPrepayment(creditId, amount, strategy, username));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/{creditId}/installments")
    public ResponseEntity<?> getCreditInstallments(@PathVariable Long creditId) {
        return ResponseEntity.ok(accountantCreditService.getCreditInstallments(creditId));
    }

    @GetMapping("/{creditId}/ledger")
    public ResponseEntity<?> getCreditLedger(@PathVariable Long creditId) {
        return ResponseEntity.ok(accountantCreditService.getCreditTransactions(creditId));
    }
    
    @GetMapping("/{creditId}/timeline")
    public ResponseEntity<?> getCreditTimeline(@PathVariable Long creditId) {
        return ResponseEntity.ok(accountantCreditService.getTimelineEvents(creditId));
    }

    @PutMapping("/{creditId}/details")
    public ResponseEntity<?> updateCreditDetails(@PathVariable Long creditId, @RequestBody com.cc.DTO.CreditDetailsUpdateRequest request) {
        try {
            return ResponseEntity.ok(accountantCreditService.updateCreditDetails(creditId, request));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @Autowired
    private com.cc.service.PdfGenerationService pdfGenerationService;

    @GetMapping("/{creditId}/closure-certificate")
    public ResponseEntity<byte[]> downloadClosureCertificate(@PathVariable Long creditId) {
        try {
            byte[] pdfBytes = pdfGenerationService.generateClosureCertificate(creditId);
            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.setContentType(org.springframework.http.MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "Closure_Certificate_CR" + creditId + ".pdf");
            return new ResponseEntity<>(pdfBytes, headers, org.springframework.http.HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }
    @Autowired
    private com.cc.repository.CreditInstallmentRepository creditInstallmentRepository;

    @GetMapping("/calendar/{username}")
    public ResponseEntity<?> getLiabilityCalendar(@PathVariable String username) {
        // Fetch all pending/overdue installments for the accountant across all active loans
        java.util.List<com.cc.modals.CreditInstallment> pending = creditInstallmentRepository
            .findByCreditAccountantUsernameAndStatusIn(username, java.util.Arrays.asList("PENDING", "OVERDUE"));
        
        // Sort by due date
        pending.sort(java.util.Comparator.comparing(com.cc.modals.CreditInstallment::getDueDate));
        
        return ResponseEntity.ok(pending);
    }
    
    @Autowired
    private com.cc.repository.FinancialAlertRepository financialAlertRepository;
    
    @Autowired
    private com.cc.service.FinancialAlertScheduler financialAlertScheduler;
    
    @GetMapping("/alerts/{username}")
    public ResponseEntity<?> getFinancialAlerts(@PathVariable String username) {
        // Automatically run system checks to ensure latest alerts are generated dynamically
        financialAlertScheduler.generateDailyFinancialAlerts();
        
        java.util.List<com.cc.modals.FinancialAlert> alerts = financialAlertRepository
            .findByAccountantUsernameOrderByCreatedAtDesc(username);
        return ResponseEntity.ok(alerts);
    }
    
    @PostMapping("/alerts/{alertId}/read")
    public ResponseEntity<?> markAlertAsRead(@PathVariable Long alertId) {
        com.cc.modals.FinancialAlert alert = financialAlertRepository.findById(alertId).orElse(null);
        if (alert != null) {
            alert.setRead(true);
            financialAlertRepository.save(alert);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }
}
