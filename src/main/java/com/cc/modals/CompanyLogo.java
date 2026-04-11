package com.cc.modals;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;

@Entity
@Table(name = "company_logo")
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })

public class CompanyLogo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name; // e.g., "RARE Education Logo"

    @Lob
    @Column(name = "logo_data", nullable = false, columnDefinition = "LONGBLOB") // <-- ADD columnDefinition for MySQL
                                                                                 // LONGBLOB (up to 4GB)
    private byte[] logoData;
    @Column(name = "content_type", nullable = false)
    private String contentType; // e.g., "image/png"

    // Constructors
    public CompanyLogo() {
    }

    public CompanyLogo(String name, byte[] logoData, String contentType) {
        this.name = name;
        this.logoData = logoData;
        this.contentType = contentType;
    }

    // Getters & Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public byte[] getLogoData() {
        return logoData;
    }

    public void setLogoData(byte[] logoData) {
        this.logoData = logoData;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }
}