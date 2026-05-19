package com.cc.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cc.modals.Student;
import com.cc.service.VisaService;

@RestController
@RequestMapping("/api/visa")
public class VisaController {

    @Autowired
    private VisaService visaService;

    // 1. Assign Visa Manager (called by Admission Manager)
    @PostMapping("/assign/{studentId}")
    public ResponseEntity<?> assignVisaManager(
            @PathVariable Long studentId,
            @RequestBody Map<String, String> request) {
        try {
            String admissionManagerUsername = request.get("admissionManagerUsername");
            String visaPersonUsername = request.get("visaPersonUsername");

            if (admissionManagerUsername == null || admissionManagerUsername.isEmpty()) {
                return ResponseEntity.badRequest().body("admissionManagerUsername is required");
            }
            if (visaPersonUsername == null || visaPersonUsername.isEmpty()) {
                return ResponseEntity.badRequest().body("visaPersonUsername is required");
            }

            Student student = visaService.assignVisaManager(studentId, admissionManagerUsername, visaPersonUsername);
            return ResponseEntity.ok(student);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error assigning Visa Manager: " + e.getMessage());
        }
    }

    // 2. Confirm Visa Fee
    @PostMapping("/fee-received/{studentId}")
    public ResponseEntity<?> confirmVisaFee(
            @PathVariable Long studentId,
            @RequestBody Map<String, String> request) {
        try {
            String managerUsername = request.get("managerUsername");
            if (managerUsername == null || managerUsername.isEmpty()) {
                return ResponseEntity.badRequest().body("managerUsername is required");
            }
            Student student = visaService.confirmVisaFee(studentId, managerUsername);
            return ResponseEntity.ok(student);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error confirming Visa Fee: " + e.getMessage());
        }
    }

    // 3. Passport Submitted
    @PostMapping("/passport-submitted/{studentId}")
    public ResponseEntity<?> submitPassport(
            @PathVariable Long studentId,
            @RequestBody Map<String, String> request) {
        try {
            String managerUsername = request.get("managerUsername");
            if (managerUsername == null || managerUsername.isEmpty()) {
                return ResponseEntity.badRequest().body("managerUsername is required");
            }
            Student student = visaService.submitPassportToEmbassy(studentId, managerUsername);
            return ResponseEntity.ok(student);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error submitting passport: " + e.getMessage());
        }
    }

    // 4. Visa Stamped
    @PostMapping("/stamped/{studentId}")
    public ResponseEntity<?> confirmVisaStamped(
            @PathVariable Long studentId,
            @RequestBody Map<String, String> request) {
        try {
            String managerUsername = request.get("managerUsername");
            if (managerUsername == null || managerUsername.isEmpty()) {
                return ResponseEntity.badRequest().body("managerUsername is required");
            }
            Student student = visaService.confirmVisaStamped(studentId, managerUsername);
            return ResponseEntity.ok(student);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error confirming Visa Stamped: " + e.getMessage());
        }
    }

    // 5. Apply for Visa
    @PostMapping("/apply/{studentId}")
    public ResponseEntity<?> applyForVisa(
            @PathVariable Long studentId,
            @RequestBody Map<String, String> request) {
        try {
            String managerUsername = request.get("managerUsername");
            if (managerUsername == null || managerUsername.isEmpty()) {
                return ResponseEntity.badRequest().body("managerUsername is required");
            }
            Student student = visaService.applyForVisa(studentId, managerUsername);
            return ResponseEntity.ok(student);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error applying for Visa: " + e.getMessage());
        }
    }

    // Step 5 (New): Collect Passport
    @PostMapping("/collect-passport/{studentId}")
    public ResponseEntity<?> collectPassport(
            @PathVariable Long studentId,
            @RequestBody Map<String, String> request) {
        try {
            String managerUsername = request.get("managerUsername");
            if (managerUsername == null || managerUsername.isEmpty()) {
                return ResponseEntity.badRequest().body("managerUsername is required");
            }
            Student student = visaService.collectPassport(studentId, managerUsername);
            return ResponseEntity.ok(student);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error collecting passport: " + e.getMessage());
        }
    }

    // 6. Return Docs
    @PostMapping("/return-docs/{studentId}")
    public ResponseEntity<?> returnDocsToDocPerson(
            @PathVariable Long studentId,
            @org.springframework.web.bind.annotation.RequestParam("username") String username) {
        try {
            return ResponseEntity.ok(visaService.returnDocsToDocPerson(studentId, username));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error returning docs: " + e.getMessage());
        }
    }

