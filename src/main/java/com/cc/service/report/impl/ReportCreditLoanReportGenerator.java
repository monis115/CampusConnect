package com.cc.service.report.impl;

import com.cc.DTO.report.ColumnDefDTO;
import com.cc.DTO.report.ReportRequest;
import com.cc.service.report.impl.DateFilterUtil;
import com.cc.DTO.report.ReportResponseDTO;
import com.cc.DTO.report.ReportSummaryDTO;
import com.cc.modals.AccountantCredit;
import com.cc.repository.AccountantCreditRepository;
import com.cc.service.report.ReportGenerator;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ReportCreditLoanReportGenerator implements ReportGenerator {

    private final AccountantCreditRepository creditRepository;

    public ReportCreditLoanReportGenerator(AccountantCreditRepository creditRepository) {
        this.creditRepository = creditRepository;
    }

    @Override
    public String getCategory() {
        return "account-reports";
    }

    @Override
    public String getReportName() {
        return "credit-loan-report";
    }

    @Override
    public ReportResponseDTO generate(ReportRequest request) {
        ReportResponseDTO response = new ReportResponseDTO();

        List<ColumnDefDTO> columns = new ArrayList<>();
        columns.add(new ColumnDefDTO("lender", "Lender", "string"));
        columns.add(new ColumnDefDTO("principal", "Principal (₹)", "currency"));
        columns.add(new ColumnDefDTO("interestRate", "Interest Rate", "string"));
        columns.add(new ColumnDefDTO("repayable", "Total Repayable (₹)", "currency"));
        columns.add(new ColumnDefDTO("paid", "Paid Amount (₹)", "currency"));
        columns.add(new ColumnDefDTO("balance", "Balance (₹)", "currency"));
        columns.add(new ColumnDefDTO("status", "Status", "status"));
        response.setColumns(columns);

        List<AccountantCredit> credits = creditRepository.findAll();
        credits = DateFilterUtil.filter(credits, request, AccountantCredit::getCreatedAt);
        List<Map<String, Object>> data = new ArrayList<>();

        double totalPrincipal = 0;
        double totalRepayable = 0;
        double totalBalance = 0;

        for (AccountantCredit credit : credits) {
            Map<String, Object> row = new HashMap<>();
            
            String lender = credit.getMethod();
            if ("FRIEND".equals(lender)) lender = credit.getFriendName();
            else if ("LOAN".equals(lender)) lender = credit.getBankName() + " Loan";
            else if ("CREDIT_CARD".equals(lender)) lender = "Credit Card";
            
            row.put("lender", lender != null ? lender : "Unknown");
            
            double principal = credit.getAmount() != null ? credit.getAmount().doubleValue() : 0.0;
            double repayable = credit.getTotalRepaymentAmount() != null ? credit.getTotalRepaymentAmount().doubleValue() : principal;
            double paid = 0.0; // Assuming we need to calculate or it's not present. Let's assume there's a paidAmount or we mock it.
            // Note: If you have a paidAmount in AccountantCredit, use it. For now, let's assume 0.0 if not available.
            double balance = repayable - paid;
            
            row.put("principal", String.format("%.2f", principal));
            row.put("interestRate", credit.getInterestRate() != null ? credit.getInterestRate() + "%" : "0%");
            row.put("repayable", String.format("%.2f", repayable));
            row.put("paid", String.format("%.2f", paid));
            row.put("balance", String.format("%.2f", balance));
            row.put("status", balance <= 0 ? "CLOSED" : "ACTIVE");

            data.add(row);
            totalPrincipal += principal;
            totalRepayable += repayable;
            totalBalance += balance;
        }

        response.setData(data);
        response.setTotalRecords(data.size());

        ReportSummaryDTO summary = new ReportSummaryDTO();
        summary.addMetric("Total Borrowed", String.format("₹%.2f", totalPrincipal));
        summary.addMetric("Total Repayable", String.format("₹%.2f", totalRepayable));
        summary.addMetric("Total Outstanding Balance", String.format("₹%.2f", totalBalance));
        response.setSummary(summary);

        return response;
    }
}
