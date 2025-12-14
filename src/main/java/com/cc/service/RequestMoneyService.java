package com.cc.service;

import com.cc.modals.RequestMoney;
import com.cc.repository.RequestMoneyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class RequestMoneyService {

    @Autowired
    private RequestMoneyRepository requestMoneyRepository;

    public RequestMoney createRequest(RequestMoney requestMoney) {
        requestMoney.setStatus("PENDING");
        requestMoney.setCreatedAt(LocalDateTime.now());
        requestMoney.setUpdatedAt(LocalDateTime.now());
        return requestMoneyRepository.save(requestMoney);
    }

    public List<RequestMoney> getAllRequests() {
        return requestMoneyRepository.findAll(
                org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "id"));
    }

    public List<RequestMoney> getRequestsByUsername(String username) {
        return requestMoneyRepository.findByRequesterUsernameOrderByIdDesc(username);
    }

    @Autowired
    private com.cc.repository.PaymentTransactionRepository paymentTransactionRepository;

    public RequestMoney addPayment(Long requestId, Double amount, String accountantPaymentMode, String transactionId) {
        Optional<RequestMoney> optionalRequest = requestMoneyRepository.findById(requestId);
        if (optionalRequest.isPresent()) {
            RequestMoney request = optionalRequest.get();

            // Create Transaction
            String finalTransactionId = (transactionId != null && !transactionId.isEmpty()) ? transactionId
                    : "CASH-" + System.currentTimeMillis();
            if ("Cash".equalsIgnoreCase(accountantPaymentMode) && (transactionId == null || transactionId.isEmpty())) {
                finalTransactionId = "CASH-PAYMENT-" + System.currentTimeMillis();
            }

            com.cc.modals.PaymentTransaction transaction = new com.cc.modals.PaymentTransaction(
                    amount,
                    accountantPaymentMode,
                    finalTransactionId,
                    request);

            // Add to list? better to save directly or let cascade handle it.
            // Let's save transaction directly to be safe and simple.
            paymentTransactionRepository.save(transaction);

            // Update Totals
            Double currentPaid = request.getAmountPaid() != null ? request.getAmountPaid() : 0.0;
            Double newTotal = currentPaid + amount;
            request.setAmountPaid(newTotal);

            // Update Status
            if (newTotal >= request.getRequestedAmount()) {
                request.setStatus("PAID");
            } else {
                request.setStatus("PARTIAL");
            }

            request.setUpdatedAt(LocalDateTime.now());
            return requestMoneyRepository.save(request);
        } else {
            throw new RuntimeException("Request not found with id: " + requestId);
        }
    }

    public RequestMoney getRequestById(Long id) {
        return requestMoneyRepository.findById(id).orElse(null);
    }
}
