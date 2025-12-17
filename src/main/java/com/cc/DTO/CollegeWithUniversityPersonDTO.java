package com.cc.DTO;

/**
 * DTO for College with assigned UniversityPerson details
 * Designed for frontend to show college-wise university coordinators
 */
public class CollegeWithUniversityPersonDTO {
    private Long collegeId;
    private String collegeName;
    private String city;
    private String country;
    private boolean hasCoordinator;
    private CoordinatorDTO coordinator;

    public CollegeWithUniversityPersonDTO() {
    }

    public CollegeWithUniversityPersonDTO(Long collegeId, String collegeName, String city, String country,
            boolean hasCoordinator, CoordinatorDTO coordinator) {
        this.collegeId = collegeId;
        this.collegeName = collegeName;
        this.city = city;
        this.country = country;
        this.hasCoordinator = hasCoordinator;
        this.coordinator = coordinator;
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

    public boolean isHasCoordinator() {
        return hasCoordinator;
    }

    public void setHasCoordinator(boolean hasCoordinator) {
        this.hasCoordinator = hasCoordinator;
    }

    public CoordinatorDTO getCoordinator() {
        return coordinator;
    }

    public void setCoordinator(CoordinatorDTO coordinator) {
        this.coordinator = coordinator;
    }

    /**
     * Inner class for UniversityPerson (Coordinator) details
     */
    public static class CoordinatorDTO {
        private Long id;
        private String userName;
        private String status;
        private Integer studentCount;

        public CoordinatorDTO() {
        }

        public CoordinatorDTO(Long id, String userName, String status, Integer studentCount) {
            this.id = id;
            this.userName = userName;
            this.status = status;
            this.studentCount = studentCount;
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

        public Integer getStudentCount() {
            return studentCount;
        }

        public void setStudentCount(Integer studentCount) {
            this.studentCount = studentCount;
        }
    }
}
