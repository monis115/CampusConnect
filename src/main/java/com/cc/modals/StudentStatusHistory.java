package com.cc.modals;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "student_status_history")

@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })

public class StudentStatusHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Inside StudentStatusHistory.java

    @ManyToOne
    @JoinColumn(name = "student_id")
    // 👇 THIS IS THE MISSING LINE 👇
    @JsonBackReference(value = "student-history")
    private Student student;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private StudentStatus studentApplicationStatus;

    public StudentStatus getStudentApplicationStatus() {
        return studentApplicationStatus;
    }

    public void setStudentApplicationStatus(StudentStatus studentApplicationStatus) {
        this.studentApplicationStatus = studentApplicationStatus;
    }

    @Column(nullable = false)
    private String updatedBy; // could be "Admin", "Agent", "System", "Student"

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public StudentStatusHistory() {
    }

    @Column(name = "remarks", length = 500, nullable = true)
    private String remarks;

    // Add getter and setter
    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public StudentStatusHistory(Student student, StudentStatus newStatus, String updatedBy) {
        this.student = student;
        this.studentApplicationStatus = newStatus;
        this.updatedBy = updatedBy;
        this.updatedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Student getStudent() {
        return student;
    }

    public void setStudent(Student student) {
        this.student = student;
    }

    public StudentStatus getNewStatus() {
        return studentApplicationStatus;
    }

    public void setNewStatus(StudentStatus newStatus) {
        this.studentApplicationStatus = newStatus;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
