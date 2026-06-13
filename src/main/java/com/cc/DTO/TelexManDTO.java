package com.cc.DTO;

public class TelexManDTO {
    private Long id;
    private String username;
    private String fullName;
    private String email;
    private String contactNumber;
    private String status;
    private String role;
    private Integer studentCount;
    private String profilePhotoPath;

    // Constructors
    public TelexManDTO() {
    }

    public TelexManDTO(Long id, String username, String fullName, String email,
            String contactNumber, String status, String role, Integer studentCount, String profilePhotoPath) {
        this.id = id;
        this.username = username;
        this.fullName = fullName;
        this.email = email;
        this.contactNumber = contactNumber;
        this.status = status;
        this.role = role;
        this.studentCount = studentCount;
        this.profilePhotoPath = profilePhotoPath;
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

    public Integer getStudentCount() {
        return studentCount;
    }

    public void setStudentCount(Integer studentCount) {
        this.studentCount = studentCount;
    }

    public String getProfilePhotoPath() {
        return profilePhotoPath;
    }

    public void setProfilePhotoPath(String profilePhotoPath) {
        this.profilePhotoPath = profilePhotoPath;
    }
}
