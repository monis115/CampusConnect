package com.cc.DTO;

import java.util.List;

/**
 * DTO for entity (role) performance metrics.
 * Tracks how each entity type is performing for the college.
 */
public class EntityPerformanceDTO {

    private EntityMetrics admissionManager;
    private EntityMetrics universityCoordinator;
    private List<EntityMetrics> telexMen;
    private List<EntityMetrics> visaMen;
    private EntityMetrics docPerson;
    private EntityMetrics accountant;

    public EntityPerformanceDTO() {
    }

    // Getters and Setters
    public EntityMetrics getAdmissionManager() {
        return admissionManager;
    }

    public void setAdmissionManager(EntityMetrics admissionManager) {
        this.admissionManager = admissionManager;
    }

    public EntityMetrics getUniversityCoordinator() {
        return universityCoordinator;
    }

    public void setUniversityCoordinator(EntityMetrics universityCoordinator) {
        this.universityCoordinator = universityCoordinator;
    }

    public List<EntityMetrics> getTelexMen() {
        return telexMen;
    }

    public void setTelexMen(List<EntityMetrics> telexMen) {
        this.telexMen = telexMen;
    }

    public List<EntityMetrics> getVisaMen() {
        return visaMen;
    }

    public void setVisaMen(List<EntityMetrics> visaMen) {
        this.visaMen = visaMen;
    }

    public EntityMetrics getDocPerson() {
        return docPerson;
    }

    public void setDocPerson(EntityMetrics docPerson) {
        this.docPerson = docPerson;
    }

    public EntityMetrics getAccountant() {
        return accountant;
    }

    public void setAccountant(EntityMetrics accountant) {
        this.accountant = accountant;
    }

    /**
     * Inner class for individual entity metrics.
     */
    public static class EntityMetrics {
        private Long entityId;
        private String entityName;
        private String role;
        private String status;
        private Long studentsHandled;
        private Long studentsInProgress;
        private Long studentsCompleted;
        private Double avgProcessingDays;
        private Double successRate;

        public EntityMetrics() {
        }

        public EntityMetrics(Long entityId, String entityName, String role) {
            this.entityId = entityId;
            this.entityName = entityName;
            this.role = role;
        }

        public Long getEntityId() {
            return entityId;
        }

        public void setEntityId(Long entityId) {
            this.entityId = entityId;
        }

        public String getEntityName() {
            return entityName;
        }

        public void setEntityName(String entityName) {
            this.entityName = entityName;
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

        public Long getStudentsHandled() {
            return studentsHandled;
        }

        public void setStudentsHandled(Long studentsHandled) {
            this.studentsHandled = studentsHandled;
        }

        public Long getStudentsInProgress() {
            return studentsInProgress;
        }

        public void setStudentsInProgress(Long studentsInProgress) {
            this.studentsInProgress = studentsInProgress;
        }

        public Long getStudentsCompleted() {
            return studentsCompleted;
        }

        public void setStudentsCompleted(Long studentsCompleted) {
            this.studentsCompleted = studentsCompleted;
        }

        public Double getAvgProcessingDays() {
            return avgProcessingDays;
        }

        public void setAvgProcessingDays(Double avgProcessingDays) {
            this.avgProcessingDays = avgProcessingDays;
        }

        public Double getSuccessRate() {
            return successRate;
        }

        public void setSuccessRate(Double successRate) {
            this.successRate = successRate;
        }
    }
}
