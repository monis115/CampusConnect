package com.cc.DTO;

import java.util.List;

/**
 * DTO for student pipeline/funnel visualization.
 * Shows student count at each stage of the journey.
 */
public class StudentPipelineDTO {

    private List<PipelineStage> stages;
    private Long totalStudents;
    private Long rejectedCount;

    public StudentPipelineDTO() {
    }

    public StudentPipelineDTO(List<PipelineStage> stages, Long totalStudents, Long rejectedCount) {
        this.stages = stages;
        this.totalStudents = totalStudents;
        this.rejectedCount = rejectedCount;
    }

    // Getters and Setters
    public List<PipelineStage> getStages() {
        return stages;
    }

    public void setStages(List<PipelineStage> stages) {
        this.stages = stages;
    }

    public Long getTotalStudents() {
        return totalStudents;
    }

    public void setTotalStudents(Long totalStudents) {
        this.totalStudents = totalStudents;
    }

    public Long getRejectedCount() {
        return rejectedCount;
    }

    public void setRejectedCount(Long rejectedCount) {
        this.rejectedCount = rejectedCount;
    }

    /**
     * Inner class for individual pipeline stages.
     */
    public static class PipelineStage {
        private String stageName;
        private String stageKey;
        private Long count;
        private Double percentage; // Percentage of total
        private Double dropOffRate; // Drop-off from previous stage

        public PipelineStage() {
        }

        public PipelineStage(String stageName, String stageKey, Long count, Double percentage, Double dropOffRate) {
            this.stageName = stageName;
            this.stageKey = stageKey;
            this.count = count;
            this.percentage = percentage;
            this.dropOffRate = dropOffRate;
        }

        public String getStageName() {
            return stageName;
        }

        public void setStageName(String stageName) {
            this.stageName = stageName;
        }

        public String getStageKey() {
            return stageKey;
        }

        public void setStageKey(String stageKey) {
            this.stageKey = stageKey;
        }

        public Long getCount() {
            return count;
        }

        public void setCount(Long count) {
            this.count = count;
        }

        public Double getPercentage() {
            return percentage;
        }

        public void setPercentage(Double percentage) {
            this.percentage = percentage;
        }

        public Double getDropOffRate() {
            return dropOffRate;
        }

        public void setDropOffRate(Double dropOffRate) {
            this.dropOffRate = dropOffRate;
        }
    }
}
