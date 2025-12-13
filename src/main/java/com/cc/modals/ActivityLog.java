package com.cc.modals;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "activity_logs")
public class ActivityLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // "LOGIN", "CREATE_STUDENT", "UPDATE_AGENT", "DELETE", etc.
    @Column(nullable = false)
    private String action;

    // Username of the actor, or "Anonymous" if unknown
    @Column(nullable = false)
    private String username;

    // Role of the user if known (e.g. "ADMIN", "AGENT")
    private String role;

    // Which entity was affected (e.g. "Student", "Agent")
    private String entityName;

    // Additional details (e.g. "User 'john' logged in successfully", "Deleted
    // student ID 5")
    @Column(length = 2000)
    private String details;

    private String ipAddress;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    public ActivityLog() {
        this.timestamp = LocalDateTime.now();
    }

    public ActivityLog(String action, String username, String role, String entityName, String details,
            String ipAddress) {
        this.action = action;
        this.username = username;
        this.role = role;
        this.entityName = entityName;
        this.details = details;
        this.ipAddress = ipAddress;
        this.timestamp = LocalDateTime.now();
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getEntityName() {
        return entityName;
    }

    public void setEntityName(String entityName) {
        this.entityName = entityName;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
