package com.cc.controller;

import com.cc.modals.RequestMoney;
import com.cc.service.RequestMoneyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/request-money")
public class RequestMoneyController {

    @Autowired
    private RequestMoneyService requestMoneyService;

    @PostMapping("/create")
    public ResponseEntity<RequestMoney> createRequest(@RequestBody RequestMoney requestMoney) {
        RequestMoney createdRequest = requestMoneyService.createRequest(requestMoney);
        return ResponseEntity.ok(createdRequest);
    }

    @GetMapping("/all")
    public ResponseEntity<List<RequestMoney>> getAllRequests() {
        return ResponseEntity.ok(requestMoneyService.getAllRequests());
    }

    @GetMapping("/user/{username}")
    public ResponseEntity<List<RequestMoney>> getRequestsByUsername(@PathVariable String username) {
        return ResponseEntity.ok(requestMoneyService.getRequestsByUsername(username));
    }

    @GetMapping("/{id}")
    public ResponseEntity<RequestMoney> getRequestById(@PathVariable Long id) {
        RequestMoney request = requestMoneyService.getRequestById(id);
        if (request != null) {
            return ResponseEntity.ok(request);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/update-status/{id}")
    public ResponseEntity<?> updateStatus(@PathVariable Long id, @RequestBody Map<String, String> payload) {
        String status = payload.get("status");
        String remarks = payload.get("rejectionRemarks");
        try {
            RequestMoney updatedRequest = requestMoneyService.updateStatus(id, status, remarks);
            return ResponseEntity.ok(updatedRequest);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/pay/{id}")
    public ResponseEntity<RequestMoney> addPayment(@PathVariable Long id,
            @RequestBody Map<String, Object> paymentDetails) {
        Double amountPaid;
        Object amountPaidObj = paymentDetails.get("amountPaid");
        if (amountPaidObj instanceof Integer) {
            amountPaid = ((Integer) amountPaidObj).doubleValue();
        } else {
            amountPaid = (Double) amountPaidObj;
        }

        String transactionId = (String) paymentDetails.get("transactionId");
        String accountantPaymentMode = (String) paymentDetails.get("accountantPaymentMode");
        Long subAccountId = null;
        Object subAccountObj = paymentDetails.get("subAccountId");
        if (subAccountObj != null) {
            if (subAccountObj instanceof Integer) {
                subAccountId = ((Integer) subAccountObj).longValue();
            } else if (subAccountObj instanceof Long) {
                subAccountId = (Long) subAccountObj;
            } else if (subAccountObj instanceof String) {
                subAccountId = Long.parseLong((String) subAccountObj);
            }
        }

        try {
            RequestMoney updatedRequest = requestMoneyService.addPayment(id, amountPaid, accountantPaymentMode,
                    transactionId, subAccountId);
            return ResponseEntity.ok(updatedRequest);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
