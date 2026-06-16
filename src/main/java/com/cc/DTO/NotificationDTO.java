package com.cc.DTO;

import java.time.LocalDateTime;

public class NotificationDTO {

    private Long id;
    private String heading;
    private String content;
    private String type;
    private String senderUsername;
    private String targetUsername;
    private LocalDateTime createdAt;
    private boolean isRead;

    public NotificationDTO() {
    }

    public NotificationDTO(Long id, String heading, String content, String type, String senderUsername, String targetUsername, LocalDateTime createdAt, boolean isRead) {
        this.id = id;
        this.heading = heading;
        this.content = content;
        this.type = type;
        this.senderUsername = senderUsername;
        this.targetUsername = targetUsername;
        this.createdAt = createdAt;
        this.isRead = isRead;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getHeading() {
        return heading;
    }

    public void setHeading(String heading) {
        this.heading = heading;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSenderUsername() {
        return senderUsername;
    }

    public void setSenderUsername(String senderUsername) {
        this.senderUsername = senderUsername;
    }

    public String getTargetUsername() {
        return targetUsername;
    }

    public void setTargetUsername(String targetUsername) {
        this.targetUsername = targetUsername;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }
}
