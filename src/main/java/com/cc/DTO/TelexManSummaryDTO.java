package com.cc.DTO;

/**
 * DTO for TelexMan summary (without students list, just count)
 * Used for listing all TelexMans in management cards
 */
public class TelexManSummaryDTO {
    private Long id;
    private String username;
    private String fullName;
    private Integer studentCount;

    public TelexManSummaryDTO() {
    }

    public TelexManSummaryDTO(Long id, String username, String fullName, Integer studentCount) {
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
