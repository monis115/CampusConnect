package com.cc.modals;

import java.time.LocalDateTime;

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
public class StudentStatusHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Link back to the student
    @ManyToOne
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StudentStatus oldStatus;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StudentStatus newStatus;

    @Column(nullable = false)
    private String updatedBy; // could be "Admin", "Agent", "System", "Student" 

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public StudentStatusHistory() {}

    public StudentStatusHistory(Student student, StudentStatus oldStatus, StudentStatus newStatus, String updatedBy) {
        this.student = student;
        this.oldStatus = oldStatus;
        this.newStatus = newStatus;
        this.updatedBy = updatedBy;
        this.updatedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Student getStudent() { return student; }
    public void setStudent(Student student) { this.student = student; }

    public StudentStatus getOldStatus() { return oldStatus; }
    public void setOldStatus(StudentStatus oldStatus) { this.oldStatus = oldStatus; }

    public StudentStatus getNewStatus() { return newStatus; }
    public void setNewStatus(StudentStatus newStatus) { this.newStatus = newStatus; }

    public String getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(String updatedBy) { this.updatedBy = updatedBy; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
