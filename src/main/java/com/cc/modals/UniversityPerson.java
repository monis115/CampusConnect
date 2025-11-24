package com.cc.modals;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.*;

@Entity
@Table(name = "university_person")
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
public class UniversityPerson {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String userName;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String status;

    @Column(nullable = false)
    private String role;

    // owner side of one-to-one (each UniversityPerson manages exactly one College)
    // REMOVED @JsonManagedReference here to fix the serialization issue
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "college_id", unique = true)
    @JsonIgnoreProperties({ "admissionManager", "universityPerson" })
    private College college;

    // A UniversityPerson owns multiple Students
    // REMOVED @JsonManagedReference here as well
    @OneToMany(mappedBy = "universityPerson", fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    @JsonIgnoreProperties({ "universityPerson", "admissionManager", "college" })
    private List<Student> students;

    public UniversityPerson() {
    }

    public UniversityPerson(Long id, String userName, String password, String status, String role, College college) {
        this.id = id;
        this.userName = userName;
        this.password = password;
        this.status = status;
        this.role = role;
        this.college = college;
    }

    // getters & setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public College getCollege() {
        return college;
    }

    public void setCollege(College college) {
        this.college = college;
    }

    public List<Student> getStudents() {
        return students;
    }

    public void setStudents(List<Student> students) {
        this.students = students;
    }

    public String getFullName() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getFullName'");
    }
}