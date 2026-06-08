package com.cc.service;

import com.cc.modals.CollectionRequest;
import com.cc.modals.Student;
import com.cc.repository.CollectionRequestRepository;
import com.cc.repository.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class CollectionRequestService {

    @Autowired
    private CollectionRequestRepository collectionRequestRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private TransactionService transactionService;

    @Transactional
    public CollectionRequest processCashSettlement(Long studentId, String accountantUsername, Double amount,
            Long subAccountId, String date, String remarks, String fileUrl) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));

        List<CollectionRequest> activeRequests = collectionRequestRepository.findByStudentIdAndStatusIn(studentId,
                List.of("PENDING", "PARTIAL"));

        CollectionRequest req;
        if (!activeRequests.isEmpty()) {
            req = activeRequests.get(0);
        } else {
            // Generate one if missing (e.g. cash settlement before agent notification)
            req = new CollectionRequest();
            req.setStudentId(student.getId());
            req.setStudentName(student.getFullName());
            req.setApplicationId(student.getApplicationId());
            req.setAgentUsername(student.getAgent() != null ? student.getAgent().getUserId() : null);
            req.setAgentName(student.getAgent() != null ? student.getAgent().getFullName() : null);
            req.setUniversityName(student.getCollege() != null ? student.getCollege().getCollegeName() : null);
            req.setRequestedAmount(
                    (student.getVisaPaymentRequestedAmount() != null ? student.getVisaPaymentRequestedAmount() : 0.0)
                            + (student.getVisaManServiceCharge() != null ? student.getVisaManServiceCharge() : 0.0));
            req.setCollectedAmount(0.0);
            req.setPendingAmount(req.getRequestedAmount());
            req.setStatus("PENDING");
            req.setCreatedBy(accountantUsername);
            req = collectionRequestRepository.save(req);
            req.setRequestId("COL-" + String.format("%06d", req.getId()));
            req = collectionRequestRepository.save(req);
        }

        // Update amounts
        req.setCollectedAmount((req.getCollectedAmount() == null ? 0 : req.getCollectedAmount()) + amount);
        req = collectionRequestRepository.save(req); // PreUpdate recalculates pending

        // Create credit transaction
        transactionService.logCashSettlement(subAccountId, amount, req.getRequestId(), remarks);

        return req;
    }
}
