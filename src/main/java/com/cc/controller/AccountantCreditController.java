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
}
