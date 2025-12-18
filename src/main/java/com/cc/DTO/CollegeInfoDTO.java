package com.cc.DTO;

/**
 * DTO for basic college information in the performance dashboard.
 */
public class CollegeInfoDTO {

    private Long id;
    private String collegeName;
    private String city;
    private String country;
    private String admissionManagerName;
    private String universityCoordinatorName;

    public CollegeInfoDTO() {
    }

    public CollegeInfoDTO(Long id, String collegeName, String city, String country,
            String admissionManagerName, String universityCoordinatorName) {
        this.id = id;
        this.collegeName = collegeName;
        this.city = city;
        this.country = country;
        this.admissionManagerName = admissionManagerName;
        this.universityCoordinatorName = universityCoordinatorName;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getAdmissionManagerName() {
        return admissionManagerName;
    }

    public void setAdmissionManagerName(String admissionManagerName) {
        this.admissionManagerName = admissionManagerName;
    }

    public String getUniversityCoordinatorName() {
        return universityCoordinatorName;
    }

    public void setUniversityCoordinatorName(String universityCoordinatorName) {
        this.universityCoordinatorName = universityCoordinatorName;
    }
}
