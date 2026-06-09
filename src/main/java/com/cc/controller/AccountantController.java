package com.cc.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.HttpStatus;
import java.util.Map;
import java.util.List;
import com.cc.service.AccountantService;
import com.cc.repository.AccountantRepository;



import com.cc.DTO.FundReleaseRequestDTO;
import com.cc.modals.FundRelease;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.cc.modals.Accountant;
import com.cc.service.AccountantService;

@RestController
@RequestMapping("/api/accountant")
public class AccountantController {
    @Autowired
    private AccountantService accountantService;
    @Autowired
    private AccountantRepository accountantRepository;

    @Autowired
    private AccountantService service;

    @Autowired
    private com.cc.repository.PassbookVisaManAccountantRepository passbookVisaManAccountantRepository;

    @Autowired
    private com.cc.repository.PassbookAccountantAgentRepository passbookAccountantAgentRepository;

    @Autowired
    private com.cc.repository.BeneficiaryAccountRepository beneficiaryAccountRepository;

    @Autowired
    private com.cc.service.UserService userService;

    @GetMapping("/passbook/visaman")
    public ResponseEntity<List<com.cc.modals.PassbookVisaManAccountant>> getPassbookVisaMan() {
        List<com.cc.modals.PassbookVisaManAccountant> list = passbookVisaManAccountantRepository.findAll();
        for (com.cc.modals.PassbookVisaManAccountant pb : list) {
            pb.setRequesterFullName(userService.resolveFullName(pb.getRequesterName()));
        }
        return ResponseEntity.ok(list);
    }

    @GetMapping("/passbook/agent")
    public ResponseEntity<List<com.cc.modals.PassbookForAccountantAgent>> getPassbookAgent() {
        List<com.cc.modals.PassbookForAccountantAgent> list = passbookAccountantAgentRepository.findAll();
        for (com.cc.modals.PassbookForAccountantAgent pb : list) {
            pb.setPaidByAgentFullName(userService.resolveFullName(pb.getPaidByAgent()));
        }
        return ResponseEntity.ok(list);
    }

    @Autowired
    private com.cc.service.SubAccountService subAccountService;

    @Autowired
    private com.cc.repository.SubAccountTransactionRepository subAccountTransactionRepository;

    // --- SubAccount Endpoints ---
    // --- Beneficiary Account Endpoints ---
    @GetMapping("/beneficiary-accounts")
    public ResponseEntity<List<com.cc.modals.BeneficiaryAccount>> getAllBeneficiaryAccounts() {
        return ResponseEntity.ok(beneficiaryAccountRepository.findAll());
    }

    @PostMapping("/beneficiary-accounts")
    public ResponseEntity<com.cc.modals.BeneficiaryAccount> createBeneficiaryAccount(@RequestBody com.cc.modals.BeneficiaryAccount account) {
        return ResponseEntity.ok(beneficiaryAccountRepository.save(account));
    }

    @PutMapping("/beneficiary-accounts/{id}")
    public ResponseEntity<com.cc.modals.BeneficiaryAccount> updateBeneficiaryAccount(@PathVariable Long id, @RequestBody com.cc.modals.BeneficiaryAccount account) {
        java.util.Optional<com.cc.modals.BeneficiaryAccount> existingOpt = beneficiaryAccountRepository.findById(id);
        if (!existingOpt.isPresent()) return ResponseEntity.notFound().build();
        
        com.cc.modals.BeneficiaryAccount existing = existingOpt.get();
        existing.setBeneficiaryType(account.getBeneficiaryType());
        existing.setBeneficiaryName(account.getBeneficiaryName());
        existing.setBankName(account.getBankName());
        existing.setAccountNumber(account.getAccountNumber());
        existing.setIfscCode(account.getIfscCode());
        existing.setBranchName(account.getBranchName());
        existing.setUpiId(account.getUpiId());
        existing.setMobileNumber(account.getMobileNumber());
        existing.setEmailAddress(account.getEmailAddress());
        existing.setStatus(account.getStatus());
        
        return ResponseEntity.ok(beneficiaryAccountRepository.save(existing));
    }
    @GetMapping("/subaccounts")
    public ResponseEntity<List<com.cc.modals.SubAccount>> getAllSubAccounts() {
        return ResponseEntity.ok(subAccountService.getAllSubAccounts());
    }

    @PostMapping("/subaccounts")
    public ResponseEntity<com.cc.modals.SubAccount> createSubAccount(@RequestBody com.cc.modals.SubAccount subAccount) {
        return ResponseEntity.ok(subAccountService.saveSubAccount(subAccount));
    }

