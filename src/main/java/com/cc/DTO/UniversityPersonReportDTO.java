package com.cc.DTO;

/**
 * DTO for UniversityPerson performance reports
 */
public class UniversityPersonReportDTO {

    private Long id;
    private String userName;
    private String collegeName;
    private Long collegeId;
    private String period;
    private UniversityPersonStatsDTO periodStats;

    public UniversityPersonReportDTO() {
    }

    public UniversityPersonReportDTO(Long id, String userName, String collegeName, Long collegeId, String period,
            UniversityPersonStatsDTO periodStats) {
        this.id = id;
        this.userName = userName;
        this.collegeName = collegeName;
        this.collegeId = collegeId;
        this.period = period;
        this.periodStats = periodStats;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getCollegeName() {
        return collegeName;
    }

    public void setCollegeName(String collegeName) {
        this.collegeName = collegeName;
    }

    public Long getCollegeId() {
        return collegeId;
    }

    public void setCollegeId(Long collegeId) {
        this.collegeId = collegeId;
    }

    public String getPeriod() {
        return period;
    }

    public void setPeriod(String period) {
        this.period = period;
    }

    public UniversityPersonStatsDTO getPeriodStats() {
        return periodStats;
    }

    public void setPeriodStats(UniversityPersonStatsDTO periodStats) {
        this.periodStats = periodStats;
    }

    /**
     * Nested Stats DTO for UniversityPerson metrics
     */
    public static class UniversityPersonStatsDTO {
        private int totalStudents;
        private int lettersUploaded;
        private int conditionalOffersUploaded;
        private int pendingLetters;
        private Double lettersUploadedTrend;
        private Double conditionalOffersTrend;
        private double efficiencyRate;

        public UniversityPersonStatsDTO() {
        }

        // Getters and Setters
        public int getTotalStudents() {
            return totalStudents;
        }

        public void setTotalStudents(int totalStudents) {
            this.totalStudents = totalStudents;
        }

        public int getLettersUploaded() {
            return lettersUploaded;
        }

        public void setLettersUploaded(int lettersUploaded) {
            this.lettersUploaded = lettersUploaded;
        }

        public int getConditionalOffersUploaded() {
            return conditionalOffersUploaded;
        }

        public void setConditionalOffersUploaded(int conditionalOffersUploaded) {
            this.conditionalOffersUploaded = conditionalOffersUploaded;
        }

        public int getPendingLetters() {
            return pendingLetters;
        }

        public void setPendingLetters(int pendingLetters) {
            this.pendingLetters = pendingLetters;
        }

        public Double getLettersUploadedTrend() {
            return lettersUploadedTrend;
        }

        public void setLettersUploadedTrend(Double lettersUploadedTrend) {
            this.lettersUploadedTrend = lettersUploadedTrend;
        }

        public Double getConditionalOffersTrend() {
            return conditionalOffersTrend;
        }

        public void setConditionalOffersTrend(Double conditionalOffersTrend) {
            this.conditionalOffersTrend = conditionalOffersTrend;
        }

        public double getEfficiencyRate() {
            return efficiencyRate;
        }

        public void setEfficiencyRate(double efficiencyRate) {
            this.efficiencyRate = efficiencyRate;
        }

        public static Double calculateTrend(int current, int previous) {
            if (previous == 0) {
                return current > 0 ? 100.0 : null;
            }
            return ((double) (current - previous) / previous) * 100;
        }
    }
}
