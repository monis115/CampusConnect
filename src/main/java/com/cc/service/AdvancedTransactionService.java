package com.cc.service;

import com.cc.DTO.AdvancedTransactionRequestDTO;
import com.cc.DTO.UserTransactionSummaryDTO;
import com.cc.modals.PassbookForAccountantAgent;
import com.cc.modals.RequestMoney;
import com.cc.modals.Student;
import com.cc.repository.PassbookAccountantAgentRepository;
import com.cc.repository.RequestMoneyRepository;
import com.cc.repository.StudentRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Collections;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import com.cc.repository.AgentRepository;
import com.cc.repository.DocPersonRepository;
import com.cc.repository.VisaManRepository;
import com.cc.modals.Agent;
import com.cc.modals.DocPerson;
import com.cc.modals.VisaMan;
@Service
public class AdvancedTransactionService {
    @Autowired
    private AgentRepository agentRepository;
    
    @Autowired
    private DocPersonRepository docPersonRepository;
    
    @Autowired
    private VisaManRepository visaManRepository;


    private final PassbookAccountantAgentRepository agentPassbookRepository;
    private final RequestMoneyRepository requestMoneyRepository;
    private final StudentRepository studentRepository;

    public AdvancedTransactionService(PassbookAccountantAgentRepository agentPassbookRepository,
            RequestMoneyRepository requestMoneyRepository,
            StudentRepository studentRepository) {
        this.agentPassbookRepository = agentPassbookRepository;
        this.requestMoneyRepository = requestMoneyRepository;
        this.studentRepository = studentRepository;
    }

