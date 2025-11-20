package com.cc.modals;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "agent_college_access")
public class AgentCollegeAccess {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "agent_id", nullable = false)
    @JsonBackReference
    private Agent agent;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "college_id", nullable = false)
    @JsonIgnoreProperties({ "admissionManager", "universityPerson" })
    private College college;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "agent_college_access_locations", joinColumns = @JoinColumn(name = "access_id"), inverseJoinColumns = @JoinColumn(name = "location_id"))
    private java.util.Set<Location> allowedLocations;

    @Column(nullable = false)
    private boolean accessAllLocations = false;

    public AgentCollegeAccess() {
    }

    public AgentCollegeAccess(Agent agent, College college, java.util.Set<Location> allowedLocations,
            boolean accessAllLocations) {
        this.agent = agent;
        this.college = college;
        this.allowedLocations = allowedLocations;
        this.accessAllLocations = accessAllLocations;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Agent getAgent() {
        return agent;
    }

    public void setAgent(Agent agent) {
        this.agent = agent;
    }

    public College getCollege() {
        return college;
    }

    public void setCollege(College college) {
        this.college = college;
    }

    public java.util.Set<Location> getAllowedLocations() {
        return allowedLocations;
    }

    public void setAllowedLocations(java.util.Set<Location> allowedLocations) {
        this.allowedLocations = allowedLocations;
    }

    public boolean isAccessAllLocations() {
        return accessAllLocations;
    }

    public void setAccessAllLocations(boolean accessAllLocations) {
        this.accessAllLocations = accessAllLocations;
    }
}