    @PutMapping("/subaccounts/{id}")
    public ResponseEntity<com.cc.modals.SubAccount> updateSubAccount(@PathVariable Long id,
            @RequestBody com.cc.modals.SubAccount subAccount) {
        com.cc.modals.SubAccount existing = subAccountService.getSubAccountById(id);
        if (existing == null)
            return ResponseEntity.notFound().build();
        existing.setBankName(subAccount.getBankName());
        existing.setAccountNumber(subAccount.getAccountNumber());
        existing.setSwiftBic(subAccount.getSwiftBic());
        existing.setBeneficiaryName(subAccount.getBeneficiaryName());
        existing.setBranchAddress(subAccount.getBranchAddress());
        existing.setAccountType(subAccount.getAccountType());
        existing.setCashAccountName(subAccount.getCashAccountName());
        return ResponseEntity.ok(subAccountService.saveSubAccount(existing));
    }

    @PostMapping("/subaccounts/internal-transfer")
    public ResponseEntity<?> internalTransfer(@RequestBody java.util.Map<String, Object> payload) {
        try {
            Long sourceId = Long.valueOf(payload.get("sourceId").toString());
            Long destId = Long.valueOf(payload.get("destId").toString());
            Double amount = Double.valueOf(payload.get("amount").toString());
            String description = (String) payload.get("description");

            subAccountService.internalTransfer(sourceId, destId, amount, description);
            
            return ResponseEntity.ok(java.util.Collections.singletonMap("message", "Transfer successful"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error processing internal transfer: " + e.getMessage());
        }
    }

    // Create
    @PostMapping
    public ResponseEntity<Accountant> create(@RequestBody Accountant entity) {
        return ResponseEntity.ok(service.create(entity));
    }

    // Read All
    @GetMapping({ "", "/all" })
    public ResponseEntity<List<Accountant>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

    // Check if new Accountant can be created
    @GetMapping("/check-availability")
    public ResponseEntity<java.util.Map<String, Object>> checkAvailability() {
        return ResponseEntity.ok(service.checkAvailability());
    }

    // Read by ID
    @GetMapping("/{id}")
    public ResponseEntity<Accountant> getById(@PathVariable Long id) {
        Optional<Accountant> entity = service.getById(id);
        return entity.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Read by Username
    @GetMapping("/username/{username}")
    public ResponseEntity<Accountant> getByUsername(@PathVariable String username) {
        Optional<Accountant> entity = service.getByUsername(username);
        return entity.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Update
    @PutMapping("/{id}")
    public ResponseEntity<Accountant> update(@PathVariable Long id, @RequestBody Accountant entity) {
        Accountant updated = service.update(id, entity);
        return updated != null ? ResponseEntity.ok(updated) : ResponseEntity.notFound().build();
    }

    // Make Inactive (Soft Delete)
    @PutMapping("/{id}/status/{status}")
    public ResponseEntity<Accountant> updateStatus(@PathVariable Long id, @PathVariable String status) {
        Optional<Accountant> optionalEntity = service.getById(id);
        if (optionalEntity.isPresent()) {
            Accountant entity = optionalEntity.get();
            entity.setStatus(status);
            return ResponseEntity.ok(service.update(id, entity));
        }
        return ResponseEntity.notFound().build();
    }

    // Delete (Hard Delete)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    // Update Student Payment Status
    @PostMapping("/update-payment/{studentId}")
    public ResponseEntity<?> updatePaymentStatus(
            @PathVariable Long studentId,
            @RequestBody java.util.Map<String, String> request) {
        try {
            String paymentStatus = request.get("paymentStatus");
            String accountantUsername = request.get("accountantUsername");

            if (paymentStatus == null || paymentStatus.isEmpty()) {
                return ResponseEntity.badRequest().body("paymentStatus is required");
            }
            if (accountantUsername == null || accountantUsername.isEmpty()) {
                return ResponseEntity.badRequest().body("accountantUsername is required");
            }

            com.cc.modals.Student updatedStudent = service.updateStudentPaymentStatus(studentId, paymentStatus,
                    accountantUsername);
            return ResponseEntity.ok(updatedStudent);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error updating payment status: " + e.getMessage());
        }
    }

    @PostMapping("/approve-telex-payment/{studentId}")
    public ResponseEntity<?> approveTelexPayment(
            @PathVariable Long studentId,
            @RequestBody java.util.Map<String, Object> request) {
        try {
            Double receivedAmount = Double.valueOf(request.get("receivedAmount").toString());
            String accountantUsername = (String) request.get("accountantUsername");
            String receivedDateStr = (String) request.get("receivedDate");

            if (receivedAmount == null) {
                return ResponseEntity.badRequest().body("receivedAmount is required");
            }
            if (accountantUsername == null || accountantUsername.isEmpty()) {
                return ResponseEntity.badRequest().body("accountantUsername is required");
            }
            if (receivedDateStr == null || receivedDateStr.isEmpty()) {
                return ResponseEntity.badRequest().body("receivedDate is required");
            }

            java.time.LocalDate receivedDate = java.time.LocalDate.parse(receivedDateStr);

            com.cc.modals.Student updatedStudent = service.approveTelexPayment(studentId, receivedAmount, receivedDate,
                    accountantUsername);
            return ResponseEntity.ok(updatedStudent);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error approving telex payment: " + e.getMessage());
        }
    }

    @PostMapping("/payment-not-received/{studentId}")
    public ResponseEntity<?> markPaymentNotReceived(
            @PathVariable Long studentId,
            @RequestBody java.util.Map<String, String> request) {
        try {
            String accountantUsername = request.get("accountantUsername");
            String rejectionRemarks = request.get("rejectionRemarks");

            if (accountantUsername == null || accountantUsername.isEmpty()) {
                return ResponseEntity.badRequest().body("accountantUsername is required");
            }

            com.cc.modals.Student updatedStudent = service.markTelexPaymentNotReceived(studentId, accountantUsername,
                    rejectionRemarks);
            return ResponseEntity.ok(updatedStudent);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error marking payment not received: " + e.getMessage());
        }
    }

    @PostMapping("/confirm-visa-fee/{studentId}")
    public ResponseEntity<?> confirmVisaFee(
            @PathVariable Long studentId,
            @RequestBody java.util.Map<String, String> request) {
        try {
            String accountantUsername = request.get("accountantUsername");

            if (accountantUsername == null || accountantUsername.isEmpty()) {
                return ResponseEntity.badRequest().body("accountantUsername is required");
            }

            com.cc.modals.Student updatedStudent = service.confirmVisaFee(studentId, accountantUsername);
            return ResponseEntity.ok(updatedStudent);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error confirming visa fee: " + e.getMessage());
        }
    }

    // ======================= DASHBOARD ENDPOINTS =======================

    @GetMapping("/dashboard/stats/{username}")
    public ResponseEntity<?> getDashboardStats(@PathVariable String username) {
        try {
            return ResponseEntity.ok(service.getDashboardStats(username));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error fetching dashboard stats: " + e.getMessage());
        }
    }

    @GetMapping("/dashboard/collections/{username}")
    public ResponseEntity<?> getCollectionSummary(@PathVariable String username) {
        try {
            return ResponseEntity.ok(service.getCollectionSummary(username));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error fetching collection summary: " + e.getMessage());
        }
    }

    @GetMapping("/dashboard/activities/{username}")
    public ResponseEntity<?> getRecentActivities(@PathVariable String username,
            @RequestParam(defaultValue = "10") int limit) {
        try {
            return ResponseEntity.ok(service.getRecentActivities(username, limit));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error fetching activities: " + e.getMessage());
        }
    }

    @GetMapping("/dashboard/students/{username}")
    public ResponseEntity<?> getAssignedStudents(@PathVariable String username) {
        try {
            return ResponseEntity.ok(service.getAssignedStudents(username));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error fetching students: " + e.getMessage());
        }
    }

    @GetMapping("/{id}/with-students")
    public ResponseEntity<?> getAccountantWithStudents(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(service.getAccountantWithStudents(id));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error fetching accountant details: " + e.getMessage());
        }
    }

    @GetMapping("/subaccounts/{id}/transactions")
    public ResponseEntity<?> getSubAccountTransactions(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(subAccountTransactionRepository.findBySubAccountIdOrderByTransactionDateDesc(id));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error fetching transactions: " + e.getMessage());
        }
    }

    @PostMapping("/visa-fund-release")
    public ResponseEntity<?> releaseFund(
            @RequestParam("username") String username,
            @ModelAttribute FundReleaseRequestDTO requestDTO,
            @RequestParam(value = "receiptFile", required = false) MultipartFile receiptFile) {
        try {
            FundRelease release = accountantService.releaseFund(username, requestDTO, receiptFile);
            return ResponseEntity.ok(release);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to release fund: " + e.getMessage());
        }
    }

    @GetMapping("/outstanding-recovery")
    public ResponseEntity<?> getOutstandingRecovery(@RequestParam String username) {
        try {
            // Verify accountant exists
            Accountant accountant = accountantRepository.findByUsername(username).orElse(null);
            if (accountant == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
            }
            List<Map<String, Object>> recoveryList = accountantService.getOutstandingRecovery();
            return ResponseEntity.ok(recoveryList);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to fetch outstanding recovery: " + e.getMessage());
        }
    }
}
