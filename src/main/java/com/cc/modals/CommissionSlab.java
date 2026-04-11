package com.cc.modals;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class CommissionSlab {

    @Column(nullable = false)
    private Integer minStudents;

    @Column(nullable = true)
    private Integer maxStudents; // null indicates "and above" (e.g. 21+)

    @Column(nullable = false)
    private Double dealPricePerStudent;

    @com.fasterxml.jackson.annotation.JsonIgnore
    @Column(name = "commission_per_student", nullable = false)
    private Double legacyCommission = 0.0;

    public CommissionSlab() {
    }

    public CommissionSlab(Integer minStudents, Integer maxStudents, Double dealPricePerStudent) {
        this.minStudents = minStudents;
        this.maxStudents = maxStudents;
        this.dealPricePerStudent = dealPricePerStudent;
    }

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
