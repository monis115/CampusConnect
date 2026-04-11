package com.cc.DTO;

import java.util.List;

public class AgentCollegeAccessDTO {
    private Long collegeId;
    private List<Long> locationIds;
    private boolean accessAllLocations;

    private Double dealPrice;
    @com.fasterxml.jackson.annotation.JsonProperty("isSlabApplicable")
    private boolean isSlabApplicable;
    private List<CommissionSlabDTO> commissionSlabs;

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

    public Double getDealPrice() {
        return dealPrice;
    }

    public void setDealPrice(Double dealPrice) {
        this.dealPrice = dealPrice;
    }

    @com.fasterxml.jackson.annotation.JsonProperty("isSlabApplicable")
    public boolean isSlabApplicable() {
        return isSlabApplicable;
    }

    @com.fasterxml.jackson.annotation.JsonProperty("isSlabApplicable")
    public void setSlabApplicable(boolean isSlabApplicable) {
        this.isSlabApplicable = isSlabApplicable;
    }

    public List<CommissionSlabDTO> getCommissionSlabs() {
        return commissionSlabs;
    }

    public void setCommissionSlabs(List<CommissionSlabDTO> commissionSlabs) {
        this.commissionSlabs = commissionSlabs;
    }

    private String collegeName;
    private String city;
    private String country;
    private boolean collegeActive = true;
    private List<LocationDetailDTO> locationDetails;

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

    public boolean isCollegeActive() {
        return collegeActive;
    }

    public void setCollegeActive(boolean collegeActive) {
        this.collegeActive = collegeActive;
    }

    public List<LocationDetailDTO> getLocationDetails() {
        return locationDetails;
    }

    public void setLocationDetails(List<LocationDetailDTO> locationDetails) {
        this.locationDetails = locationDetails;
    }

    @Override
    public String toString() {
        return "AgentCollegeAccessDTO [collegeId=" + collegeId + ", collegeName=" + collegeName + ", locationIds="
                + locationIds
                + ", accessAllLocations=" + accessAllLocations + ", locationDetails=" + locationDetails + "]";
    }

    public static class LocationDetailDTO {
        private Long id;
        private String state;
        private String district;

        public LocationDetailDTO(Long id, String state, String district) {
            this.id = id;
            this.state = state;
            this.district = district;
        }

        public LocationDetailDTO() {
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getState() {
            return state;
        }

        public void setState(String state) {
            this.state = state;
        }

        public String getDistrict() {
            return district;
        }

        public void setDistrict(String district) {
            this.district = district;
        }
    }
}
