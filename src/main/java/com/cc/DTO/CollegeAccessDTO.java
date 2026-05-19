package com.cc.DTO;

/**
 * Simple DTO for College Access of an Agent
 */
public class CollegeAccessDTO {

    private Long collegeId;
    private String collegeName;

    public CollegeAccessDTO() {
    }

    public CollegeAccessDTO(Long collegeId, String collegeName) {
        this.collegeId = collegeId;
        this.collegeName = collegeName;
    }

    public Long getCollegeId() {
        return collegeId;
    }

    public void setCollegeId(Long collegeId) {
        this.collegeId = collegeId;
    }

    public String getCollegeName() {
        return collegeName;
    }

    public void setCollegeName(String collegeName) {
        this.collegeName = collegeName;
    }
}