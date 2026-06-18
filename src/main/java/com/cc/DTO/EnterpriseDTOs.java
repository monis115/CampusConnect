package com.cc.DTO;

import java.util.List;
import java.util.Map;

public class EnterpriseDTOs {

    public static class ForecastPoint {
        private String date;
        private Double amount;
        private Double riskLevel;

        public ForecastPoint() {}

        public ForecastPoint(String date, Double amount, Double riskLevel) {
            this.date = date;
            this.amount = amount;
            this.riskLevel = riskLevel;
        }

        public String getDate() { return date; }
        public void setDate(String date) { this.date = date; }
        public Double getAmount() { return amount; }
        public void setAmount(Double amount) { this.amount = amount; }
        public Double getRiskLevel() { return riskLevel; }
        public void setRiskLevel(Double riskLevel) { this.riskLevel = riskLevel; }
    }

    public static class HeatmapRegion {
        private String id;
        private String name;
        private Double revenue;
        private Integer students;

        public HeatmapRegion() {}

        public HeatmapRegion(String id, String name, Double revenue, Integer students) {
            this.id = id;
            this.name = name;
            this.revenue = revenue;
            this.students = students;
        }

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public Double getRevenue() { return revenue; }
        public void setRevenue(Double revenue) { this.revenue = revenue; }
        public Integer getStudents() { return students; }
        public void setStudents(Integer students) { this.students = students; }
    }

    public static class AuditLog {
        private String id;
        private String time;
        private String event;
        private String risk;
        private String user;
        private String details;

        public AuditLog() {}

        public AuditLog(String id, String time, String event, String risk, String user, String details) {
            this.id = id;
            this.time = time;
            this.event = event;
            this.risk = risk;
            this.user = user;
            this.details = details;
        }

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getTime() { return time; }
        public void setTime(String time) { this.time = time; }
        public String getEvent() { return event; }
        public void setEvent(String event) { this.event = event; }
        public String getRisk() { return risk; }
        public void setRisk(String risk) { this.risk = risk; }
        public String getUser() { return user; }
        public void setUser(String user) { this.user = user; }
        public String getDetails() { return details; }
        public void setDetails(String details) { this.details = details; }
    }

    public static class EodResult {
        private boolean success;
        private String message;
        private String lockToken;
        private List<String> checks;

        public EodResult() {}

        public EodResult(boolean success, String message, String lockToken, List<String> checks) {
            this.success = success;
            this.message = message;
            this.lockToken = lockToken;
            this.checks = checks;
        }

        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        public String getLockToken() { return lockToken; }
        public void setLockToken(String lockToken) { this.lockToken = lockToken; }
        public List<String> getChecks() { return checks; }
        public void setChecks(List<String> checks) { this.checks = checks; }
    }
}
