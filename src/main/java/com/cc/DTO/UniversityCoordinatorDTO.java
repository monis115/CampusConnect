package com.cc.DTO;

public class UniversityCoordinatorDTO {
    private Long id;
    private String userName;
    private String status;
    private String role;
    private String collegeName;

    public UniversityCoordinatorDTO(Long id, String userName, String status, String role, String collegeName) {
        this.id = id;
        this.userName = userName;
        this.status = status;
        this.role = role;
        this.collegeName = collegeName;
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

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getCollegeName() {
        return collegeName;
    }

    public void setCollegeName(String collegeName) {
        this.collegeName = collegeName;
    }
}
