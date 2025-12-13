package com.cc.DTO;

public class AdmissionManagerSummaryDTO {
    private Long id;
    private String fullName;
    private String email;
    private String contactNumber;
    private String role;
    private String status;
    private String username;
    private java.util.List<String> assignedColleges;
    private Long studentCount;

    public AdmissionManagerSummaryDTO(Long id, String fullName, String email, String contactNumber, String role,
            String status, String username, java.util.List<String> assignedColleges, Long studentCount) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.contactNumber = contactNumber;
        this.role = role;
        this.status = status;
        this.username = username;
        this.assignedColleges = assignedColleges;
        this.studentCount = studentCount;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public java.util.List<String> getAssignedColleges() {
        return assignedColleges;
    }

    public void setAssignedColleges(java.util.List<String> assignedColleges) {
        this.assignedColleges = assignedColleges;
    }

    public Long getStudentCount() {
        return studentCount;
    }

    public void setStudentCount(Long studentCount) {
        this.studentCount = studentCount;
    }
}
