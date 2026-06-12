package com.cc.service.report.impl;

import com.cc.DTO.report.ColumnDefDTO;
import com.cc.DTO.report.ReportRequest;
import com.cc.service.report.impl.DateFilterUtil;
import com.cc.DTO.report.ReportResponseDTO;
import com.cc.DTO.report.ReportSummaryDTO;
import com.cc.modals.AccountantCredit;
import com.cc.modals.SubAccount;
import com.cc.repository.AccountantCreditRepository;
import com.cc.repository.SubAccountRepository;
import com.cc.service.report.ReportGenerator;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ReportBalanceSheetGenerator implements ReportGenerator {

    private final SubAccountRepository subAccountRepository;
    private final AccountantCreditRepository creditRepository;

    public ReportBalanceSheetGenerator(SubAccountRepository subAccountRepository, AccountantCreditRepository creditRepository) {
        this.subAccountRepository = subAccountRepository;
        this.creditRepository = creditRepository;
    }

    @Override
    public String getCategory() {
        return "financial-reports";
    }

    @Override
    public String getReportName() {
        return "balance-sheet";
    }

    @Override
    public ReportResponseDTO generate(ReportRequest request) {
        ReportResponseDTO response = new ReportResponseDTO();

        // Columns
        List<ColumnDefDTO> columns = new ArrayList<>();
        columns.add(new ColumnDefDTO("category", "Category", "string"));
        columns.add(new ColumnDefDTO("type", "Type", "string"));
        columns.add(new ColumnDefDTO("amount", "Amount (₹)", "currency"));
        response.setColumns(columns);

        List<Map<String, Object>> data = new ArrayList<>();
        double totalAssets = 0.0;
        double totalLiabilities = 0.0;

        // Assets: SubAccounts (Banks, Wallets, Cash)
        List<SubAccount> subAccounts = subAccountRepository.findAll();
        for (SubAccount acc : subAccounts) {
            double bal = acc.getBalance() != null ? acc.getBalance() : 0.0;
            if (bal > 0) {
                totalAssets += bal;
                Map<String, Object> row = new HashMap<>();
                row.put("category", "Asset");
                
                String type = acc.getAccountType() != null ? acc.getAccountType() : "BANK";
                String name = "BANK".equalsIgnoreCase(type) ? acc.getBankName() + " (" + acc.getAccountNumber() + ")" : acc.getCashAccountName();
                row.put("type", name != null ? name : "Unknown Asset");
                
                row.put("amount", String.format("%.2f", bal));
                data.add(row);
            }
        }

        // Liabilities: Accountant Credits
        List<AccountantCredit> credits = creditRepository.findAll();
        credits = DateFilterUtil.filter(credits, request, AccountantCredit::getCreatedAt);
        for (AccountantCredit credit : credits) {
            double principal = credit.getAmount() != null ? credit.getAmount().doubleValue() : 0.0;
            // Should theoretically check if repaid, but simplified for now
            if (principal > 0) {
                totalLiabilities += principal;
                Map<String, Object> row = new HashMap<>();
                row.put("category", "Liability");
                
                String lender = credit.getMethod();
                if ("FRIEND".equals(lender)) lender = credit.getFriendName();
                else if ("LOAN".equals(lender)) lender = credit.getBankName() + " Loan";
                else if ("CREDIT_CARD".equals(lender)) lender = "Credit Card";
                
                row.put("type", "Borrowing: " + lender);
                row.put("amount", String.format("%.2f", principal));
                data.add(row);
            }
        }

        response.setData(data);
        response.setTotalRecords(data.size());

        ReportSummaryDTO summary = new ReportSummaryDTO();
        summary.addMetric("Total Assets", String.format("₹%.2f", totalAssets));
        summary.addMetric("Total Liabilities", String.format("₹%.2f", totalLiabilities));
        summary.addMetric("Net Equity", String.format("₹%.2f", totalAssets - totalLiabilities));
        response.setSummary(summary);

        return response;
    }
}
