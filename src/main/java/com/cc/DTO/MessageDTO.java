package com.cc.DTO;

import java.time.LocalDateTime;

public class MessageDTO {
    private Long id;
    private String content;
    private LocalDateTime timestamp;
    private String senderRole;
    private boolean isRead;

    // Student Details
    private Long studentId;
    private String studentName;
    private String studentApplicationId;

    // Agent Details
    private Long agentId;
    private String agentName;

    // Manager Details
    private Long managerId;
    private String managerName;

    public MessageDTO() {
    }

    public MessageDTO(Long id, String content, LocalDateTime timestamp, String senderRole, boolean isRead,
            Long studentId, String studentName, String studentApplicationId, Long agentId, String agentName,
            Long managerId, String managerName) {
        this.id = id;
        this.content = content;
        this.timestamp = timestamp;
        this.senderRole = senderRole;
        this.isRead = isRead;
        this.studentId = studentId;
        this.studentName = studentName;
        this.studentApplicationId = studentApplicationId;
        this.agentId = agentId;
        this.agentName = agentName;
        this.managerId = managerId;
        this.managerName = managerName;
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getSenderRole() {
        return senderRole;
    }

    public void setSenderRole(String senderRole) {
        this.senderRole = senderRole;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean isRead) {
        this.isRead = isRead;
    }

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getStudentApplicationId() {
        return studentApplicationId;
    }

    public void setStudentApplicationId(String studentApplicationId) {
        this.studentApplicationId = studentApplicationId;
    }

    public Long getAgentId() {
        return agentId;
    }

    public void setAgentId(Long agentId) {
        this.agentId = agentId;
    }

    public String getAgentName() {
        return agentName;
    }

    public void setAgentName(String agentName) {
        this.agentName = agentName;
    }

    public Long getManagerId() {
        return managerId;
    }

    public void setManagerId(Long managerId) {
        this.managerId = managerId;
    }

    public String getManagerName() {
        return managerName;
    }

    public void setManagerName(String managerName) {
        this.managerName = managerName;
    }
}
