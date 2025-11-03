package com.cc.modals;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "admission_manager")
public class AddmissionManager {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String fullName;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String contactNumber;

    @Column(nullable = false)
    private String role = "AdmissionManager";

    @Column(nullable = false)
    private String status = "Active";

    @Column(nullable = false, unique = true)
    private String username;

    // store hashed password
    @Column(nullable = false)
    private String password;

    // One manager can manage multiple colleges
    // --- THIS IS THE FIX ---
    // Removed "orphanRemoval = true" because a College is a shared entity
    // and should not be deleted when un-assigned from a manager.
    // We keep CascadeType.ALL to save/update colleges when a manager is saved,
    // but we will manually handle unlinking in the service.
    @OneToMany(mappedBy = "admissionManager", cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH, CascadeType.DETACH})
    @JsonManagedReference(value = "manager-college")
    private List<College> colleges = new ArrayList<>();

    // One manager can issue admission letters to multiple students
    // --- NOTE: You might also want to remove orphanRemoval here if Students are shared ---
    @OneToMany(mappedBy = "admissionManager", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference(value = "manager-student")
    private List<Student> students = new ArrayList<>();

    public AddmissionManager() {}

    public AddmissionManager(String fullName, String email, String contactNumber) {
        this.fullName = fullName;
        this.email = email;
        this.contactNumber = contactNumber;
        this.role = "AdmissionManager";
        this.status = "Active";
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<College> getColleges() {
        return colleges;
    }

    public void setColleges(List<College> colleges) {
        this.colleges = colleges;
    }

    public List<Student> getStudents() {
        return students;
    }

    public void setStudents(List<Student> students) {
        this.students = students;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    // hashed password stored
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    // Relationship helpers
    // (You can add helpers here if needed, e.g., addCollege, removeCollege)
}

