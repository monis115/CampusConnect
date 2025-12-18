package com.cc.DTO;

/**
 * DTO for high-level college statistics (KPI cards).
 */
public class CollegeStatsOverviewDTO {

    private Long totalStudents;
    private Long activePipeline;
    private Double visaSuccessRate;
    private Double avgProcessingDays;
    private Double conversionRate;
    private Long newRegistrationsInPeriod;
    private Long visaStampedInPeriod;
    private Long rejectedInPeriod;
    private Long completedInPeriod;

    // Trend indicators (percentage change from previous period)
    private Double totalStudentsTrend;
    private Double activePipelineTrend;
    private Double visaSuccessRateTrend;
    private Double avgProcessingDaysTrend;
    private Double conversionRateTrend;

    public CollegeStatsOverviewDTO() {
    }

    // Getters and Setters
    public Long getTotalStudents() {
        return totalStudents;
    }

    public void setTotalStudents(Long totalStudents) {
        this.totalStudents = totalStudents;
    }

    public Long getActivePipeline() {
        return activePipeline;
    }

    public void setActivePipeline(Long activePipeline) {
        this.activePipeline = activePipeline;
    }

    public Double getVisaSuccessRate() {
        return visaSuccessRate;
    }

    public void setVisaSuccessRate(Double visaSuccessRate) {
        this.visaSuccessRate = visaSuccessRate;
    }

    public Double getAvgProcessingDays() {
        return avgProcessingDays;
    }

    public void setAvgProcessingDays(Double avgProcessingDays) {
        this.avgProcessingDays = avgProcessingDays;
    }

    public Double getConversionRate() {
        return conversionRate;
    }

    public void setConversionRate(Double conversionRate) {
        this.conversionRate = conversionRate;
    }

    public Long getNewRegistrationsInPeriod() {
        return newRegistrationsInPeriod;
    }

    public void setNewRegistrationsInPeriod(Long newRegistrationsInPeriod) {
        this.newRegistrationsInPeriod = newRegistrationsInPeriod;
    }

    public Long getVisaStampedInPeriod() {
        return visaStampedInPeriod;
    }

    public void setVisaStampedInPeriod(Long visaStampedInPeriod) {
        this.visaStampedInPeriod = visaStampedInPeriod;
    }

    public Long getRejectedInPeriod() {
        return rejectedInPeriod;
    }

    public void setRejectedInPeriod(Long rejectedInPeriod) {
        this.rejectedInPeriod = rejectedInPeriod;
    }

    public Long getCompletedInPeriod() {
        return completedInPeriod;
    }

    public void setCompletedInPeriod(Long completedInPeriod) {
        this.completedInPeriod = completedInPeriod;
    }

    public Double getTotalStudentsTrend() {
        return totalStudentsTrend;
    }

    public void setTotalStudentsTrend(Double totalStudentsTrend) {
        this.totalStudentsTrend = totalStudentsTrend;
    }

    public Double getActivePipelineTrend() {
        return activePipelineTrend;
    }

    public void setActivePipelineTrend(Double activePipelineTrend) {
        this.activePipelineTrend = activePipelineTrend;
    }

    public Double getVisaSuccessRateTrend() {
        return visaSuccessRateTrend;
    }

    public void setVisaSuccessRateTrend(Double visaSuccessRateTrend) {
        this.visaSuccessRateTrend = visaSuccessRateTrend;
    }

    public Double getAvgProcessingDaysTrend() {
        return avgProcessingDaysTrend;
    }

    public void setAvgProcessingDaysTrend(Double avgProcessingDaysTrend) {
        this.avgProcessingDaysTrend = avgProcessingDaysTrend;
    }

    public Double getConversionRateTrend() {
        return conversionRateTrend;
    }

    public void setConversionRateTrend(Double conversionRateTrend) {
        this.conversionRateTrend = conversionRateTrend;
    }
}
