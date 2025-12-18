package com.cc.DTO;

import java.time.LocalDateTime;

public class AccountantStudentDTO {
    private String applicationId;
    private String fullName;
    private String collegeName;
    private String course;
    private String status;
    private LocalDateTime lastUpdateDate;
    private String lastUpdateAction;

    public AccountantStudentDTO(String applicationId, String fullName, String collegeName, String course, String status,
            LocalDateTime lastUpdateDate, String lastUpdateAction) {
        this.applicationId = applicationId;
        this.fullName = fullName;
        this.collegeName = collegeName;
        this.course = course;
        this.status = status;
        this.lastUpdateDate = lastUpdateDate;
        this.lastUpdateAction = lastUpdateAction;
    }

    public String getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getCollegeName() {
        return collegeName;
    }

    public void setCollegeName(String collegeName) {
        this.collegeName = collegeName;
    }

    public String getCourse() {
        return course;
    }

    public void setCourse(String course) {
        this.course = course;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getLastUpdateDate() {
        return lastUpdateDate;
    }

    public void setLastUpdateDate(LocalDateTime lastUpdateDate) {
        this.lastUpdateDate = lastUpdateDate;
    }

    public String getLastUpdateAction() {
        return lastUpdateAction;
    }

    public void setLastUpdateAction(String lastUpdateAction) {
        this.lastUpdateAction = lastUpdateAction;
    }
}
