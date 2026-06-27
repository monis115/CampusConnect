package com.cc.modals;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "task_rules")
public class TaskRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String ruleCode; // e.g., "ADMISSION_LETTER_MISSING"

    @Column(nullable = false)
    private String taskTitle; // e.g., "Upload Admission Letter"

    @Column(columnDefinition = "TEXT")
    private String description; // Template for task description

    @Column(nullable = false)
    private String conditionType; // FIELD_NULL, FIELD_EQUALS, DATE_OVERDUE, STATUS_EQUALS

    @Column(nullable = false)
    private String conditionField; // e.g., "admissionLetterPath", "paymentStatus"

    private String conditionValue; // e.g., "Pending" (for FIELD_EQUALS)

    private Integer conditionDaysThreshold; // e.g., 7 (for DATE_OVERDUE)

    @Column(nullable = false)
    private String ownerRole; // e.g., "UNIVERSITY_COORDINATOR"

    @Column(nullable = false)
    private String department; // e.g., "UNIVERSITY"

    private String category; // e.g., "DOCUMENT", "PAYMENT", "FOLLOW_UP", "TRAVEL"

    private String priority; // "HIGH", "MEDIUM", "LOW"

    private Integer creationDelayMinutes = 5; // Configurable delay before task is spawned

    private Integer dueDelayMinutes; // Configurable SLA in minutes (replaces slaDays)
    private String watcherRoles; // Comma-separated: "ADMISSION_MANAGER,AGENT"

    @Column(columnDefinition = "TEXT")
    private String escalationChain; // JSON: ["ADMISSION_MANAGER","OPERATIONS","ADMIN"]

    private String autoCloseConditionField; // Same field — when populated, close task

    private String autoCloseConditionValue; // For equality checks on auto-close

    private String prerequisiteRuleCode; // Dependency — don't create until prereq is COMPLETED

    @Column(nullable = false)
    private Boolean enabled = true;

    @Column(columnDefinition = "boolean default true")
    private Boolean requiresApproval = true;

    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public TaskRule() {
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRuleCode() {
        return ruleCode;
    }

    public void setRuleCode(String ruleCode) {
        this.ruleCode = ruleCode;
    }

    public String getTaskTitle() {
        return taskTitle;
    }

    public void setTaskTitle(String taskTitle) {
        this.taskTitle = taskTitle;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getConditionType() {
        return conditionType;
    }

    public void setConditionType(String conditionType) {
        this.conditionType = conditionType;
    }

    public String getConditionField() {
        return conditionField;
    }

    public void setConditionField(String conditionField) {
        this.conditionField = conditionField;
    }

    public String getConditionValue() {
        return conditionValue;
    }

    public void setConditionValue(String conditionValue) {
        this.conditionValue = conditionValue;
    }

    public Integer getConditionDaysThreshold() {
        return conditionDaysThreshold;
    }

    public void setConditionDaysThreshold(Integer conditionDaysThreshold) {
        this.conditionDaysThreshold = conditionDaysThreshold;
    }

    public String getOwnerRole() {
        return ownerRole;
    }

    public void setOwnerRole(String ownerRole) {
        this.ownerRole = ownerRole;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public Integer getCreationDelayMinutes() {
        return creationDelayMinutes;
    }

    public void setCreationDelayMinutes(Integer creationDelayMinutes) {
        this.creationDelayMinutes = creationDelayMinutes;
    }

    public Integer getDueDelayMinutes() {
        return dueDelayMinutes;
    }

    public void setDueDelayMinutes(Integer dueDelayMinutes) {
        this.dueDelayMinutes = dueDelayMinutes;
    }

    public String getWatcherRoles() {
        return watcherRoles;
    }

    public void setWatcherRoles(String watcherRoles) {
        this.watcherRoles = watcherRoles;
    }

    public String getEscalationChain() {
        return escalationChain;
    }

    public void setEscalationChain(String escalationChain) {
        this.escalationChain = escalationChain;
    }

    public String getAutoCloseConditionField() {
        return autoCloseConditionField;
    }

    public void setAutoCloseConditionField(String autoCloseConditionField) {
        this.autoCloseConditionField = autoCloseConditionField;
    }

    public String getAutoCloseConditionValue() {
        return autoCloseConditionValue;
    }

    public void setAutoCloseConditionValue(String autoCloseConditionValue) {
        this.autoCloseConditionValue = autoCloseConditionValue;
    }

    public String getPrerequisiteRuleCode() {
        return prerequisiteRuleCode;
    }

    public void setPrerequisiteRuleCode(String prerequisiteRuleCode) {
        this.prerequisiteRuleCode = prerequisiteRuleCode;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Boolean getRequiresApproval() {
        return requiresApproval;
    }

    public void setRequiresApproval(Boolean requiresApproval) {
        this.requiresApproval = requiresApproval;
    }
}
