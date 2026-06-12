package com.cc.service.report.impl;

import com.cc.DTO.report.ColumnDefDTO;
import com.cc.DTO.report.ReportRequest;
import com.cc.DTO.report.ReportResponseDTO;
import com.cc.DTO.report.ReportSummaryDTO;
import com.cc.modals.Agent;
import com.cc.modals.College;
import com.cc.modals.PassbookForAccountantAgent;
import com.cc.modals.Student;
import com.cc.repository.AgentRepository;
import com.cc.repository.PassbookAccountantAgentRepository;
import com.cc.repository.StudentRepository;
import com.cc.service.report.ReportGenerator;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ReportAgentCollegeWiseGenerator implements ReportGenerator {

    private final PassbookAccountantAgentRepository passbookRepository;
    private final StudentRepository studentRepository;
    private final AgentRepository agentRepository;

    public ReportAgentCollegeWiseGenerator(PassbookAccountantAgentRepository passbookRepository,
                                           StudentRepository studentRepository,
                                           AgentRepository agentRepository) {
        this.passbookRepository = passbookRepository;
        this.studentRepository = studentRepository;
        this.agentRepository = agentRepository;
    }

    @Override
    public String getCategory() {
        return "collection-reports";
    }

    @Override
    public String getReportName() {
        return "agent-college-wise";
    }

    @Override
    public ReportResponseDTO generate(ReportRequest request) {
        ReportResponseDTO response = new ReportResponseDTO();

        List<ColumnDefDTO> columns = new ArrayList<>();
        columns.add(new ColumnDefDTO("college", "College / University", "string"));
        columns.add(new ColumnDefDTO("totalStudents", "Total Students", "number"));
        columns.add(new ColumnDefDTO("stampedVisas", "Stamped Visas", "number"));
        columns.add(new ColumnDefDTO("requested", "Deal Amount (₹)", "currency"));
        columns.add(new ColumnDefDTO("paid", "Paid Amount (₹)", "currency"));
        columns.add(new ColumnDefDTO("balance", "Outstanding (₹)", "currency"));
        response.setColumns(columns);

        String search = request.getSearch() != null ? request.getSearch().trim().toLowerCase() : "";

        // Determine target agents
        List<Agent> targetAgents = new ArrayList<>();
        String agentDisplay = "All Agents";
        if (!search.isEmpty()) {
            List<Agent> allAgents = agentRepository.findAll();
            for (Agent a : allAgents) {
                String username = a.getUserId() != null ? a.getUserId().toLowerCase() : "";
                String fullName = a.getFullName() != null ? a.getFullName().toLowerCase() : "";
                if (username.equals(search) || username.contains(search) || fullName.contains(search)) {
                    targetAgents.add(a);
                    if (agentDisplay.equals("All Agents")) agentDisplay = a.getFullName();
                }
            }
        } else {
            targetAgents = agentRepository.findAll();
        }

        if (targetAgents.isEmpty()) {
            response.setData(new ArrayList<>());
            response.setTotalRecords(0);
            return response;
        }

        List<String> targetAgentIds = new ArrayList<>();
        for (Agent a : targetAgents) {
            targetAgentIds.add(a.getUserId());
        }

        LocalDateTime startDate = null;
        LocalDateTime endDate = null;
        if (request.getStartDate() != null && !request.getStartDate().isEmpty()) {
            startDate = LocalDate.parse(request.getStartDate()).atStartOfDay();
        }
        if (request.getEndDate() != null && !request.getEndDate().isEmpty()) {
            endDate = LocalDate.parse(request.getEndDate()).atTime(23, 59, 59);
        }

        List<Student> allStudents = studentRepository.findAll();
        List<PassbookForAccountantAgent> allPassbooks = passbookRepository.findAll();

        Map<String, CollegeData> collegeMap = new HashMap<>();

        for (Student s : allStudents) {
            String agentId = s.getAgent() != null ? s.getAgent().getUserId() : null;
            if (agentId != null && targetAgentIds.contains(agentId)) {
                String collegeName = s.getCollege() != null && s.getCollege().getCollegeName() != null ? s.getCollege().getCollegeName() : "Unknown College";
                
                CollegeData cd = collegeMap.computeIfAbsent(collegeName, k -> new CollegeData());
                cd.totalStudents++;
                
                // Proxy for stamped visa - if visa number exists or photo exists
                if (s.getVisaNumber() != null || s.getVisaPhotoPath() != null) {
                    cd.stampedVisas++;
                }
            }
        }

        // Aggregate passbook finances
        for (PassbookForAccountantAgent pb : allPassbooks) {
            String paidByAgent = pb.getPaidByAgent();
            if (paidByAgent != null && targetAgentIds.contains(paidByAgent)) {
                LocalDateTime txDate = pb.getRequestDateTimeByAccountant() != null ? pb.getRequestDateTimeByAccountant() : pb.getPaymentDateTimeByAgent();
                if (isWithinRange(txDate, startDate, endDate)) {
                    // Try to map to college based on student applicationId
                    String collegeName = "Unknown College";
                    if (pb.getApplicationId() != null) {
                        Student s = allStudents.stream()
                            .filter(st -> pb.getApplicationId().equals(st.getApplicationId()))
                            .findFirst().orElse(null);
                        if (s != null && s.getCollege() != null && s.getCollege().getCollegeName() != null) {
                            collegeName = s.getCollege().getCollegeName();
                        }
                    }
                    
                    CollegeData cd = collegeMap.computeIfAbsent(collegeName, k -> new CollegeData());
                    cd.totalRequested += pb.getRequestedAmountByAccountantToAgent();
                    cd.totalPaid += pb.getAmountPaidByAgent();
                }
            }
        }

        List<Map<String, Object>> data = new ArrayList<>();
        double grandRequested = 0.0;
        double grandPaid = 0.0;
        int grandStudents = 0;

        for (Map.Entry<String, CollegeData> entry : collegeMap.entrySet()) {
            Map<String, Object> row = new HashMap<>();
            row.put("college", entry.getKey());
            row.put("totalStudents", entry.getValue().totalStudents);
            row.put("stampedVisas", entry.getValue().stampedVisas);
            row.put("requested", String.format("%.2f", entry.getValue().totalRequested));
            row.put("paid", String.format("%.2f", entry.getValue().totalPaid));
            row.put("balance", String.format("%.2f", entry.getValue().totalRequested - entry.getValue().totalPaid));
            data.add(row);
            
            grandRequested += entry.getValue().totalRequested;
            grandPaid += entry.getValue().totalPaid;
            grandStudents += entry.getValue().totalStudents;
        }

        // Sort alphabetically by college
        data.sort((a, b) -> ((String)a.get("college")).compareTo((String)b.get("college")));

        response.setData(data);
        response.setTotalRecords(data.size());

        ReportSummaryDTO summary = new ReportSummaryDTO();
        summary.addMetric("Target", agentDisplay);
        summary.addMetric("Total Colleges", String.valueOf(collegeMap.size()));
        summary.addMetric("Total Students", String.valueOf(grandStudents));
        summary.addMetric("Total Deal Expected", String.format("₹%.2f", grandRequested));
        summary.addMetric("Total Paid", String.format("₹%.2f", grandPaid));
        summary.addMetric("Outstanding", String.format("₹%.2f", grandRequested - grandPaid));
        response.setSummary(summary);

        return response;
    }

    private boolean isWithinRange(LocalDateTime date, LocalDateTime startDate, LocalDateTime endDate) {
        if (date == null) return true;
        if (startDate != null && date.isBefore(startDate)) return false;
        if (endDate != null && date.isAfter(endDate)) return false;
        return true;
    }

    private static class CollegeData {
        int totalStudents = 0;
        int stampedVisas = 0;
        double totalRequested = 0.0;
        double totalPaid = 0.0;
    }
}
