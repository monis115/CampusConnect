package com.cc.service.report.impl;

import com.cc.DTO.report.ColumnDefDTO;
import com.cc.DTO.report.ReportRequest;
import com.cc.DTO.report.ReportResponseDTO;
import com.cc.DTO.report.ReportSummaryDTO;
import com.cc.modals.CollectionRequest;
import com.cc.modals.Student;
import com.cc.modals.VisaPaymentTransaction;
import com.cc.repository.CollectionRequestRepository;
import com.cc.repository.StudentRepository;
import com.cc.repository.VisaPaymentTransactionRepository;
import com.cc.service.report.ReportGenerator;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ReportStudentDetailedStatementGenerator implements ReportGenerator {

    private final StudentRepository studentRepository;
    private final CollectionRequestRepository collectionRequestRepository;
    private final VisaPaymentTransactionRepository visaPaymentTransactionRepository;

    public ReportStudentDetailedStatementGenerator(
            StudentRepository studentRepository,
            CollectionRequestRepository collectionRequestRepository,
            VisaPaymentTransactionRepository visaPaymentTransactionRepository) {
        this.studentRepository = studentRepository;
        this.collectionRequestRepository = collectionRequestRepository;
        this.visaPaymentTransactionRepository = visaPaymentTransactionRepository;
    }

    @Override
    public String getCategory() {
        return "collection-reports";
    }

    @Override
    public String getReportName() {
        return "student-detailed-statement";
    }

    @Override
    public ReportResponseDTO generate(ReportRequest request) {
        ReportResponseDTO response = new ReportResponseDTO();

        // Setup columns
        List<ColumnDefDTO> columns = new ArrayList<>();
        columns.add(new ColumnDefDTO("date", "Date", "string"));
        columns.add(new ColumnDefDTO("eventType", "Event Type", "status"));
        columns.add(new ColumnDefDTO("description", "Description", "string"));
        columns.add(new ColumnDefDTO("debit", "Debit (₹)", "currency"));
        columns.add(new ColumnDefDTO("credit", "Credit (₹)", "currency"));
        columns.add(new ColumnDefDTO("balance", "Balance (₹)", "currency"));
        response.setColumns(columns);

        String search = request.getSearch() != null ? request.getSearch().trim().toLowerCase() : "";

        LocalDateTime startDate = null;
        LocalDateTime endDate = null;

        if (request.getStartDate() != null && !request.getStartDate().isEmpty()) {
            startDate = LocalDate.parse(request.getStartDate()).atStartOfDay();
        }
        if (request.getEndDate() != null && !request.getEndDate().isEmpty()) {
            endDate = LocalDate.parse(request.getEndDate()).atTime(23, 59, 59);
        }

        // Target students if search is provided
        List<Long> targetStudentIds = new ArrayList<>();
        String studentDisplay = "All Students";
        String appIdDisplay = "Multiple";

        if (!search.isEmpty()) {
            List<Student> allStudents = studentRepository.findAll();
            for (Student s : allStudents) {
                String fullName = (s.getFullName() != null ? s.getFullName() : "").toLowerCase();
                if (fullName.contains(search)
                        || (s.getApplicationId() != null && s.getApplicationId().toLowerCase().contains(search))) {
                    targetStudentIds.add(s.getId());
                    if (studentDisplay.equals("All Students")) {
                        studentDisplay = s.getFullName() != null ? s.getFullName() : "Unknown";
                        appIdDisplay = s.getApplicationId();
                    }
                }
            }
            if (targetStudentIds.isEmpty()) {
                response.setData(new ArrayList<>());
                response.setTotalRecords(0);
                ReportSummaryDTO summary = new ReportSummaryDTO();
                summary.addMetric("Status", "Student not found");
                response.setSummary(summary);
                return response;
            }
        }

        // Fetch Data
        List<CollectionRequest> collections = search.isEmpty() ? collectionRequestRepository.findAll()
                : collectionRequestRepository.findAll().stream()
                        .filter(cr -> targetStudentIds.contains(cr.getStudentId())).collect(Collectors.toList());
        List<VisaPaymentTransaction> visaPayments = search.isEmpty() ? visaPaymentTransactionRepository.findAll()
                : visaPaymentTransactionRepository.findAll().stream()
                        .filter(vp -> targetStudentIds.contains(vp.getStudentId())).collect(Collectors.toList());

        List<LedgerEvent> ledgerEvents = new ArrayList<>();

        for (CollectionRequest cr : collections) {
            LocalDateTime date = cr.getCreatedAt();
            if (isWithinRange(date, startDate, endDate)) {
                ledgerEvents.add(new LedgerEvent(date, "COLLECTION_REQUEST",
                        "Fee Requested: " + (cr.getRemarks() != null ? cr.getRemarks() : "General"),
                        cr.getRequestedAmount(), 0.0));
                if (cr.getCollectedAmount() != null && cr.getCollectedAmount() > 0) {
                    ledgerEvents.add(
                            new LedgerEvent(date, "COLLECTION_PAID", "Amount Collected", 0.0, cr.getCollectedAmount()));
                }
            }
        }

        for (VisaPaymentTransaction vp : visaPayments) {
            LocalDateTime date = vp.getTransactionDate();
            if (isWithinRange(date, startDate, endDate)) {
                boolean isDebit = vp.getTransactionType().contains("REQUESTED")
                        || vp.getTransactionType().contains("DUE");
                double debit = isDebit ? vp.getAmount() : 0.0;
                double credit = isDebit ? 0.0 : vp.getAmount();
                ledgerEvents.add(new LedgerEvent(date, vp.getTransactionType(),
                        vp.getRemarks() != null ? vp.getRemarks() : "Visa Transaction", debit, credit));
            }
        }

        // Sort events chronologically
        ledgerEvents.sort(Comparator.comparing(LedgerEvent::getDate));

        List<Map<String, Object>> data = new ArrayList<>();
        double runningBalance = 0.0;
        double totalDebit = 0.0;
        double totalCredit = 0.0;

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MMM-yyyy HH:mm");

        for (LedgerEvent event : ledgerEvents) {
            runningBalance += (event.getDebit() - event.getCredit());
            totalDebit += event.getDebit();
            totalCredit += event.getCredit();

            Map<String, Object> row = new HashMap<>();
            row.put("date", event.getDate().format(formatter));
            row.put("eventType", event.getType());
            row.put("description", event.getDescription());
            row.put("debit", String.format("%.2f", event.getDebit()));
            row.put("credit", String.format("%.2f", event.getCredit()));
            row.put("balance", String.format("%.2f", runningBalance));

            data.add(row);
        }

        response.setData(data);
        response.setTotalRecords(data.size());

        ReportSummaryDTO summary = new ReportSummaryDTO();
        summary.addMetric("Student", studentDisplay);
        summary.addMetric("App ID", appIdDisplay);
        summary.addMetric("Total Debits", String.format("₹%.2f", totalDebit));
        summary.addMetric("Total Credits", String.format("₹%.2f", totalCredit));
        summary.addMetric("Closing Balance", String.format("₹%.2f", runningBalance));

        response.setSummary(summary);

        return response;
    }

    private boolean isWithinRange(LocalDateTime date, LocalDateTime startDate, LocalDateTime endDate) {
        if (date == null)
            return false;
        if (startDate != null && date.isBefore(startDate))
            return false;
        if (endDate != null && date.isAfter(endDate))
            return false;
        return true;
    }

    private static class LedgerEvent {
        private final LocalDateTime date;
        private final String type;
        private final String description;
        private final double debit;
        private final double credit;

        public LedgerEvent(LocalDateTime date, String type, String description, double debit, double credit) {
            this.date = date;
            this.type = type;
            this.description = description;
            this.debit = debit;
            this.credit = credit;
        }

        public LocalDateTime getDate() {
            return date;
        }

        public String getType() {
            return type;
        }

        public String getDescription() {
            return description;
        }

        public double getDebit() {
            return debit;
        }

        public double getCredit() {
            return credit;
        }
    }
}
