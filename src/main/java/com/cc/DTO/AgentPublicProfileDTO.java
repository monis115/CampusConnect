package com.cc.DTO;

import java.util.List;

public class AgentPublicProfileDTO {
    private String fullName;
    private String companyName;
    private List<AgentCollegeAccessDTO> collegeAccessList;

    public AgentPublicProfileDTO() {
    }

    public AgentPublicProfileDTO(String fullName, String companyName, List<AgentCollegeAccessDTO> collegeAccessList) {
        this.fullName = fullName;
        this.companyName = companyName;
        this.collegeAccessList = collegeAccessList;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public List<AgentCollegeAccessDTO> getCollegeAccessList() {
        return collegeAccessList;
    }

    public void setCollegeAccessList(List<AgentCollegeAccessDTO> collegeAccessList) {
        this.collegeAccessList = collegeAccessList;
    }
}
