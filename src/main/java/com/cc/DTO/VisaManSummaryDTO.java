package com.cc.DTO;

/**
 * DTO for VisaMan summary (id, username, fullName, studentCount)
 * Used for listing all VisaMans in management cards
 */
public class VisaManSummaryDTO {
    private Long id;
    private String username;
    private String fullName;
    private Integer studentCount;

    public VisaManSummaryDTO() {
    }

    public VisaManSummaryDTO(Long id, String username, String fullName, Integer studentCount) {
        this.id = id;
        this.username = username;
        this.fullName = fullName;
        this.studentCount = studentCount;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public Integer getStudentCount() {
        return studentCount;
    }

    public void setStudentCount(Integer studentCount) {
        this.studentCount = studentCount;
    }
}
