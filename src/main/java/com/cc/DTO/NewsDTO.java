package com.cc.DTO;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class NewsDTO {

    private Long id;
    private LocalDate activeFrom;
    private LocalDate tillDate;
    private String heading;
    private String content;
    private String tag;
    private String username;
    private String forUser;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private boolean active;

    public NewsDTO() {
    }

    public NewsDTO(Long id, LocalDate activeFrom, LocalDate tillDate, String heading,
            String content, String tag, String username, String forUser,
            LocalDateTime createdAt, LocalDateTime updatedAt, boolean active) {
        this.id = id;
        this.activeFrom = activeFrom;
        this.tillDate = tillDate;
        this.heading = heading;
        this.content = content;
        this.tag = tag;
        this.username = username;
        this.forUser = forUser;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.active = active;
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDate getActiveFrom() {
        return activeFrom;
    }

    public void setActiveFrom(LocalDate activeFrom) {
        this.activeFrom = activeFrom;
    }

    public LocalDate getTillDate() {
        return tillDate;
    }

    public void setTillDate(LocalDate tillDate) {
        this.tillDate = tillDate;
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

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getForUser() {
        return forUser;
    }

    public void setForUser(String forUser) {
        this.forUser = forUser;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
