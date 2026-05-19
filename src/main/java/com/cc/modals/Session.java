package com.cc.modals;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;

import jakarta.persistence.*;

@Entity
@Table(name = "academic_sessions")
public class Session {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "session_name", nullable = false)
    private String sessionName;

    @Column(name = "academic_year", nullable = false)
    private String academicYear;

    @Column(name = "status")
    private String status = "ACTIVE"; // ACTIVE or INACTIVE

    public Session() {
    }

    public Session(Long id, String sessionName, String academicYear) {
        this.id = id;
        this.sessionName = sessionName;
        this.academicYear = academicYear;
    }

    public Session(Long id, String sessionName, String academicYear, String status) {
        this.id = id;
        this.sessionName = sessionName;
        this.academicYear = academicYear;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSessionName() {
        return sessionName;
    }

    public void setSessionName(String sessionName) {
        this.sessionName = sessionName;
    }

    public String getAcademicYear() {
        return academicYear;
    }

    public void setAcademicYear(String academicYear) {
        this.academicYear = academicYear;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
