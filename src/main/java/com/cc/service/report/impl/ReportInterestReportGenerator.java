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
public class ReportInterestReportGenerator implements ReportGenerator {

    private final AccountantCreditRepository creditRepository;

    public ReportInterestReportGenerator(AccountantCreditRepository creditRepository) {
        this.creditRepository = creditRepository;
    }

    @Override
    public String getCategory() {
        return "account-reports";
    }

    @Override
    public String getReportName() {
        return "interest-report";
    }

    @Override
    public ReportResponseDTO generate(ReportRequest request) {
        ReportResponseDTO response = new ReportResponseDTO();

        List<ColumnDefDTO> columns = new ArrayList<>();
        columns.add(new ColumnDefDTO("lender", "Lender", "string"));
        columns.add(new ColumnDefDTO("principal", "Principal (₹)", "currency"));
        columns.add(new ColumnDefDTO("interestRate", "Interest Rate", "string"));
        columns.add(new ColumnDefDTO("accruedInterest", "Accrued Interest (₹)", "currency"));
        columns.add(new ColumnDefDTO("paidInterest", "Paid Interest (₹)", "currency"));
        columns.add(new ColumnDefDTO("balanceInterest", "Balance Interest (₹)", "currency"));
        response.setColumns(columns);

        List<AccountantCredit> credits = creditRepository.findAll();
        credits = DateFilterUtil.filter(credits, request, AccountantCredit::getCreatedAt);
        List<Map<String, Object>> data = new ArrayList<>();

        double totalAccrued = 0;
        double totalPaid = 0;
        double totalBalance = 0;

        for (AccountantCredit credit : credits) {
            double rate = credit.getInterestRate() != null ? credit.getInterestRate().doubleValue() : 0.0;
            if (rate > 0) {
                Map<String, Object> row = new HashMap<>();
                
                String lender = credit.getMethod();
                if ("FRIEND".equals(lender)) lender = credit.getFriendName();
                else if ("LOAN".equals(lender)) lender = credit.getBankName() + " Loan";
                else if ("CREDIT_CARD".equals(lender)) lender = "Credit Card";
                
                row.put("lender", lender != null ? lender : "Unknown");
                
                double principal = credit.getAmount() != null ? credit.getAmount().doubleValue() : 0.0;
                double repayable = credit.getTotalRepaymentAmount() != null ? credit.getTotalRepaymentAmount().doubleValue() : principal;
                double accrued = repayable - principal;
                double paid = 0.0; // Approximation if exact interest paid isn't separated in DB
                double balance = accrued - paid;
                
                row.put("principal", String.format("%.2f", principal));
                row.put("interestRate", rate + "%");
                row.put("accruedInterest", String.format("%.2f", accrued));
                row.put("paidInterest", String.format("%.2f", paid));
                row.put("balanceInterest", String.format("%.2f", balance));

                data.add(row);
                totalAccrued += accrued;
                totalPaid += paid;
                totalBalance += balance;
            }
        }

        response.setData(data);
        response.setTotalRecords(data.size());

        ReportSummaryDTO summary = new ReportSummaryDTO();
        summary.addMetric("Total Accrued Interest", String.format("₹%.2f", totalAccrued));
        summary.addMetric("Total Paid Interest", String.format("₹%.2f", totalPaid));
        summary.addMetric("Outstanding Interest", String.format("₹%.2f", totalBalance));
        response.setSummary(summary);

        return response;
    }
}
