package com.cc.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.cc.modals.Accountant;
import com.cc.service.AccountantService;

@RestController
@RequestMapping("/api/accountant")
@CrossOrigin("*")
public class AccountantController {

    @Autowired
    private AccountantService service;

    // Create
    @PostMapping
    public ResponseEntity<Accountant> create(@RequestBody Accountant entity) {
        return ResponseEntity.ok(service.create(entity));
    }

    // Read All
    @GetMapping
    public ResponseEntity<List<Accountant>> getAll() {
        return ResponseEntity.ok(service.getAll());
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

            if (accountantUsername == null || accountantUsername.isEmpty()) {
                return ResponseEntity.badRequest().body("accountantUsername is required");
            }

            com.cc.modals.Student updatedStudent = service.markTelexPaymentNotReceived(studentId, accountantUsername);
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
}
