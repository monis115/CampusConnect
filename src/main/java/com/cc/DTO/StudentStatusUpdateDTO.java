package com.cc.DTO;

import com.cc.modals.StudentStatus;

/**
 * DTO for carrying student status updates.
 */
public class StudentStatusUpdateDTO {


    private StudentStatus presentStatus;
    private StudentStatus nextStep; // Optional, as logic can auto-set it
    private String updatedByRole; // "Agent" or "AdmissionManager"
    private String updatedByUsername; // --- CHANGED FROM Long updatedById ---

    // Getters and Setters
    public StudentStatus getPresentStatus() {
        return presentStatus;
    }

    public void setPresentStatus(StudentStatus presentStatus) {
        this.presentStatus = presentStatus;
    }

    public StudentStatus getNextStep() {
        return nextStep;
    }

    public void setNextStep(StudentStatus nextStep) {
        this.nextStep = nextStep;
    }

    public String getUpdatedByRole() {
        return updatedByRole;
    }

    public void setUpdatedByRole(String updatedByRole) {
        this.updatedByRole = updatedByRole;
    }

    // --- UPDATED GETTER/SETTER ---
    public String getUpdatedByUsername() {
        return updatedByUsername;
    }

    public void setUpdatedByUsername(String updatedByUsername) {
        this.updatedByUsername = updatedByUsername;
    }
}