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
            @RequestParam Double amount,
            @RequestParam(required = false) String notes) {
        try {
            return ResponseEntity.ok(accountantCreditService.recordRepayment(creditId, subAccountId, amount, notes));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/{creditId}/installments")
    public ResponseEntity<?> getCreditInstallments(@PathVariable Long creditId) {
        return ResponseEntity.ok(accountantCreditService.getCreditInstallments(creditId));
    }

    @GetMapping("/{creditId}/transactions")
    public ResponseEntity<?> getCreditTransactions(@PathVariable Long creditId) {
        return ResponseEntity.ok(accountantCreditService.getCreditTransactions(creditId));
    }
}
