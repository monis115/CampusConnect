package com.cc.modals;

import jakarta.persistence.*;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@Table(name = "task_follow_ups")
public class FollowUp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", nullable = false)
    @JsonIgnoreProperties({"followUps", "watchers", "student", "hibernateLazyInitializer", "handler"})
    private Task task;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String message;

    private String createdBy; // username

    private String communicationType; // PHONE_CALL, EMAIL, MEETING, WHATSAPP, INTERNAL_NOTE, EXTERNAL_NOTE, SYSTEM

    @Column(name = "attachment_path", columnDefinition = "TEXT")
    private String attachmentPath; // S3 path for PDF/Screenshot/Voice Note

    private String mentionedUsers; // Comma-separated usernames

    @Column(name = "is_internal", columnDefinition = "boolean default true")
    private Boolean isInternal = true;

    private LocalDateTime createdAt;

    public FollowUp() {}

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Task getTask() { return task; }
    public void setTask(Task task) { this.task = task; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public String getCommunicationType() { return communicationType; }
    public void setCommunicationType(String communicationType) { this.communicationType = communicationType; }
    public String getAttachmentPath() { return attachmentPath; }
    public void setAttachmentPath(String attachmentPath) { this.attachmentPath = attachmentPath; }
    public String getMentionedUsers() { return mentionedUsers; }
    public void setMentionedUsers(String mentionedUsers) { this.mentionedUsers = mentionedUsers; }
    public Boolean getIsInternal() { return isInternal; }
    public void setIsInternal(Boolean isInternal) { this.isInternal = isInternal; }
}
