package com.cc.DTO;

import java.util.List;

public class AgentCollegeAccessDTO {
    private Long collegeId;
    private List<Long> locationIds;
    private boolean accessAllLocations;

    public Long getCollegeId() {
        return collegeId;
    }

    public void setCollegeId(Long collegeId) {
        this.collegeId = collegeId;
    }

    public List<Long> getLocationIds() {
        return locationIds;
    }

    public void setLocationIds(List<Long> locationIds) {
        this.locationIds = locationIds;
    }

    public boolean isAccessAllLocations() {
        return accessAllLocations;
    }

    public void setAccessAllLocations(boolean accessAllLocations) {
        this.accessAllLocations = accessAllLocations;
    }

    @Override
    public String toString() {
        return "AgentCollegeAccessDTO [collegeId=" + collegeId + ", locationIds=" + locationIds
                + ", accessAllLocations=" + accessAllLocations + "]";
    }
}
