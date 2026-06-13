package com.cc.modals;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@Table(name = "telex_man")
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
public class TelexMan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String fullName;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String contactNumber;

    @Column(nullable = false)
    private String status = "Active";

    @Column(nullable = false)
    private String role = "TelexManager";

    @Column(name = "profile_photo_path")
    private String profilePhotoPath;

    @OneToMany(mappedBy = "telexMan", cascade = CascadeType.ALL)
    @JsonIgnoreProperties("telexMan")
    private java.util.List<Student> students = new java.util.ArrayList<>();

    public TelexMan() {
    }

    public TelexMan(String username, String password, String fullName, String email, String contactNumber) {
        this.username = username;
        this.password = password;
        this.fullName = fullName;
        this.email = email;
        this.contactNumber = contactNumber;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
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

    public String getProfilePhotoPath() {
        return profilePhotoPath;
    }

    public void setProfilePhotoPath(String profilePhotoPath) {
        this.profilePhotoPath = profilePhotoPath;
    }

    public java.util.List<Student> getStudents() {
        return students;
    }

    public void setStudents(java.util.List<Student> students) {
        this.students = students;
    }
}
