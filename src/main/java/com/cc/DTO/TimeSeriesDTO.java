package com.cc.DTO;

import java.time.LocalDate;
import java.util.List;

/**
 * DTO for time series data in the admin dashboard.
 */
public class TimeSeriesDTO {

    private String granularity; // daily, weekly, monthly
    private LocalDate dateFrom;
    private LocalDate dateTo;
    private List<DataPoint> data;

    public TimeSeriesDTO() {
    }

    public TimeSeriesDTO(String granularity, LocalDate dateFrom, LocalDate dateTo, List<DataPoint> data) {
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

    public List<DataPoint> getData() {
        return data;
    }

    public void setData(List<DataPoint> data) {
        this.data = data;
    }

    // Inner class for individual data points
    public static class DataPoint {
        private LocalDate date;
        private Long newRegistrations;
        private Long visaStamped;
        private Long admissionLettersGenerated;
        private Long telexIssued;

        public DataPoint() {
        }

        public DataPoint(LocalDate date, Long newRegistrations, Long visaStamped,
                Long admissionLettersGenerated, Long telexIssued) {
            this.date = date;
            this.newRegistrations = newRegistrations;
            this.visaStamped = visaStamped;
            this.admissionLettersGenerated = admissionLettersGenerated;
            this.telexIssued = telexIssued;
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

        public Long getVisaStamped() {
            return visaStamped;
        }

        public void setVisaStamped(Long visaStamped) {
            this.visaStamped = visaStamped;
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
    }
}