    // ======================= VISA PAYMENT WORKFLOW =======================

    // Step 1: VisaMan requests payment from Accountant
    @PostMapping("/request-payment/{studentId}")
    public ResponseEntity<?> requestPaymentFromAccountant(
            @PathVariable Long studentId,
            @RequestBody Map<String, Object> request) {
        try {
            String visaManUsername = (String) request.get("visaManUsername");
            Double amount = Double.valueOf(request.get("amount").toString());
            String currency = (String) request.get("currency");

            Double visaManServiceCharge = 0.0;
            if (request.get("visaManServiceCharge") != null) {
                visaManServiceCharge = Double.valueOf(request.get("visaManServiceCharge").toString());
            }

            if (visaManUsername == null || visaManUsername.isEmpty()) {
                return ResponseEntity.badRequest().body("visaManUsername is required");
            }
            if (amount == null) {
                return ResponseEntity.badRequest().body("amount is required");
            }
            if (currency == null || currency.isEmpty()) {
                return ResponseEntity.badRequest().body("currency is required");
            }

            Student student = visaService.requestPaymentFromAccountant(studentId, visaManUsername, amount, currency,
                    visaManServiceCharge);
            return ResponseEntity.ok(student);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error requesting payment: " + e.getMessage());
        }
    }

    // Step 2: Accountant (in Uzbekistan) sends bank details to Agent (in India)
    @PostMapping("/send-bank-details/{studentId}")
    public ResponseEntity<?> sendBankDetailsToAgent(
            @PathVariable Long studentId,
            @RequestBody Map<String, Object> request) {
        try {
            String accountantUsername = (String) request.get("accountantUsername");
            Double amount = Double.valueOf(request.get("amount").toString());
            String currency = (String) request.get("currency");
            Long subAccountId = Long.valueOf(request.get("subAccountId").toString());

            if (accountantUsername == null || accountantUsername.isEmpty()) {
                return ResponseEntity.badRequest().body("accountantUsername is required");
            }
            if (amount == null) {
                return ResponseEntity.badRequest().body("amount is required");
            }
            if (currency == null || currency.isEmpty()) {
                return ResponseEntity.badRequest().body("currency is required");
            }
            if (subAccountId == null) {
                return ResponseEntity.badRequest().body("subAccountId is required");
            }

            Student student = visaService.sendBankDetailsToAgent(studentId, accountantUsername, amount, currency,
                    subAccountId);
            return ResponseEntity.ok(student);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error sending bank details: " + e.getMessage());
        }
    }

    // Step 3: Agent sends payment confirmation (transactionId and
    // confirmationPagePath are optional)
    @PostMapping("/submit-payment-confirmation/{studentId}")
    public ResponseEntity<?> submitPaymentConfirmation(
            @PathVariable Long studentId,
            @RequestBody Map<String, Object> request) {
        try {
            String agentUsername = (String) request.get("agentUsername");
            Double amount = Double.valueOf(request.get("amount").toString());
            String currency = (String) request.get("currency");
            String transactionId = (String) request.get("transactionId"); // Optional
            String confirmationPagePath = (String) request.get("confirmationPagePath"); // Optional

            if (agentUsername == null || agentUsername.isEmpty()) {
                return ResponseEntity.badRequest().body("agentUsername is required");
            }
            if (amount == null) {
                return ResponseEntity.badRequest().body("amount is required");
            }
            if (currency == null || currency.isEmpty()) {
                return ResponseEntity.badRequest().body("currency is required");
            }

            Student student = visaService.submitPaymentConfirmation(studentId, agentUsername, amount, currency,
                    transactionId, confirmationPagePath);
            return ResponseEntity.ok(student);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error submitting payment confirmation: " + e.getMessage());
        }
    }

