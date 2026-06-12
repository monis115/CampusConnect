package com.cc.service.report.impl;

import com.cc.DTO.report.ColumnDefDTO;
import com.cc.DTO.report.ReportRequest;
import com.cc.service.report.impl.DateFilterUtil;
import com.cc.DTO.report.ReportResponseDTO;
import com.cc.DTO.report.ReportSummaryDTO;
import com.cc.modals.BeneficiaryAccount;
import com.cc.repository.BeneficiaryAccountRepository;
import com.cc.service.report.ReportGenerator;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ReportBeneficiaryReportGenerator implements ReportGenerator {

    private final BeneficiaryAccountRepository beneficiaryRepository;

    public ReportBeneficiaryReportGenerator(BeneficiaryAccountRepository beneficiaryRepository) {
        this.beneficiaryRepository = beneficiaryRepository;
    }

    @Override
    public String getCategory() {
        return "account-reports";
    }

    @Override
    public String getReportName() {
        return "beneficiary-report";
    }

    @Override
    public ReportResponseDTO generate(ReportRequest request) {
        ReportResponseDTO response = new ReportResponseDTO();

        List<ColumnDefDTO> columns = new ArrayList<>();
        columns.add(new ColumnDefDTO("name", "Beneficiary Name", "string"));
        columns.add(new ColumnDefDTO("type", "Type", "string"));
        columns.add(new ColumnDefDTO("bank", "Bank Name", "string"));
        columns.add(new ColumnDefDTO("account", "Account Number", "string"));
        columns.add(new ColumnDefDTO("status", "Status", "status"));
        response.setColumns(columns);

        List<BeneficiaryAccount> beneficiaries = beneficiaryRepository.findAll();
        beneficiaries = DateFilterUtil.filter(beneficiaries, request, BeneficiaryAccount::getCreatedAt);
        List<Map<String, Object>> data = new ArrayList<>();

        int activeCount = 0;
        int totalCount = beneficiaries.size();

        for (BeneficiaryAccount b : beneficiaries) {
            Map<String, Object> row = new HashMap<>();
            row.put("name", b.getBeneficiaryName());
            row.put("type", b.getBeneficiaryType());
            row.put("bank", b.getBankName());
            row.put("account", b.getAccountNumber());
            
            String status = b.getStatus() != null ? b.getStatus() : "Active";
            row.put("status", status);
            
            if ("Active".equalsIgnoreCase(status)) activeCount++;

            data.add(row);
        }

        response.setData(data);
        response.setTotalRecords(data.size());

        ReportSummaryDTO summary = new ReportSummaryDTO();
        summary.addMetric("Total Beneficiaries", String.valueOf(totalCount));
        summary.addMetric("Active Beneficiaries", String.valueOf(activeCount));
        response.setSummary(summary);

        return response;
    }
}
