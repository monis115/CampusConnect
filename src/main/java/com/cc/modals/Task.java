package com.cc.modals;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@Table(name = "tasks")
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String type; // "TASK"
    private String department; // "ADMISSION", "VISA", "TELEX", "DOCUMENT", "ACCOUNTANT", "AGENT", "STUDENT",
                               // "UNIVERSITY"

    private String assignedToRole;

    private String createdBy; // "SYSTEM" or username/id of creator

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "student_id")
    @JsonIgnoreProperties({ "tasks", "collegePreferences", "college", "admissionManager", "telexMan", "visaMan",
            "docPerson", "accountant", "universityPerson", "agent" })
    private Student student;

    private String priority; // e.g. "HIGH", "MEDIUM", "LOW"
    private String sla; // e.g. "48 hours"
    private String escalatedTo; // Username or Role

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "task_watchers", joinColumns = @JoinColumn(name = "task_id"))
    @Column(name = "username")
    private Set<String> watchers = new HashSet<>();

    private LocalDate dueDate;
    private String status; // PENDING, IN_PROGRESS, COMPLETED_BY_TEAM, AWAITING_APPROVAL, CLOSED, REWORK_REQUIRED, VERIFIED, ARCHIVED

    private String category;

    @Column(name = "system_generated", columnDefinition = "boolean default false")
    private Boolean systemGenerated = false;

    private LocalDateTime closedAt;

    // Dynamic Owner Resolution — the ACTUAL username who owns this task
    private String assignedToUser;

    // Verification-based closure
    private String verifiedBy;
    private LocalDateTime verifiedAt;

    // Task Dependencies
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_task_id")
    @JsonIgnoreProperties({"childTasks", "followUps", "watchers", "student", "parentTask"})
    private Task parentTask;

    @OneToMany(mappedBy = "parentTask", fetch = FetchType.LAZY)
    @JsonIgnoreProperties({"parentTask", "followUps", "watchers", "student", "childTasks"})
    private Set<Task> childTasks = new HashSet<>();

    private String blockedByTaskIds; // Comma-separated IDs

    // Reminder tracking
    @Column(columnDefinition = "integer default 0")
    private Integer reminderCount = 0;
    private LocalDateTime lastReminderAt;

    // Template origin
    private String templateName;

    // Approval fields
    private String approvalDecision; // APPROVED, REJECTED, RETURNED
    @Column(columnDefinition = "TEXT")
    private String approvalComment;

    // Completed-by-team tracking
    private String completedBy;
    private LocalDateTime completedAt;

    // Configurable approval gate
    @Column(columnDefinition = "boolean default true")
    private Boolean requiresApproval = true;

    // Rework tracking
    @Column(columnDefinition = "integer default 0")
    private Integer reworkCount = 0;

    // Clarification from AM
    @Column(columnDefinition = "TEXT")
    private String clarificationMessage;

    // Rule origin
    private String ruleCode; // Which TaskRule generated this

    @OneToMany(mappedBy = "task", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<FollowUp> followUps = new HashSet<>();

    private LocalDateTime scheduledFor;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Task() {
    }

    public LocalDateTime getScheduledFor() {
        return scheduledFor;
    }

    public void setScheduledFor(LocalDateTime scheduledFor) {
        this.scheduledFor = scheduledFor;
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null)
            status = "PENDING";
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getAssignedToRole() {
        return assignedToRole;
    }

    public void setAssignedToRole(String assignedToRole) {
        this.assignedToRole = assignedToRole;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public Student getStudent() {
        return student;
    }

    public void setStudent(Student student) {
        this.student = student;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getSla() {
        return sla;
    }

    public void setSla(String sla) {
        this.sla = sla;
    }

    public String getEscalatedTo() {
        return escalatedTo;
    }

    public void setEscalatedTo(String escalatedTo) {
        this.escalatedTo = escalatedTo;
    }

    public Set<String> getWatchers() {
        return watchers;
    }

    public void setWatchers(Set<String> watchers) {
        this.watchers = watchers;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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

    public Set<FollowUp> getFollowUps() {
        return followUps;
    }

    public void setFollowUps(Set<FollowUp> followUps) {
        this.followUps = followUps;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Boolean getSystemGenerated() {
        return systemGenerated;
    }

    public void setSystemGenerated(Boolean systemGenerated) {
        this.systemGenerated = systemGenerated;
    }

    public LocalDateTime getClosedAt() {
        return closedAt;
    }

    public void setClosedAt(LocalDateTime closedAt) {
        this.closedAt = closedAt;
    }

    public String getAssignedToUser() {
        return assignedToUser;
    }

    public void setAssignedToUser(String assignedToUser) {
        this.assignedToUser = assignedToUser;
    }

    public String getVerifiedBy() {
        return verifiedBy;
    }

    public void setVerifiedBy(String verifiedBy) {
        this.verifiedBy = verifiedBy;
    }

    public LocalDateTime getVerifiedAt() {
        return verifiedAt;
    }

    public void setVerifiedAt(LocalDateTime verifiedAt) {
        this.verifiedAt = verifiedAt;
    }

    public Task getParentTask() {
        return parentTask;
    }

    public void setParentTask(Task parentTask) {
        this.parentTask = parentTask;
    }

    public Set<Task> getChildTasks() {
        return childTasks;
    }

    public void setChildTasks(Set<Task> childTasks) {
        this.childTasks = childTasks;
    }

    public String getBlockedByTaskIds() {
        return blockedByTaskIds;
    }

    public void setBlockedByTaskIds(String blockedByTaskIds) {
        this.blockedByTaskIds = blockedByTaskIds;
    }

    public Integer getReminderCount() {
        return reminderCount;
    }

    public void setReminderCount(Integer reminderCount) {
        this.reminderCount = reminderCount;
    }

    public LocalDateTime getLastReminderAt() {
        return lastReminderAt;
    }

    public void setLastReminderAt(LocalDateTime lastReminderAt) {
        this.lastReminderAt = lastReminderAt;
    }

    public String getTemplateName() {
        return templateName;
    }

    public void setTemplateName(String templateName) {
        this.templateName = templateName;
    }

    public String getApprovalDecision() {
        return approvalDecision;
    }

    public void setApprovalDecision(String approvalDecision) {
        this.approvalDecision = approvalDecision;
    }

    public String getApprovalComment() {
        return approvalComment;
    }

    public void setApprovalComment(String approvalComment) {
        this.approvalComment = approvalComment;
    }

    public String getRuleCode() {
        return ruleCode;
    }

    public void setRuleCode(String ruleCode) {
        this.ruleCode = ruleCode;
    }

    public String getCompletedBy() {
        return completedBy;
    }

    public void setCompletedBy(String completedBy) {
        this.completedBy = completedBy;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }

    public Boolean getRequiresApproval() {
        return requiresApproval;
    }

    public void setRequiresApproval(Boolean requiresApproval) {
        this.requiresApproval = requiresApproval;
    }

    public Integer getReworkCount() {
        return reworkCount;
    }

    public void setReworkCount(Integer reworkCount) {
        this.reworkCount = reworkCount;
    }

    public String getClarificationMessage() {
        return clarificationMessage;
    }

    public void setClarificationMessage(String clarificationMessage) {
        this.clarificationMessage = clarificationMessage;
    }
}
