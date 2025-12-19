package com.cc.DTO;

/**
 * DTO for College-centric performance reports
 */
public class CollegeReportDTO {

    private Long collegeId;
    private String collegeName;
    private String city;
    private String country;
    private String period;
    private CoordinatorDTO coordinator;
    private CollegeStatsDTO stats;

    public CollegeReportDTO() {
    }

    public CollegeReportDTO(Long collegeId, String collegeName, String city, String country, String period,
            CoordinatorDTO coordinator, CollegeStatsDTO stats) {
        this.collegeId = collegeId;
        this.collegeName = collegeName;
        this.city = city;
        this.country = country;
        this.period = period;
        this.coordinator = coordinator;
        this.stats = stats;
    }

    // Getters and Setters
    public Long getCollegeId() {
        return collegeId;
    }

    public void setCollegeId(Long collegeId) {
        this.collegeId = collegeId;
    }

    public String getCollegeName() {
        return collegeName;
    }

    public void setCollegeName(String collegeName) {
        this.collegeName = collegeName;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getPeriod() {
        return period;
    }

    public void setPeriod(String period) {
        this.period = period;
    }

    public CoordinatorDTO getCoordinator() {
        return coordinator;
    }

    public void setCoordinator(CoordinatorDTO coordinator) {
        this.coordinator = coordinator;
    }

    public CollegeStatsDTO getStats() {
        return stats;
    }

    public void setStats(CollegeStatsDTO stats) {
        this.stats = stats;
    }

    /**
     * Nested DTO for University Coordinator info
     */
    public static class CoordinatorDTO {
        private Long id;
        private String userName;
        private String status;

        public CoordinatorDTO() {
        }

        public CoordinatorDTO(Long id, String userName, String status) {
            this.id = id;
            this.userName = userName;
            this.status = status;
        }

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

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }
    }

    /**
     * Nested DTO for College performance stats
     */
    public static class CollegeStatsDTO {
        private int totalStudents;
        private int lettersUploaded;
        private int conditionalOffers;
        private int pendingLetters;
        private double efficiencyRate;
        private Double lettersTrend;
        private Double conditionalOffersTrend;

        public CollegeStatsDTO() {
        }

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

        public int getConditionalOffers() {
            return conditionalOffers;
        }

        public void setConditionalOffers(int conditionalOffers) {
            this.conditionalOffers = conditionalOffers;
        }

        public int getPendingLetters() {
            return pendingLetters;
        }

        public void setPendingLetters(int pendingLetters) {
            this.pendingLetters = pendingLetters;
        }

        public double getEfficiencyRate() {
            return efficiencyRate;
        }

        public void setEfficiencyRate(double efficiencyRate) {
            this.efficiencyRate = efficiencyRate;
        }

        public Double getLettersTrend() {
            return lettersTrend;
        }

        public void setLettersTrend(Double lettersTrend) {
            this.lettersTrend = lettersTrend;
        }

        public Double getConditionalOffersTrend() {
            return conditionalOffersTrend;
        }

        public void setConditionalOffersTrend(Double conditionalOffersTrend) {
            this.conditionalOffersTrend = conditionalOffersTrend;
        }

        public static Double calculateTrend(int current, int previous) {
            if (previous == 0) {
                return current > 0 ? 100.0 : null;
            }
            return ((double) (current - previous) / previous) * 100;
        }
    }
}