    public UserTransactionSummaryDTO getAdvancedSummary(AdvancedTransactionRequestDTO request) {
        UserTransactionSummaryDTO summary = new UserTransactionSummaryDTO();
        List<Map<String, Object>> transactions = new ArrayList<>();
        double totalInflow = 0.0;
        double totalOutflow = 0.0;

        LocalDateTime startDate = null;
        LocalDateTime endDate = null;
        if (request.getStartDate() != null && !request.getStartDate().isEmpty()) {
            startDate = LocalDate.parse(request.getStartDate()).atStartOfDay();
        }
        if (request.getEndDate() != null && !request.getEndDate().isEmpty()) {
            endDate = LocalDate.parse(request.getEndDate()).atTime(23, 59, 59);
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MMM-yyyy HH:mm");

        String role = request.getRole() != null ? request.getRole().toUpperCase() : "";
        String userId = request.getUserId() != null ? request.getUserId().toLowerCase() : "";
        String collegeName = request.getCollegeName() != null ? request.getCollegeName().toLowerCase() : "";

        if ("AGENT".equals(role) || "UNIVERSITY".equals(role)) {
            List<PassbookForAccountantAgent> agentPassbooks = agentPassbookRepository.findAll();
            for (PassbookForAccountantAgent pb : agentPassbooks) {
                String pbAgentId = pb.getPaidByAgent() != null ? pb.getPaidByAgent().toLowerCase() : "";
                String pbAgentName = pb.getPaidByAgentFullName() != null ? pb.getPaidByAgentFullName().toLowerCase()
                        : "";

                boolean matchesUser = userId.isEmpty() || pbAgentId.contains(userId) || pbAgentName.contains(userId)
                        || userId.equals("all");

                if (matchesUser) {
                    // Check college if provided
                    boolean matchesCollege = true;
                    if (!collegeName.isEmpty() && !collegeName.equals("all")) {
                        if (pb.getApplicationId() != null) {
                            Optional<Student> studentOpt = studentRepository.findByApplicationId(pb.getApplicationId());
                            if (studentOpt.isPresent()) {
                                Student student = studentOpt.get();
                                String studentCollege = student.getCollegeName() != null
                                        ? student.getCollegeName().toLowerCase()
                                        : "";
                                if (!studentCollege.contains(collegeName) && !collegeName.contains(studentCollege)) {
                                    matchesCollege = false;
                                }
                            } else {
                                matchesCollege = false;
                            }
                        } else {
                            matchesCollege = false; // No app ID, can't verify college
                        }
                    }

                    if (matchesCollege) {
                        LocalDateTime txDate = pb.getRequestDateTimeByAccountant() != null
                                ? pb.getRequestDateTimeByAccountant()
                                : pb.getPaymentDateTimeByAgent();
                        if (isWithinRange(txDate, startDate, endDate)) {
                            double inflow = pb.getAmountPaidByAgent();
                            double outflow = 0.0; // Enforce only Inflow for Agent as requested

                            Map<String, Object> row = new HashMap<>();
                            row.put("date", txDate != null ? txDate.format(formatter) : "-");
                            row.put("particulars",
                                    "Student: " + (pb.getStudentName() != null ? pb.getStudentName() : "N/A") + " ("
                                            + pb.getApplicationId() + ")");
                            row.put("role", role);
                            row.put("userName", pb.getPaidByAgentFullName() != null ? pb.getPaidByAgentFullName()
                                    : pb.getPaidByAgent());
                            row.put("inflow", inflow);
                            row.put("outflow", outflow);

                            transactions.add(row);
                            totalInflow += inflow;
                            totalOutflow += outflow;
                        }
                    }
                }
            }
        } else if ("DOCMANAGER".equals(role) || "DOC_PERSON".equals(role) || "VISAMAN".equals(role)) {
            List<RequestMoney> moneyRequests = requestMoneyRepository.findAll();
            for (RequestMoney rm : moneyRequests) {
                String rmRole = rm.getRequesterRole() != null ? rm.getRequesterRole().toUpperCase() : "";
                if (rmRole.contains(role) || role.contains(rmRole)) {
                    String rmUserId = rm.getRequesterUsername() != null ? rm.getRequesterUsername().toLowerCase() : "";
                    String rmUserName = rm.getRequesterFullName() != null ? rm.getRequesterFullName().toLowerCase()
                            : "";

                    boolean matchesUser = userId.isEmpty() || rmUserId.contains(userId) || rmUserName.contains(userId)
                            || userId.equals("all");

                    if (matchesUser) {
                        LocalDateTime txDate = rm.getCreatedAt();
                        if (isWithinRange(txDate, startDate, endDate)) {
                            double outflow = rm.getRequestedAmount() != null ? rm.getRequestedAmount() : 0.0;
                            double inflow = rm.getAmountPaid() != null ? rm.getAmountPaid() : 0.0; // In this context,
                                                                                                   // paid means
                                                                                                   // accountant paid
                                                                                                   // them, so it's
                                                                                                   // outflow for
                                                                                                   // accountant. Wait,
                                                                                                   // let's keep
                                                                                                   // terminology
                                                                                                   // consistent.
                            // If Accountant gives money to DocManager, it's an Outflow from Accountant. So
                            // requested/paid by accountant is Outflow.
                            // In RequestMoney, requestedAmount is what they want. amountPaid is what
                            // accountant actually paid them. So accountant Outflow = amountPaid.

                            Map<String, Object> row = new HashMap<>();
                            row.put("date", txDate != null ? txDate.format(formatter) : "-");
                            row.put("particulars", rm.getPurpose() != null ? rm.getPurpose() : "N/A");
                            row.put("role", rm.getRequesterRole());
                            row.put("userName", rm.getRequesterFullName() != null ? rm.getRequesterFullName()
                                    : rm.getRequesterUsername());

                            // For DocManagers, they request money from Accountant. So Accountant pays them.
                            // This is an Outflow for Accountant.
                            // If they return money, it would be an Inflow. But for now, amountPaid is
                            // Outflow.
                            row.put("inflow", 0.0);
                            row.put("outflow", rm.getAmountPaid() != null ? rm.getAmountPaid() : 0.0);
                            row.put("requested", outflow);

                            transactions.add(row);
                            totalOutflow += rm.getAmountPaid() != null ? rm.getAmountPaid() : 0.0;
                        }
                    }
                }
            }
        }

        summary.setTransactions(transactions);
        summary.setTotalInflow(totalInflow);
        summary.setTotalOutflow(totalOutflow);
        summary.setBalance(totalInflow - totalOutflow);
        return summary;
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

        public Map<String, List<String>> getFilterOptions() {
        Map<String, List<String>> options = new HashMap<>();

        // Get Agents
        Set<String> agents = new HashSet<>();
        List<Agent> allAgents = agentRepository.findAll();
        for (Agent a : allAgents) {
            if (a.getFullName() != null && !a.getFullName().isEmpty()) {
                agents.add(a.getFullName());
            } else if (a.getUserId() != null && !a.getUserId().isEmpty()) {
                agents.add(a.getUserId());
            }
        }

        // Fallback: also add from passbook in case some transactions don't have matching agent records
        List<PassbookForAccountantAgent> agentsPb = agentPassbookRepository.findAll();
        for (PassbookForAccountantAgent pb : agentsPb) {
            if (pb.getPaidByAgentFullName() != null && !pb.getPaidByAgentFullName().isEmpty()) {
                agents.add(pb.getPaidByAgentFullName());
            } else if (pb.getPaidByAgent() != null && !pb.getPaidByAgent().isEmpty()) {
                agents.add(pb.getPaidByAgent());
            }
        }

        // Get Doc Managers
        Set<String> docManagers = new HashSet<>();
        List<DocPerson> allDocPersons = docPersonRepository.findAll();
        for (DocPerson dp : allDocPersons) {
            if (dp.getFullName() != null && !dp.getFullName().isEmpty()) {
                docManagers.add(dp.getFullName());
            } else if (dp.getUsername() != null && !dp.getUsername().isEmpty()) {
                docManagers.add(dp.getUsername());
            }
        }
        List<VisaMan> allVisaMen = visaManRepository.findAll();
        for (VisaMan vm : allVisaMen) {
            if (vm.getFullName() != null && !vm.getFullName().isEmpty()) {
                docManagers.add(vm.getFullName());
            } else if (vm.getUsername() != null && !vm.getUsername().isEmpty()) {
                docManagers.add(vm.getUsername());
            }
        }

        // Fallback: from requests
        List<RequestMoney> requests = requestMoneyRepository.findAll();
        for (RequestMoney rm : requests) {
            if (rm.getRequesterFullName() != null && !rm.getRequesterFullName().isEmpty()) {
                docManagers.add(rm.getRequesterFullName());
            } else if (rm.getRequesterUsername() != null && !rm.getRequesterUsername().isEmpty()) {
                docManagers.add(rm.getRequesterUsername());
            }
        }

        // Get Colleges
        List<Student> students = studentRepository.findAll();
        Set<String> colleges = new HashSet<>();
        for (Student s : students) {
            if (s.getCollegeName() != null && !s.getCollegeName().isEmpty()) {
                colleges.add(s.getCollegeName());
            }
        }

        List<String> agentList = new ArrayList<>(agents);
        Collections.sort(agentList);
        List<String> docList = new ArrayList<>(docManagers);
        Collections.sort(docList);
        List<String> collegeList = new ArrayList<>(colleges);
        Collections.sort(collegeList);

        options.put("agents", agentList);
        options.put("docManagers", docList);
        options.put("colleges", collegeList);

        return options;
    }

}
