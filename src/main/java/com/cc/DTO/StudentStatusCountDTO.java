package com.cc.DTO;

public class StudentStatusCountDTO {
    private String status;
    private Long count;

    public StudentStatusCountDTO() {
    }

    public StudentStatusCountDTO(String status, Long count) {
        this.status = status;
        this.count = count;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getCount() {
        return count;
    }

    public void setCount(Long count) {
        this.count = count;
    }
}
