package com.cc.controller;

import com.cc.modals.CollectionRequest;
import com.cc.repository.CollectionRequestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/collections")
public class CollectionRequestController {

    @Autowired
    private CollectionRequestRepository collectionRequestRepository;

    @PostMapping("/generate")
    public ResponseEntity<?> generateCollectionRequest(@RequestBody CollectionRequest request) {
        try {
            // Basic validation
            if (request.getStudentId() == null) {
                return ResponseEntity.badRequest().body("Student ID is required");
            }
            if (request.getRequestedAmount() == null || request.getRequestedAmount() <= 0) {
                return ResponseEntity.badRequest().body("Valid requested amount is required");
            }
            
            // Set initial state
            request.setCollectedAmount(0.0);
            request.setPendingAmount(request.getRequestedAmount());
            request.setStatus("PENDING");
            
            CollectionRequest saved = collectionRequestRepository.save(request);
            
            // Generate request ID
            saved.setRequestId("COL-" + String.format("%06d", saved.getId()));
            saved = collectionRequestRepository.save(saved);
            
            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Failed to generate collection request: " + e.getMessage());
        }
    }

    @GetMapping("/all")
    public ResponseEntity<List<CollectionRequest>> getAllCollections() {
        return ResponseEntity.ok(collectionRequestRepository.findAll());
    }

    @GetMapping("/pending")
    public ResponseEntity<List<CollectionRequest>> getPendingCollections() {
        return ResponseEntity.ok(collectionRequestRepository.findByStatusIn(List.of("PENDING", "PARTIAL")));
    }

    @PostMapping("/{id}/add-payment")
    public ResponseEntity<?> addPayment(@PathVariable Long id, @RequestBody java.util.Map<String, Double> payload) {
        try {
            Double amount = payload.get("amount");
            if (amount == null || amount <= 0) {
                return ResponseEntity.badRequest().body("Valid amount is required");
            }
            
            Optional<CollectionRequest> optReq = collectionRequestRepository.findById(id);
            if (!optReq.isPresent()) {
                return ResponseEntity.notFound().build();
            }
            
            CollectionRequest req = optReq.get();
            req.setCollectedAmount((req.getCollectedAmount() == null ? 0 : req.getCollectedAmount()) + amount);
            collectionRequestRepository.save(req); // PreUpdate handles pendingAmount and status
            
            return ResponseEntity.ok(req);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Failed to add payment: " + e.getMessage());
        }
    }
}
