package com.cc.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.cc.modals.FundRelease;
import com.cc.repository.FundReleaseRepository;
import com.cc.DTO.FundReleaseDTO;
import java.util.Map;
import java.util.HashMap;
import com.cc.modals.FundReleaseConfirmation;
import com.cc.repository.FundReleaseConfirmationRepository;
import java.time.LocalDateTime;

import com.cc.modals.VisaMan;

import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.List;

import com.cc.repository.VisaManRepository;
import java.util.List;
import java.util.Optional;

@Service
@SuppressWarnings("null")
public class VisaManService {

    @Autowired
    private VisaManRepository repository;

    @Autowired
    private UserService userService;

    public VisaMan create(VisaMan entity) {
        VisaMan savedEntity = repository.save(entity);

        com.cc.modals.User user = new com.cc.modals.User();
        user.setUsername(savedEntity.getUsername());
        user.setPassword(savedEntity.getPassword());
        user.setRole("visa_manager");
        userService.createUser(user);

        return savedEntity;
    }

    public List<VisaMan> getAll() {
        return repository.findAll();
    }

    public Optional<VisaMan> getById(Long id) {
        return repository.findById(id);
    }

    public java.util.Optional<VisaMan> getOne() {
        return repository.findAll().stream().findFirst();
    }

    public Optional<VisaMan> getByUsername(String username) {
        return repository.findByUsername(username);
    }

    public VisaMan update(Long id, VisaMan entity) {
        if (repository.existsById(id)) {
            entity.setId(id);
            return repository.save(entity);
        }
        return null;
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }

    @Autowired
    private com.cc.repository.StudentRepository studentRepository;

    @org.springframework.beans.factory.annotation.Autowired
    private com.cc.repository.VisaManRepository visaManRepository;
    
    @org.springframework.beans.factory.annotation.Autowired
    private com.cc.repository.FundReleaseRepository fundReleaseRepository;

    @org.springframework.beans.factory.annotation.Autowired
    private FundReleaseConfirmationRepository fundReleaseConfirmationRepository;

    @Autowired
    private com.cc.repository.StudentStatusHistoryRepository studentStatusHistoryRepository;

    @Autowired
    private com.cc.repository.VisaPaymentTransactionRepository visaPaymentTransactionRepository;

    @org.springframework.transaction.annotation.Transactional
    public com.cc.modals.Student submitPassport(Long studentId, String visaManUsername) {
        com.cc.modals.Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found with ID: " + studentId));

        student.setApplicationStatus(com.cc.modals.StudentStatus.PASSPORT_SUBMITTED_TO_EMBASSY);

        com.cc.modals.Student savedStudent = studentRepository.save(student);

        // History
        com.cc.modals.StudentStatusHistory history = new com.cc.modals.StudentStatusHistory();
        history.setStudent(savedStudent);
        history.setStudentApplicationStatus(com.cc.modals.StudentStatus.PASSPORT_SUBMITTED_TO_EMBASSY);
        history.setUpdatedBy(visaManUsername);
        history.setUpdatedAt(java.time.LocalDateTime.now());
        studentStatusHistoryRepository.save(history);

        return savedStudent;
    }

    @org.springframework.transaction.annotation.Transactional
    public com.cc.modals.Student stampVisa(Long studentId, String visaManUsername) {
        com.cc.modals.Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found with ID: " + studentId));

        student.setApplicationStatus(com.cc.modals.StudentStatus.VISA_STAMPED);

        com.cc.modals.Student savedStudent = studentRepository.save(student);

        // History
        com.cc.modals.StudentStatusHistory history = new com.cc.modals.StudentStatusHistory();
        history.setStudent(savedStudent);
        history.setStudentApplicationStatus(com.cc.modals.StudentStatus.VISA_STAMPED);
        history.setUpdatedBy(visaManUsername);
        history.setUpdatedAt(java.time.LocalDateTime.now());
        studentStatusHistoryRepository.save(history);

        return savedStudent;
    }

    public List<FundReleaseDTO> getFundReleases(String username) {
        VisaMan visaMan = visaManRepository.findByUsername(username).orElse(null);
        if (visaMan == null) return new ArrayList<>();

        List<FundRelease> releases = fundReleaseRepository.findByStudent_VisaMan_UsernameOrderByCreatedAtDesc(username);
        return releases.stream().map(r -> {
            FundReleaseDTO dto = new FundReleaseDTO();
            dto.setReleaseId(r.getReleaseId());
            dto.setStudentId(r.getStudent() != null ? r.getStudent().getId() : null);
            dto.setStudentName(r.getStudent() != null ? r.getStudent().getFullName() : "N/A");
            dto.setAgentName(r.getStudent() != null && r.getStudent().getAgent() != null ? r.getStudent().getAgent().getFullName() : "N/A");
            dto.setUniversity(r.getStudent() != null && r.getStudent().getCollege() != null ? r.getStudent().getCollege().getCollegeName() : "N/A");
            dto.setSemester(r.getStudent() != null ? r.getStudent().getIntake() : "N/A");
            dto.setSourceAccount("Visa Operations Account");
            dto.setBeneficiaryBankName(r.getBeneficiaryBankName());
            dto.setBeneficiaryAccountNumber(r.getBeneficiaryAccountNumber());
            dto.setBeneficiarySwiftCode(r.getBeneficiarySwiftCode());
            dto.setBeneficiaryAccountType(r.getBeneficiaryAccountType());
            dto.setVisaFee(r.getVisaFee());
            dto.setServiceCharge(r.getServiceCharge());
            dto.setTotalReleasedAmount(r.getTotalReleasedAmount());
            dto.setTransactionReference(r.getTransactionReference());
            dto.setTransferDate(r.getTransferDate());
            dto.setReceiptUrl(r.getReceiptUrl());
            dto.setRemarks(r.getRemarks());
            dto.setReleasedBy(r.getReleasedBy());
            dto.setReleasedDate(r.getReleasedDate());
            dto.setReleaseStatus(r.getReleaseStatus());
            dto.setRejectionReason(r.getRejectionReason());
            return dto;
        }).collect(Collectors.toList());
    }

