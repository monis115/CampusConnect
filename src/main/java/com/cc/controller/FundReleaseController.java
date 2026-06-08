package com.cc.controller;

import com.cc.modals.FundRelease;
import com.cc.modals.Student;
import com.cc.modals.CollectionRequest;
import com.cc.repository.FundReleaseRepository;
import com.cc.repository.StudentRepository;
import com.cc.repository.CollectionRequestRepository;
import com.cc.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/fund-release")
public class FundReleaseController {

    @Autowired
    private FundReleaseRepository fundReleaseRepository;

    @Autowired
    private CollectionRequestRepository collectionRequestRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private com.cc.repository.VisaPaymentTransactionRepository visaPaymentTransactionRepository;

    @Autowired
    private com.cc.repository.StudentStatusHistoryRepository studentStatusHistoryRepository;

    @PostMapping("/release/{studentId}")
    public ResponseEntity<?> releaseFunds(
            @PathVariable Long studentId,
            @RequestParam("accountantUsername") String accountantUsername,
            @RequestParam("sourceAccountId") Long sourceAccountId,
            @RequestParam("beneficiaryAccountId") Long beneficiaryAccountId,
            @RequestParam("visaFee") Double visaFee,
            @RequestParam("serviceCharge") Double serviceCharge,
            @RequestParam("transferDate") String transferDateStr,
            @RequestParam(value = "transactionReference", required = false) String transactionReference,
            @RequestParam(value = "remarks", required = false) String remarks,
            @RequestParam(value = "receipt", required = false) MultipartFile receipt) {
        
        try {
            Student student = studentRepository.findById(studentId).orElseThrow(() -> new RuntimeException("Student not found"));
            
            // Check if collection request exists (required by business rules)
            List<CollectionRequest> requests = collectionRequestRepository.findByStudentId(studentId);
            if (requests.isEmpty()) {
                return ResponseEntity.badRequest().body("A Collection Request must exist before funds can be released.");
            }
            
            CollectionRequest req = requests.get(requests.size() - 1); // Get latest

            FundRelease release = new FundRelease();
            release.setStudent(student);
            release.setCollectionRequestId(req.getId());
            release.setSourceAccountId(sourceAccountId);
            release.setBeneficiaryAccountId(beneficiaryAccountId);
            release.setVisaFee(visaFee);
            release.setServiceCharge(serviceCharge);
            release.setTotalReleasedAmount(visaFee + serviceCharge);
            release.setTransactionReference(transactionReference);
            release.setTransferDate(LocalDate.parse(transferDateStr));
            release.setRemarks(remarks);
            release.setReleasedBy(accountantUsername);
            release.setReleasedDate(LocalDateTime.now());
            release.setReleaseStatus("RELEASED_PENDING_CONFIRMATION");
            
            if (receipt != null && !receipt.isEmpty()) {
                release.setReceiptUrl(receipt.getOriginalFilename());
            }

            fundReleaseRepository.save(release);

            // Create Debit transaction
            transactionService.logFundRelease(sourceAccountId, release.getTotalReleasedAmount(), transactionReference, remarks);

            // Update Student status for settlement tracking
            student.setAccToVisaManStatus("RELEASED_PENDING_CONFIRMATION");
            student.setAccToVisaManPaidAmount(visaFee + serviceCharge);
            studentRepository.save(student);

            // Log transaction in Visa Payment Timeline
            com.cc.modals.VisaPaymentTransaction tx = new com.cc.modals.VisaPaymentTransaction();
            tx.setStudentId(student.getId());
            tx.setApplicationId(student.getApplicationId());
            tx.setTransactionType("FUNDS_RELEASED");
            tx.setAmount(visaFee + serviceCharge);
            tx.setCurrency("INR");
            tx.setActorUsername(accountantUsername);
            tx.setActorRole("ACCOUNTANT");
            tx.setRemarks("Funds released to Visa Manager. Visa Fee: " + visaFee + ", Service Charge: " + serviceCharge
                    + (transactionReference != null ? " (Ref: " + transactionReference + ")" : ""));
            visaPaymentTransactionRepository.save(tx);

            // Add Student Status History
            com.cc.modals.StudentStatusHistory history = new com.cc.modals.StudentStatusHistory();
            history.setStudent(student);
            history.setStudentApplicationStatus(student.getApplicationStatus());
            history.setUpdatedBy(accountantUsername);
            history.setUpdatedByName(accountantUsername + " (Accountant)");
            history.setUpdatedAt(LocalDateTime.now());
            history.setRemarks("Funds released to Visa Manager: " + (visaFee + serviceCharge) + " INR"
                    + (transactionReference != null ? " (Ref: " + transactionReference + ")" : ""));
            studentStatusHistoryRepository.save(history);

            return ResponseEntity.ok(release);
            
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Failed to release funds: " + e.getMessage());
        }
    }

    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<FundRelease>> getReleasesByStudent(@PathVariable Long studentId) {
        return ResponseEntity.ok(fundReleaseRepository.findByStudent_Id(studentId));
    }

    @GetMapping("/visa-man/{username}")
    public ResponseEntity<List<FundRelease>> getReleasesByVisaMan(@PathVariable String username) {
        return ResponseEntity.ok(fundReleaseRepository.findByStudent_VisaMan_UsernameOrderByCreatedAtDesc(username));
    }

    @GetMapping("/all")
    public ResponseEntity<List<FundRelease>> getAllReleases() {
        return ResponseEntity.ok(fundReleaseRepository.findAll());
    }

    @PostMapping("/{releaseId}/confirm")
    public ResponseEntity<?> confirmRelease(@PathVariable Long releaseId) {
        try {
            Optional<FundRelease> opt = fundReleaseRepository.findById(releaseId);
            if (!opt.isPresent()) return ResponseEntity.notFound().build();
            
            FundRelease release = opt.get();
            release.setReleaseStatus("CONFIRMED");
            fundReleaseRepository.save(release);
            
            Student student = release.getStudent();
            if (student != null) {
                student.setAccToVisaManStatus("SETTLED");
                studentRepository.save(student);
            }
            
            return ResponseEntity.ok(release);
        } catch(Exception e) {
            return ResponseEntity.status(500).body(e.getMessage());
        }
    }

    @PostMapping("/{releaseId}/reject")
    public ResponseEntity<?> rejectRelease(@PathVariable Long releaseId, @RequestBody Map<String, String> payload) {
        try {
            Optional<FundRelease> opt = fundReleaseRepository.findById(releaseId);
            if (!opt.isPresent()) return ResponseEntity.notFound().build();
            
            FundRelease release = opt.get();
            release.setReleaseStatus("REJECTED");
            release.setRejectionReason(payload.get("reason"));
            fundReleaseRepository.save(release);
            
            return ResponseEntity.ok(release);
        } catch(Exception e) {
            return ResponseEntity.status(500).body(e.getMessage());
        }
    }
}
