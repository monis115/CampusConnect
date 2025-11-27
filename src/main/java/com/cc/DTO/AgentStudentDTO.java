package com.cc.DTO;

public class AgentStudentDTO {
    private Long id;
    private String name;
    private String applicationId;

    public AgentStudentDTO(Long id, String name, String applicationId) {
        this.id = id;
        this.name = name;
        this.applicationId = applicationId;
    }

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

    public String getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }
}