    public FundRelease confirmFundRelease(String username, Long releaseId, String remarks) throws Exception {
        FundRelease release = fundReleaseRepository.findById(releaseId)
                .orElseThrow(() -> new Exception("Release not found"));
        
        if (!release.getStudent().getVisaMan().getUsername().equals(username)) {
            throw new Exception("Unauthorized");
        }
        
        release.setReleaseStatus("FUNDS_CONFIRMED");
        FundRelease savedRelease = fundReleaseRepository.save(release);

        // Update Student Status
        com.cc.modals.Student student = savedRelease.getStudent();
        student.setAccToVisaManStatus("SETTLED");
        studentRepository.save(student);

        FundReleaseConfirmation confirmation = new FundReleaseConfirmation();
        confirmation.setFundRelease(savedRelease);
        confirmation.setStudent(savedRelease.getStudent());
        confirmation.setVisaManagerUsername(username);
        confirmation.setConfirmationStatus("CONFIRMED");
        confirmation.setConfirmedBy(username);
        confirmation.setConfirmedDate(LocalDateTime.now());
        confirmation.setRemarks(remarks);
        fundReleaseConfirmationRepository.save(confirmation);

        // History
        com.cc.modals.StudentStatusHistory history = new com.cc.modals.StudentStatusHistory();
        history.setStudent(savedRelease.getStudent());
        history.setStudentApplicationStatus(savedRelease.getStudent().getApplicationStatus());
        history.setUpdatedBy(username);
        history.setUpdatedAt(LocalDateTime.now());
        history.setRemarks("Fund Release Confirmed by Visa Manager. Amount: " + savedRelease.getTotalReleasedAmount() + " INR"
                + (remarks != null ? ". Remarks: " + remarks : ""));
        studentStatusHistoryRepository.save(history);

        // Log transaction in Visa Payment Timeline
        com.cc.modals.VisaPaymentTransaction tx = new com.cc.modals.VisaPaymentTransaction();
        tx.setStudentId(student.getId());
        tx.setApplicationId(student.getApplicationId());
        tx.setTransactionType("FUNDS_RECEIVED_BY_VISAMAN");
        tx.setAmount(savedRelease.getTotalReleasedAmount());
        tx.setCurrency("INR");
        tx.setActorUsername(username);
        tx.setActorRole("VISA_MANAGER");
        tx.setRemarks("Visa Manager confirmed receipt of funds: " + savedRelease.getTotalReleasedAmount() + " INR"
                + (remarks != null ? ". Remarks: " + remarks : ""));
        visaPaymentTransactionRepository.save(tx);

        return savedRelease;
    }

    public FundRelease rejectFundRelease(String username, Long releaseId, String reason, String remarks) throws Exception {
        FundRelease release = fundReleaseRepository.findById(releaseId)
                .orElseThrow(() -> new Exception("Release not found"));
        
        if (!release.getStudent().getVisaMan().getUsername().equals(username)) {
            throw new Exception("Unauthorized");
        }
        
        release.setReleaseStatus("REJECTED");
        release.setRejectionReason(reason);
        FundRelease savedRelease = fundReleaseRepository.save(release);

        // Update Student Status
        com.cc.modals.Student student = savedRelease.getStudent();
        student.setAccToVisaManStatus("REJECTED");
        studentRepository.save(student);

        FundReleaseConfirmation confirmation = new FundReleaseConfirmation();
        confirmation.setFundRelease(savedRelease);
        confirmation.setStudent(savedRelease.getStudent());
        confirmation.setVisaManagerUsername(username);
        confirmation.setConfirmationStatus("REJECTED");
        confirmation.setConfirmedBy(username);
        confirmation.setConfirmedDate(LocalDateTime.now());
        confirmation.setRemarks(remarks);
        confirmation.setRejectionReason(reason);
        fundReleaseConfirmationRepository.save(confirmation);

        // History
        com.cc.modals.StudentStatusHistory history = new com.cc.modals.StudentStatusHistory();
        history.setStudent(savedRelease.getStudent());
        history.setStudentApplicationStatus(savedRelease.getStudent().getApplicationStatus());
        history.setUpdatedBy(username);
        history.setUpdatedAt(LocalDateTime.now());
        history.setRemarks("Fund Release Rejected by Visa Manager. Reason: " + reason
                + (remarks != null ? ". Remarks: " + remarks : ""));
        studentStatusHistoryRepository.save(history);

        // Log transaction in Visa Payment Timeline
        com.cc.modals.VisaPaymentTransaction tx = new com.cc.modals.VisaPaymentTransaction();
        tx.setStudentId(student.getId());
        tx.setApplicationId(student.getApplicationId());
        tx.setTransactionType("FUNDS_REJECTED_BY_VISAMAN");
        tx.setAmount(savedRelease.getTotalReleasedAmount());
        tx.setCurrency("INR");
        tx.setActorUsername(username);
        tx.setActorRole("VISA_MANAGER");
        tx.setRemarks("Visa Manager rejected funds release. Reason: " + reason);
        visaPaymentTransactionRepository.save(tx);

        return savedRelease;
    }
}
