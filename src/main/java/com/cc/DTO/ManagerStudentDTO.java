package com.cc.DTO;

import com.cc.modals.StudentStatus;

public class ManagerStudentDTO {
    private Long id;
    private String applicationId;
    private String fullName;
    private String collegeName;
    private String course; // appliedFor
    private StudentStatus applicationStatus;
    private String actionOnUser;
    private String action;

    public ManagerStudentDTO(Long id, String applicationId, String fullName, String collegeName, String course,
            StudentStatus applicationStatus, String actionOnUser, String action) {
        this.id = id;
        this.applicationId = applicationId;
        this.fullName = fullName;
        this.collegeName = collegeName;
        this.course = course;
        this.applicationStatus = applicationStatus;
        this.actionOnUser = actionOnUser;
        this.action = action;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public StudentStatus getApplicationStatus() {
        return applicationStatus;
    }

    public void setApplicationStatus(StudentStatus applicationStatus) {
        this.applicationStatus = applicationStatus;
    }

    public String getActionOnUser() {
        return actionOnUser;
    }

    public void setActionOnUser(String actionOnUser) {
        this.actionOnUser = actionOnUser;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }
}
