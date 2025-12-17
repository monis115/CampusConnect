package com.cc.DTO;

/**
 * DTO for UniversityPerson full details (without students list)
 * Used for management card - update username, status, college assignment
 */
public class UniversityPersonDTO {
    private Long id;
    private String userName;
    private String status;
    private String role;
    private Long collegeId;
    private String collegeName;
    private Integer studentCount;

    public UniversityPersonDTO() {
    }

    public UniversityPersonDTO(Long id, String userName, String status, String role,
            Long collegeId, String collegeName, Integer studentCount) {
        this.id = id;
        this.userName = userName;
        this.status = status;
        this.role = role;
        this.collegeId = collegeId;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

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

    public Integer getStudentCount() {
        return studentCount;
    }

    public void setStudentCount(Integer studentCount) {
        this.studentCount = studentCount;
    }
}
