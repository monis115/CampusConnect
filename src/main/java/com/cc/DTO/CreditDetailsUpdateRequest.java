package com.cc.DTO;

import java.math.BigDecimal;

public class CreditDetailsUpdateRequest {
    private String purpose;
    private String lateFeeType;
    private BigDecimal lateFeeValue;
    private BigDecimal fixedLateFine;

    public String getPurpose() {
        return purpose;
    }

    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }

    public String getLateFeeType() {
        return lateFeeType;
    }

    public void setLateFeeType(String lateFeeType) {
        this.lateFeeType = lateFeeType;
    }

    public BigDecimal getLateFeeValue() {
        return lateFeeValue;
    }

    public void setLateFeeValue(BigDecimal lateFeeValue) {
        this.lateFeeValue = lateFeeValue;
    }

    public BigDecimal getFixedLateFine() {
        return fixedLateFine;
    }

    public void setFixedLateFine(BigDecimal fixedLateFine) {
        this.fixedLateFine = fixedLateFine;
    }
}
