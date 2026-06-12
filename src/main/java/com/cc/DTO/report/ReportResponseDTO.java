package com.cc.DTO.report;

import java.util.List;
import java.util.Map;

public class ReportResponseDTO {
    
    private ReportSummaryDTO summary;
    private List<ColumnDefDTO> columns;
    private List<Map<String, Object>> data;
    private long totalRecords;

    public ReportResponseDTO() {}

    public ReportResponseDTO(ReportSummaryDTO summary, List<ColumnDefDTO> columns, List<Map<String, Object>> data, long totalRecords) {
        this.summary = summary;
        this.columns = columns;
        this.data = data;
        this.totalRecords = totalRecords;
    }

    public ReportSummaryDTO getSummary() { return summary; }
    public void setSummary(ReportSummaryDTO summary) { this.summary = summary; }

    public List<ColumnDefDTO> getColumns() { return columns; }
    public void setColumns(List<ColumnDefDTO> columns) { this.columns = columns; }

    public List<Map<String, Object>> getData() { return data; }
    public void setData(List<Map<String, Object>> data) { this.data = data; }

    public long getTotalRecords() { return totalRecords; }
    public void setTotalRecords(long totalRecords) { this.totalRecords = totalRecords; }
}
