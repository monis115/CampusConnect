package com.cc.modals;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "message")
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 1000)
    private String content;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @ManyToOne
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne
    @JoinColumn(name = "agent_id", nullable = false)
    private Agent agent;

    @ManyToOne
    @JoinColumn(name = "admission_manager_id", nullable = false)
    private AddmissionManager admissionManager;

    @Column(nullable = false)
    private String senderRole; // "AGENT" or "MANAGER"

    @Column(nullable = false)
    private boolean isRead = false;

    public Message() {
        this.timestamp = LocalDateTime.now();
    }

    public Message(String content, Student student, Agent agent, AddmissionManager admissionManager,
            String senderRole) {
        this.content = content;
        this.student = student;
        this.agent = agent;
        this.admissionManager = admissionManager;
        this.senderRole = senderRole;
        this.timestamp = LocalDateTime.now();
        this.isRead = false;
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

    public Student getStudent() {
        return student;
    }

    public void setStudent(Student student) {
        this.student = student;
    }

    public Agent getAgent() {
        return agent;
    }

    public void setAgent(Agent agent) {
        this.agent = agent;
    }

    public AddmissionManager getAdmissionManager() {
        return admissionManager;
    }

    public void setAdmissionManager(AddmissionManager admissionManager) {
        this.admissionManager = admissionManager;
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
}
