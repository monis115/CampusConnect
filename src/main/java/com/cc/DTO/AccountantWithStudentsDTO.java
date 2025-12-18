package com.cc.DTO;

import java.util.List;

public class AccountantWithStudentsDTO {
    private Long id;
    private String username;
    private String fullName;
    private String email;
    private String contactNumber;
    private String status;
    private int studentCount;
    private List<AccountantStudentDTO> students;

    public AccountantWithStudentsDTO(Long id, String username, String fullName, String email, String contactNumber,
            String status, int studentCount, List<AccountantStudentDTO> students) {
        this.id = id;
        this.username = username;
        this.fullName = fullName;
        this.email = email;
        this.contactNumber = contactNumber;
        this.status = status;
        this.studentCount = studentCount;
        this.students = students;
    }

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

    public int getStudentCount() {
        return studentCount;
    }

    public void setStudentCount(int studentCount) {
        this.studentCount = studentCount;
    }

    public List<AccountantStudentDTO> getStudents() {
        return students;
    }

    public void setStudents(List<AccountantStudentDTO> students) {
        this.students = students;
    }
}
