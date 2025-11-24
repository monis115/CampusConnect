package com.cc.DTO;

import java.time.LocalDateTime;

public class AdmittedStudentDTO {
    private Long id;
    private String fullName;
    private String passportNumber;
    private String courseName;
    private String status;
    private LocalDateTime admissionDate;

    public AdmittedStudentDTO(Long id, String fullName, String passportNumber, String courseName, String status,
            LocalDateTime admissionDate) {
        this.id = id;
        this.fullName = fullName;
        this.passportNumber = passportNumber;
        this.courseName = courseName;
        this.status = status;
        this.admissionDate = admissionDate;
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

    public String getPassportNumber() {
        return passportNumber;
    }

    public void setPassportNumber(String passportNumber) {
        this.passportNumber = passportNumber;
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getAdmissionDate() {
        return admissionDate;
    }

    public void setAdmissionDate(LocalDateTime admissionDate) {
        this.admissionDate = admissionDate;
    }
}
