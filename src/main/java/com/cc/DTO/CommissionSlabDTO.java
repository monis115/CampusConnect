package com.cc.DTO;

public class CommissionSlabDTO {

    private Integer minStudents;
    private Integer maxStudents; // null means 'and above'
    private Double dealPricePerStudent;

    public Integer getMinStudents() {
        return minStudents;
    }

    public void setMinStudents(Integer minStudents) {
        this.minStudents = minStudents;
    }

    public Integer getMaxStudents() {
        return maxStudents;
    }

    public void setMaxStudents(Integer maxStudents) {
        this.maxStudents = maxStudents;
    }

    public Double getDealPricePerStudent() {
        return dealPricePerStudent;
    }

    public void setDealPricePerStudent(Double dealPricePerStudent) {
        this.dealPricePerStudent = dealPricePerStudent;
    }
}
