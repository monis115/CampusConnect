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

    public Session() {
    }

    public Session(Long id, String sessionName, String academicYear) {
        this.id = id;
        this.sessionName = sessionName;
        this.academicYear = academicYear;
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
}
