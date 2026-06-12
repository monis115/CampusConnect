package com.cc.DTO.report;

import java.util.LinkedHashMap;
import java.util.Map;

public class ReportSummaryDTO {
    // Key: Metric Name (e.g. "Total Revenue"), Value: Metric Value (e.g. 5000.00)
    private Map<String, Object> metrics = new LinkedHashMap<>();

    public ReportSummaryDTO() {}

    public void addMetric(String name, Object value) {
        this.metrics.put(name, value);
    }

    public Map<String, Object> getMetrics() {
        return metrics;
    }

    public void setMetrics(Map<String, Object> metrics) {
        this.metrics = metrics;
    }
}
