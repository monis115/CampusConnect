package com.cc.DTO.report;

public class ReportRequest {
    private String category;
    private String reportName;
    private String startDate; // ISO-8601 or specific format e.g. YYYY-MM-DD
    private String endDate;
    private String search;
    private int page = 0;
    private int size = 10;
    private String sort;

    public ReportRequest() {}

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getReportName() { return reportName; }
    public void setReportName(String reportName) { this.reportName = reportName; }

    public String getStartDate() { return startDate; }
    public void setStartDate(String startDate) { this.startDate = startDate; }

    public String getEndDate() { return endDate; }
    public void setEndDate(String endDate) { this.endDate = endDate; }

    public String getSearch() { return search; }
    public void setSearch(String search) { this.search = search; }

    public int getPage() { return page; }
    public void setPage(int page) { this.page = page; }

    public int getSize() { return size; }
    public void setSize(int size) { this.size = size; }

    public String getSort() { return sort; }
    public void setSort(String sort) { this.sort = sort; }
}
