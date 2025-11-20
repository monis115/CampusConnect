package com.cc.modals;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;

@Entity
@Table(name = "colleges")
public class College {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String collegeName;

    @Column(nullable = false)
    private String city;

    @Column(nullable = false)
    private String country;

    // FIX 1: Use JsonIgnoreProperties instead of BackReference
    // This says: "Show the Manager, but don't show the Manager's 'colleges' list"
    @ManyToOne
    @JoinColumn(name = "admission_manager_id")
    @JsonIgnoreProperties("colleges")
    private AddmissionManager admissionManager;

    // FIX 2: Add UniversityPerson back (it was missing in your file)
    // This says: "Show the Person, but don't show the Person's 'college' field"
    @OneToOne(mappedBy = "college", fetch = FetchType.LAZY)
    @JsonIgnoreProperties("college")
    private UniversityPerson universityPerson;

    public College() {
    }

    public College(String collegeName, String city, String country) {
        this.collegeName = collegeName;
        this.city = city;
        this.country = country;
    }

    // Getters & Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCollegeName() { return collegeName; }
    public void setCollegeName(String collegeName) { this.collegeName = collegeName; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }

    public AddmissionManager getAdmissionManager() { return admissionManager; }
    public void setAdmissionManager(AddmissionManager admissionManager) { this.admissionManager = admissionManager; }

    public UniversityPerson getUniversityPerson() { return universityPerson; }
    public void setUniversityPerson(UniversityPerson universityPerson) { this.universityPerson = universityPerson; }
}