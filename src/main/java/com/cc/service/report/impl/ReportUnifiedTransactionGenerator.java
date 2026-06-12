package com.cc.service.report.impl;

import com.cc.DTO.report.ColumnDefDTO;
import com.cc.DTO.report.ReportRequest;
import com.cc.service.report.impl.DateFilterUtil;
import com.cc.DTO.report.ReportResponseDTO;
import com.cc.DTO.report.ReportSummaryDTO;
import com.cc.modals.Agent;
import com.cc.modals.PassbookForAccountantAgent;
import com.cc.modals.RequestMoney;
import com.cc.modals.PassbookVisaManAccountant;
import com.cc.repository.AgentRepository;
import com.cc.repository.PassbookAccountantAgentRepository;
import com.cc.repository.RequestMoneyRepository;
import com.cc.repository.PassbookVisaManAccountantRepository;
import com.cc.service.report.ReportGenerator;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ReportUnifiedTransactionGenerator implements ReportGenerator {

    private final PassbookAccountantAgentRepository agentPassbookRepository;
    private final PassbookVisaManAccountantRepository visaManPassbookRepository;
    private final RequestMoneyRepository requestMoneyRepository;
    private final AgentRepository agentRepository;

    public ReportUnifiedTransactionGenerator(PassbookAccountantAgentRepository agentPassbookRepository,
            PassbookVisaManAccountantRepository visaManPassbookRepository,
            RequestMoneyRepository requestMoneyRepository,
            AgentRepository agentRepository) {
        this.agentPassbookRepository = agentPassbookRepository;
        this.visaManPassbookRepository = visaManPassbookRepository;
        this.requestMoneyRepository = requestMoneyRepository;
        this.agentRepository = agentRepository;
    }

    @Override
    public String getCategory() {
        return "financial-reports";
    }

    @Override
    public String getReportName() {
        return "unified-transaction-summary";
    }

    @Override
    public ReportResponseDTO generate(ReportRequest request) {
        ReportResponseDTO response = new ReportResponseDTO();

        List<ColumnDefDTO> columns = new ArrayList<>();
        columns.add(new ColumnDefDTO("date", "Date", "string"));
        columns.add(new ColumnDefDTO("userName", "User Name", "string"));
        columns.add(new ColumnDefDTO("particulars", "Particulars / Purpose", "string"));
        columns.add(new ColumnDefDTO("role", "Role", "string"));
        columns.add(new ColumnDefDTO("requested", "Requested (₹)", "currency"));
        columns.add(new ColumnDefDTO("paid", "Paid (₹)", "currency"));
        columns.add(new ColumnDefDTO("balance", "Balance (₹)", "currency"));
        columns.add(new ColumnDefDTO("status", "Status", "status"));
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

        List<Map<String, Object>> data = new ArrayList<>();
        double totalRequested = 0.0;
        double totalPaid = 0.0;
        String userDisplay = search.isEmpty() ? "All Users" : request.getSearch();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MMM-yyyy HH:mm");

        // 1. Agents (from PassbookForAccountantAgent)
        List<String> targetAgentUsernames = new ArrayList<>();
        if (!search.isEmpty()) {
            List<Agent> allAgents = agentRepository.findAll();
            for (Agent a : allAgents) {
                String username = a.getUserId() != null ? a.getUserId().toLowerCase() : "";
                String fullName = a.getFullName() != null ? a.getFullName().toLowerCase() : "";
                if (username.equals(search) || username.contains(search) || fullName.contains(search)) {
                    if (a.getUserId() != null)
                        targetAgentUsernames.add(a.getUserId());
                }
            }
        }

        List<PassbookForAccountantAgent> agentPassbooks = agentPassbookRepository.findAll();
        for (PassbookForAccountantAgent pb : agentPassbooks) {
            String paidByAgent = pb.getPaidByAgent() != null ? pb.getPaidByAgent().toLowerCase() : "";
            String paidByAgentName = pb.getPaidByAgentFullName() != null ? pb.getPaidByAgentFullName().toLowerCase()
                    : "";
            boolean matchesSearch = search.isEmpty() ||
                    (!targetAgentUsernames.isEmpty() && targetAgentUsernames.contains(pb.getPaidByAgent())) ||
                    paidByAgent.contains(search) || paidByAgentName.contains(search);

            if (matchesSearch) {
                LocalDateTime txDate = pb.getRequestDateTimeByAccountant() != null ? pb.getRequestDateTimeByAccountant()
                        : pb.getPaymentDateTimeByAgent();
                if (isWithinRange(txDate, startDate, endDate)) {
                    Map<String, Object> row = new HashMap<>();
                    row.put("date", txDate != null ? txDate.format(formatter) : "-");

                    String particulars = "Student: " + (pb.getStudentName() != null ? pb.getStudentName() : "N/A");
                    if (pb.getApplicationId() != null)
                        particulars += " (" + pb.getApplicationId() + ")";
                    row.put("particulars", particulars);

                    row.put("userName", pb.getPaidByAgentFullName() != null ? pb.getPaidByAgentFullName() : (pb.getPaidByAgent() != null ? pb.getPaidByAgent() : "N/A"));
                    row.put("role", "Agent");
                    double req = pb.getRequestedAmountByAccountantToAgent();
                    double paid = pb.getAmountPaidByAgent();
                    row.put("requested", String.format("%.2f", req));
                    row.put("paid", String.format("%.2f", paid));
                    row.put("balance", String.format("%.2f", req - paid));
                    String derivedStatus = paid >= req ? "PAID" : (paid > 0 ? "PARTIAL" : "PENDING");
                    row.put("status", derivedStatus);
                    data.add(row);

                    totalRequested += req;
                    totalPaid += paid;
                }
            }
        }

        // 2. Visa Managers & Doc Persons (from RequestMoney)
        List<RequestMoney> moneyRequests = requestMoneyRepository.findAll();
        moneyRequests = DateFilterUtil.filter(moneyRequests, request, RequestMoney::getCreatedAt);
        for (RequestMoney rm : moneyRequests) {
            String reqUser = rm.getRequesterUsername() != null ? rm.getRequesterUsername().toLowerCase() : "";
            String reqName = rm.getRequesterFullName() != null ? rm.getRequesterFullName().toLowerCase() : "";
            boolean matchesSearch = search.isEmpty() || reqUser.contains(search) || reqName.contains(search);

            if (matchesSearch) {
                LocalDateTime txDate = rm.getCreatedAt();
                if (isWithinRange(txDate, startDate, endDate)) {
                    Map<String, Object> row = new HashMap<>();
                    row.put("date", txDate != null ? txDate.format(formatter) : "-");
                    row.put("particulars", rm.getPurpose() != null ? rm.getPurpose() : "N/A");
                    row.put("userName", rm.getRequesterFullName() != null ? rm.getRequesterFullName() : (rm.getRequesterUsername() != null ? rm.getRequesterUsername() : "N/A"));
                    row.put("role", rm.getRequesterRole() != null ? rm.getRequesterRole() : "User");

                    double req = rm.getRequestedAmount() != null ? rm.getRequestedAmount() : 0.0;
                    double paid = rm.getAmountPaid() != null ? rm.getAmountPaid() : 0.0;

                    row.put("requested", String.format("%.2f", req));
                    row.put("paid", String.format("%.2f", paid));
                    row.put("balance", String.format("%.2f", req - paid));
                    row.put("status", rm.getStatus() != null ? rm.getStatus() : "N/A");
                    data.add(row);

                    totalRequested += req;
                    totalPaid += paid;
                }
            }
        }

        // 3. Visa Managers (from PassbookVisaManAccountant for older/student-specific
        // records)
        // Only include if RequestMoney isn't already covering these (to avoid
        // duplicates).
        // RequestMoney is usually used for general requests. For students, VisaManager
        // uses PassbookVisaManAccountant.
        List<PassbookVisaManAccountant> visaPassbooks = visaManPassbookRepository.findAll();
        for (PassbookVisaManAccountant pb : visaPassbooks) {
            String reqUser = pb.getRequesterName() != null ? pb.getRequesterName().toLowerCase() : "";
            String reqName = pb.getRequesterFullName() != null ? pb.getRequesterFullName().toLowerCase() : "";
            boolean matchesSearch = search.isEmpty() || reqUser.contains(search) || reqName.contains(search);

            if (matchesSearch) {
                LocalDateTime txDate = pb.getRequestedDateTime();
                if (isWithinRange(txDate, startDate, endDate)) {
                    Map<String, Object> row = new HashMap<>();
                    row.put("date", txDate != null ? txDate.format(formatter) : "-");

                    String particulars = "Visa App: " + (pb.getStudentName() != null ? pb.getStudentName() : "N/A");
                    if (pb.getApplicationId() != null)
                        particulars += " (" + pb.getApplicationId() + ")";
                    row.put("particulars", particulars);

                    row.put("userName", pb.getRequesterFullName() != null ? pb.getRequesterFullName() : (pb.getRequesterName() != null ? pb.getRequesterName() : "N/A"));
                    row.put("role", "VisaMan");

                    double req = pb.getVisaAmountRequest() + pb.getServiceChargeRequested();
                    double paid = pb.getTotalAmountPaidByAccountant();

                    row.put("requested", String.format("%.2f", req));
                    row.put("paid", String.format("%.2f", paid));
                    row.put("balance", String.format("%.2f", req - paid));
                    row.put("status", pb.getStatus() != null ? pb.getStatus() : "N/A");
                    data.add(row);

                    totalRequested += req;
                    totalPaid += paid;
                }
            }
        }

        response.setData(data);
        response.setTotalRecords(data.size());

        ReportSummaryDTO summary = new ReportSummaryDTO();
        summary.addMetric("User", userDisplay);
        summary.addMetric("Total Requested", String.format("₹%.2f", totalRequested));
        summary.addMetric("Total Paid", String.format("₹%.2f", totalPaid));
        summary.addMetric("Total Balance", String.format("₹%.2f", totalRequested - totalPaid));
        response.setSummary(summary);

        return response;
    }

    private boolean isWithinRange(LocalDateTime date, LocalDateTime startDate, LocalDateTime endDate) {
        if (date == null)
            return true;
        if (startDate != null && date.isBefore(startDate))
            return false;
        if (endDate != null && date.isAfter(endDate))
            return false;
        return true;
    }
}
