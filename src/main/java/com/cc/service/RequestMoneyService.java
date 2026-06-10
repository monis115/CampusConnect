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

    @Autowired
    private com.cc.repository.VisaManRepository visaManRepository;
    @Autowired
    private com.cc.repository.DocPersonRepository docPersonRepository;
    @Autowired
    private com.cc.repository.AccountantRepository accountantRepository;
    @Autowired
    private com.cc.repository.AgentRepository agentRepository;
    @Autowired
    private SubAccountService subAccountService;

    public RequestMoney createRequest(RequestMoney requestMoney) {
        // Auto-populate requesterFullName if missing
        if (requestMoney.getRequesterFullName() == null || requestMoney.getRequesterFullName().isEmpty()) {
            String role = requestMoney.getRequesterRole();
            String username = requestMoney.getRequesterUsername();
            String fullName = null;

            if (role != null && username != null) {
                if ("VisaManager".equalsIgnoreCase(role) || "visa_manager".equalsIgnoreCase(role)) {
                    fullName = visaManRepository.findByUsername(username)
                            .map(com.cc.modals.VisaMan::getFullName).orElse(null);
                } else if ("DocManager".equalsIgnoreCase(role) || "DocPerson".equalsIgnoreCase(role)
                        || "doc_manager".equalsIgnoreCase(role)) {
                    fullName = docPersonRepository.findByUsername(username)
                            .map(com.cc.modals.DocPerson::getFullName).orElse(null);
                } else if ("Accountant".equalsIgnoreCase(role)) {
                    fullName = accountantRepository.findByUsername(username)
                            .map(com.cc.modals.Accountant::getFullName).orElse(null);
                } else if ("Agent".equalsIgnoreCase(role)) {
                    // Agent uses userId as username
                    fullName = agentRepository.findByUserId(username)
                            .map(com.cc.modals.Agent::getFullName).orElse(null);
                }
            }
            if (fullName != null) {
                requestMoney.setRequesterFullName(fullName);
            } else {
                // If still null, set a fallback or allow it to fail if DB constraint exists
                // requestMoney.setRequesterFullName("Unknown");
            }
        }

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

    public RequestMoney addPayment(Long requestId, Double amount, String accountantPaymentMode, String transactionId, Long subAccountId) {
        Optional<RequestMoney> optionalRequest = requestMoneyRepository.findById(requestId);
        if (optionalRequest.isPresent()) {
            RequestMoney request = optionalRequest.get();
            
            if (subAccountId != null) {
                com.cc.modals.SubAccount subAccount = subAccountService.getSubAccountById(subAccountId);
                if (subAccount != null) {
                    if (subAccount.getBalance() < amount) {
                        throw new RuntimeException("Insufficient balance in SubAccount.");
                    }
                    subAccountService.updateBalance(subAccount.getId(), -amount, "Fund Request Release: " + request.getPurpose(), transactionId);
                }
            }

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

    public RequestMoney updateStatus(Long id, String status, String remarks) {
        Optional<RequestMoney> optionalRequest = requestMoneyRepository.findById(id);
        if (optionalRequest.isPresent()) {
            RequestMoney request = optionalRequest.get();
            if (status != null) {
                request.setStatus(status);
            }
            if ("REJECTED".equalsIgnoreCase(status) && remarks != null) {
                request.setRejectionRemarks(remarks);
            }
            request.setUpdatedAt(LocalDateTime.now());
            return requestMoneyRepository.save(request);
        } else {
            throw new RuntimeException("Request not found with id: " + id);
        }
    }
}
