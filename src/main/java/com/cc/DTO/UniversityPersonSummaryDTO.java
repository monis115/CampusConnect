package com.cc.DTO;

/**
 * DTO for UniversityPerson summary (for listing with student counts)
 */
public class UniversityPersonSummaryDTO {
    private Long id;
    private String userName;
    private String collegeName;
    private Integer studentCount;

    public UniversityPersonSummaryDTO() {
    }

    public UniversityPersonSummaryDTO(Long id, String userName, String collegeName, Integer studentCount) {
        this.id = id;
        this.userName = userName;
        this.collegeName = collegeName;
        this.studentCount = studentCount;
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

    public Integer getStudentCount() {
        return studentCount;
    }

    public void setStudentCount(Integer studentCount) {
        this.studentCount = studentCount;
    }
}
