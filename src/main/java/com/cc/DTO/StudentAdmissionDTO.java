package com.cc.DTO;

import com.cc.modals.StudentStatus;

public class StudentAdmissionDTO {

    private String applicationId;
    private String studentName;
    private String agentName;
    private String collegeName;
    private StudentStatus applicationStatus;
    private String course;
    private String admissionLetterPath;
    private String agentUserId;

    public StudentAdmissionDTO(String applicationId, String studentName, String agentName, String agentUserId,
            String collegeName,
            StudentStatus applicationStatus, String course, String admissionLetterPath) {
        this.applicationId = applicationId;
        this.studentName = studentName;
        this.agentName = agentName;
        this.agentUserId = agentUserId;
        this.collegeName = collegeName;
        this.applicationStatus = applicationStatus;
        this.course = course;
        this.admissionLetterPath = admissionLetterPath;
    }

    public String getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getAgentName() {
        return agentName;
    }

    public void setAgentName(String agentName) {
        this.agentName = agentName;
    }

    public String getAgentUserId() {
        return agentUserId;
    }

    public void setAgentUserId(String agentUserId) {
        this.agentUserId = agentUserId;
    }

    public String getCollegeName() {
        return collegeName;
    }

    public void setCollegeName(String collegeName) {
        this.collegeName = collegeName;
    }

    public StudentStatus getApplicationStatus() {
        return applicationStatus;
    }

    public void setApplicationStatus(StudentStatus applicationStatus) {
        this.applicationStatus = applicationStatus;
    }

    public String getCourse() {
        return course;
    }

    public void setCourse(String course) {
        this.course = course;
    }

    public String getAdmissionLetterPath() {
        return admissionLetterPath;
    }

    public void setAdmissionLetterPath(String admissionLetterPath) {
        this.admissionLetterPath = admissionLetterPath;
    }
}
