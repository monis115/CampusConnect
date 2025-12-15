package com.cc.DTO;

import java.time.LocalDate;

public class DocumentReturnRequestDTO {

    // Request Details
    private String contactPersonName;
    private String contactPhone;
    private String alternativePhone;
    private String addressWithZip;
    private LocalDate needBeforeDate;

    // Travel Confirmation
    private LocalDate travelDate;
    private String fromCountry;
    private String fromAirport;
    private String toCountry;
    private String toAirport;
    private String pnrNumber;

    // Optional file can be handled in controller via MultipartFile,
    // but we might want a field here if we were sending JSON only.
    // Since we are likely using FormData for file upload, the DTO will likely come
    // as JSON string.

    public DocumentReturnRequestDTO() {
    }

    public String getContactPersonName() {
        return contactPersonName;
    }

    public void setContactPersonName(String contactPersonName) {
        this.contactPersonName = contactPersonName;
    }

    public String getContactPhone() {
        return contactPhone;
    }

    public void setContactPhone(String contactPhone) {
        this.contactPhone = contactPhone;
    }

    public String getAlternativePhone() {
        return alternativePhone;
    }

    public void setAlternativePhone(String alternativePhone) {
        this.alternativePhone = alternativePhone;
    }

    public String getAddressWithZip() {
        return addressWithZip;
    }

    public void setAddressWithZip(String addressWithZip) {
        this.addressWithZip = addressWithZip;
    }

    public LocalDate getNeedBeforeDate() {
        return needBeforeDate;
    }

    public void setNeedBeforeDate(LocalDate needBeforeDate) {
        this.needBeforeDate = needBeforeDate;
    }

    public LocalDate getTravelDate() {
        return travelDate;
    }

    public void setTravelDate(LocalDate travelDate) {
        this.travelDate = travelDate;
    }

    public String getFromCountry() {
        return fromCountry;
    }

    public void setFromCountry(String fromCountry) {
        this.fromCountry = fromCountry;
    }

    public String getFromAirport() {
        return fromAirport;
    }

    public void setFromAirport(String fromAirport) {
        this.fromAirport = fromAirport;
    }

    public String getToCountry() {
        return toCountry;
    }

    public void setToCountry(String toCountry) {
        this.toCountry = toCountry;
    }

    public String getToAirport() {
        return toAirport;
    }

    public void setToAirport(String toAirport) {
        this.toAirport = toAirport;
    }

    public String getPnrNumber() {
        return pnrNumber;
    }

    public void setPnrNumber(String pnrNumber) {
        this.pnrNumber = pnrNumber;
    }
}
