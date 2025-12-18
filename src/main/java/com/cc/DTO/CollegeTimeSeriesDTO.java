package com.cc.DTO;

import java.time.LocalDate;
import java.util.List;

/**
 * DTO for college-specific time series data (charts).
 */
public class CollegeTimeSeriesDTO {

    private String granularity; // daily, weekly, monthly
    private LocalDate dateFrom;
    private LocalDate dateTo;
    private List<CollegeDataPoint> data;

    public CollegeTimeSeriesDTO() {
    }

    public CollegeTimeSeriesDTO(String granularity, LocalDate dateFrom, LocalDate dateTo, List<CollegeDataPoint> data) {
        this.granularity = granularity;
        this.dateFrom = dateFrom;
        this.dateTo = dateTo;
        this.data = data;
    }

    // Getters and Setters
    public String getGranularity() {
        return granularity;
    }

    public void setGranularity(String granularity) {
        this.granularity = granularity;
    }

    public LocalDate getDateFrom() {
        return dateFrom;
    }

    public void setDateFrom(LocalDate dateFrom) {
        this.dateFrom = dateFrom;
    }

    public LocalDate getDateTo() {
        return dateTo;
    }

    public void setDateTo(LocalDate dateTo) {
        this.dateTo = dateTo;
    }

    public List<CollegeDataPoint> getData() {
        return data;
    }

    public void setData(List<CollegeDataPoint> data) {
        this.data = data;
    }

    /**
     * Inner class for individual data points.
     */
    public static class CollegeDataPoint {
        private LocalDate date;
        private Long newRegistrations;
        private Long admissionLettersGenerated;
        private Long telexIssued;
        private Long visaStamped;
        private Long rejected;

        public CollegeDataPoint() {
        }

        public CollegeDataPoint(LocalDate date) {
            this.date = date;
            this.newRegistrations = 0L;
            this.admissionLettersGenerated = 0L;
            this.telexIssued = 0L;
            this.visaStamped = 0L;
            this.rejected = 0L;
        }

        public LocalDate getDate() {
            return date;
        }

        public void setDate(LocalDate date) {
            this.date = date;
        }

        public Long getNewRegistrations() {
            return newRegistrations;
        }

        public void setNewRegistrations(Long newRegistrations) {
            this.newRegistrations = newRegistrations;
        }

        public Long getAdmissionLettersGenerated() {
            return admissionLettersGenerated;
        }

        public void setAdmissionLettersGenerated(Long admissionLettersGenerated) {
            this.admissionLettersGenerated = admissionLettersGenerated;
        }

        public Long getTelexIssued() {
            return telexIssued;
        }

        public void setTelexIssued(Long telexIssued) {
            this.telexIssued = telexIssued;
        }

        public Long getVisaStamped() {
            return visaStamped;
        }

        public void setVisaStamped(Long visaStamped) {
            this.visaStamped = visaStamped;
        }

        public Long getRejected() {
            return rejected;
        }

        public void setRejected(Long rejected) {
            this.rejected = rejected;
        }
    }
}
