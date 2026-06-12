package com.cc.service.report.impl;

import com.cc.DTO.report.ColumnDefDTO;
import com.cc.DTO.report.ReportRequest;
import com.cc.DTO.report.ReportResponseDTO;
import com.cc.DTO.report.ReportSummaryDTO;
import com.cc.modals.Agent;
import com.cc.modals.PassbookForAccountantAgent;
import com.cc.repository.AgentRepository;
import com.cc.repository.PassbookAccountantAgentRepository;
import com.cc.service.report.ReportGenerator;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ReportAgentDetailedStatementGenerator implements ReportGenerator {

    private final PassbookAccountantAgentRepository passbookRepository;
    private final AgentRepository agentRepository;

    public ReportAgentDetailedStatementGenerator(PassbookAccountantAgentRepository passbookRepository, AgentRepository agentRepository) {
        this.passbookRepository = passbookRepository;
        this.agentRepository = agentRepository;
    }

    @Override
    public String getCategory() {
        return "collection-reports";
    }

    @Override
    public String getReportName() {
        return "agent-detailed-statement";
    }

    @Override
    public ReportResponseDTO generate(ReportRequest request) {
        ReportResponseDTO response = new ReportResponseDTO();

        List<ColumnDefDTO> columns = new ArrayList<>();
        columns.add(new ColumnDefDTO("date", "Date", "string"));
        columns.add(new ColumnDefDTO("applicationId", "App ID", "string"));
        columns.add(new ColumnDefDTO("studentName", "Student", "string"));
        columns.add(new ColumnDefDTO("mode", "Mode", "status"));
        columns.add(new ColumnDefDTO("transactionId", "Txn ID", "string"));
        columns.add(new ColumnDefDTO("amount", "Amount Paid (₹)", "currency"));
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

        // Target agents if search is provided
        List<String> targetAgentUsernames = new ArrayList<>();
        String agentDisplay = "All Agents";
        
        if (!search.isEmpty()) {
            List<Agent> allAgents = agentRepository.findAll();
            for (Agent a : allAgents) {
                String username = a.getUserId() != null ? a.getUserId().toLowerCase() : "";
                String fullName = a.getFullName() != null ? a.getFullName().toLowerCase() : "";
                if (username.equals(search) || username.contains(search) || fullName.contains(search)) {
                    if (a.getUserId() != null) targetAgentUsernames.add(a.getUserId());
                    if (agentDisplay.equals("All Agents")) agentDisplay = a.getFullName();
                }
            }
            if (targetAgentUsernames.isEmpty()) {
                response.setData(new ArrayList<>());
                response.setTotalRecords(0);
                ReportSummaryDTO summary = new ReportSummaryDTO();
                summary.addMetric("Status", "Agent not found");
                response.setSummary(summary);
                return response;
            }
        }

        List<PassbookForAccountantAgent> allPassbooks = passbookRepository.findAll();
        List<PassbookForAccountantAgent> agentPassbooks = new ArrayList<>();

        double totalAmountPaid = 0.0;

        for (PassbookForAccountantAgent pb : allPassbooks) {
            boolean matchesSearch = search.isEmpty() || (pb.getPaidByAgent() != null && targetAgentUsernames.contains(pb.getPaidByAgent()));
            
            if (matchesSearch) {
                LocalDateTime txDate = pb.getPaymentDateTimeByAgent();
                if (txDate == null) txDate = pb.getRequestDateTimeByAccountant();
                
                if (isWithinRange(txDate, startDate, endDate)) {
                    agentPassbooks.add(pb);
                    totalAmountPaid += pb.getAmountPaidByAgent();
                }
            }
        }

        agentPassbooks.sort((a, b) -> {
            LocalDateTime dateA = a.getPaymentDateTimeByAgent() != null ? a.getPaymentDateTimeByAgent() : a.getRequestDateTimeByAccountant();
            LocalDateTime dateB = b.getPaymentDateTimeByAgent() != null ? b.getPaymentDateTimeByAgent() : b.getRequestDateTimeByAccountant();
            if (dateA == null && dateB == null) return 0;
            if (dateA == null) return 1;
            if (dateB == null) return -1;
            return dateA.compareTo(dateB);
        });

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MMM-yyyy HH:mm");
        List<Map<String, Object>> data = new ArrayList<>();

        for (PassbookForAccountantAgent pb : agentPassbooks) {
            Map<String, Object> row = new HashMap<>();
            LocalDateTime date = pb.getPaymentDateTimeByAgent() != null ? pb.getPaymentDateTimeByAgent() : pb.getRequestDateTimeByAccountant();
            row.put("date", date != null ? date.format(formatter) : "-");
            row.put("applicationId", pb.getApplicationId() != null ? pb.getApplicationId() : "-");
            row.put("studentName", pb.getStudentName() != null ? pb.getStudentName() : "-");
            row.put("mode", pb.getModeOfPayment() != null ? pb.getModeOfPayment() : "UNKNOWN");
            row.put("transactionId", pb.getTransactionIdOrUpiIdGivenByAgent() != null ? pb.getTransactionIdOrUpiIdGivenByAgent() : "-");
            row.put("amount", String.format("%.2f", pb.getAmountPaidByAgent()));
            data.add(row);
        }

        response.setData(data);
        response.setTotalRecords(data.size());

        ReportSummaryDTO summary = new ReportSummaryDTO();
        summary.addMetric("Agent", agentDisplay);
        summary.addMetric("Total Paid", String.format("₹%.2f", totalAmountPaid));
        summary.addMetric("Transactions", String.valueOf(agentPassbooks.size()));
        response.setSummary(summary);

        return response;
    }

    private boolean isWithinRange(LocalDateTime date, LocalDateTime startDate, LocalDateTime endDate) {
        if (date == null) return true; // Include if no date to be safe
        if (startDate != null && date.isBefore(startDate)) return false;
        if (endDate != null && date.isAfter(endDate)) return false;
        return true;
    }
}