    // Step 4: Accountant confirms payment received
    @PostMapping("/confirm-payment/{studentId}")
    public ResponseEntity<?> confirmVisaPaymentReceived(
            @PathVariable Long studentId,
            @RequestBody Map<String, Object> request) {
        try {
            String accountantUsername = (String) request.get("accountantUsername");
            Double receivedAmount = Double.valueOf(request.get("receivedAmount").toString());
            String receivedDateStr = (String) request.get("receivedDate");

            if (accountantUsername == null || accountantUsername.isEmpty()) {
                return ResponseEntity.badRequest().body("accountantUsername is required");
            }
            if (receivedAmount == null) {
                return ResponseEntity.badRequest().body("receivedAmount is required");
            }
            if (receivedDateStr == null || receivedDateStr.isEmpty()) {
                return ResponseEntity.badRequest().body("receivedDate is required");
            }

            java.time.LocalDate receivedDate = java.time.LocalDate.parse(receivedDateStr);
            Student student = visaService.confirmVisaPaymentReceived(studentId, accountantUsername, receivedAmount,
                    receivedDate);
            return ResponseEntity.ok(student);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error confirming visa payment: " + e.getMessage());
        }
    }

    // Step 5 (New): Accountant pays Visa Man
    @PostMapping("/pay-visa-man/{studentId}")
    public ResponseEntity<?> payVisaMan(
            @PathVariable Long studentId,
            @RequestBody Map<String, Object> request) {
        try {
            String accountantUsername = (String) request.get("accountantUsername");
            Double amountToPay = Double.valueOf(request.get("amountToPay").toString());
            String txId = (String) request.get("txId"); // Optional
            String mode = (String) request.get("mode");
            String paymentDateStr = (String) request.get("paymentDate");
            Long subAccountId = Long.valueOf(request.get("subAccountId").toString());

            if (accountantUsername == null || accountantUsername.isEmpty()) {
                return ResponseEntity.badRequest().body("accountantUsername is required");
            }
            if (amountToPay == null) {
                return ResponseEntity.badRequest().body("amountToPay is required");
            }
            if (mode == null || mode.isEmpty()) {
                return ResponseEntity.badRequest().body("mode is required");
            }
            if (paymentDateStr == null || paymentDateStr.isEmpty()) {
                return ResponseEntity.badRequest().body("paymentDate is required");
            }
            if (subAccountId == null) {
                return ResponseEntity.badRequest().body("subAccountId is required");
            }

            java.time.LocalDate paymentDate = java.time.LocalDate.parse(paymentDateStr);
            Student student = visaService.payVisaMan(studentId, accountantUsername, amountToPay, txId, mode,
                    paymentDate, subAccountId);
            return ResponseEntity.ok(student);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error paying Visa Man: " + e.getMessage());
        }
    }

    // ======================= REPORTING ENDPOINTS =======================

    @org.springframework.web.bind.annotation.GetMapping("/stats")
    public ResponseEntity<?> getVisaStats(
            @org.springframework.web.bind.annotation.RequestParam(value = "period", defaultValue = "DAILY") String period,
            @org.springframework.web.bind.annotation.RequestParam(value = "visaManUsername", required = false) String username) {
        try {
            return ResponseEntity.ok(visaService.getVisaStats(period, username));
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error fetching Visa Stats: " + e.getMessage());
        }
    }

    @org.springframework.web.bind.annotation.GetMapping("/all-stats")
    public ResponseEntity<?> getAllVisaStats(
            @org.springframework.web.bind.annotation.RequestParam(value = "period", defaultValue = "DAILY") String period) {
        try {
            return ResponseEntity.ok(visaService.getAllVisaManagersStats(period));
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error fetching All Visa Stats: " + e.getMessage());
        }
    }

    @org.springframework.web.bind.annotation.GetMapping("/students")
    public ResponseEntity<?> getVisaStudents(
            @org.springframework.web.bind.annotation.RequestParam(value = "period", defaultValue = "DAILY") String period,
            @org.springframework.web.bind.annotation.RequestParam(value = "visaManUsername") String username) {
        try {
            return ResponseEntity.ok(visaService.getVisaStudents(period, username));
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error fetching Visa Students: " + e.getMessage());
        }
    }

    @PostMapping("/report/email")
    public ResponseEntity<?> sendReportEmail(@RequestBody Map<String, String> request) {
        try {
            String period = request.get("period"); // DAILY, MONTHLY, YEARLY
            String username = request.get("username"); // Optional (for specific manager)
            String targetEmail = request.get("targetEmail");

            if (period == null || period.isEmpty()) {
                period = "DAILY"; // Default
            }
            if (targetEmail == null || targetEmail.isEmpty()) {
                return ResponseEntity.badRequest().body("targetEmail is required");
            }

            visaService.sendReportEmail(period, username, targetEmail);
            return ResponseEntity.ok("Report email sent successfully to " + targetEmail);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error sending report email: " + e.getMessage());
        }
    }
}
