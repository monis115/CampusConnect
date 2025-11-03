package com.cc.modals;

import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

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
	
	 @ManyToOne
	    @JoinColumn(name = "admission_manager_id")
	    @JsonBackReference(value = "manager-college")
	    private AddmissionManager admissionManager;


	public College() {
	}

	public College(String collegeName, String city, String country) {
		this.collegeName = collegeName;

		this.city = city;
		this.country = country;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getCollegeName() {
		return collegeName;
	}

	public void setCollegeName(String collegeName) {
		this.collegeName = collegeName;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public AddmissionManager getAdmissionManager() {
		return admissionManager;
	}

	public void setAdmissionManager(AddmissionManager admissionManager) {
		this.admissionManager = admissionManager;
	}
	
	
}
