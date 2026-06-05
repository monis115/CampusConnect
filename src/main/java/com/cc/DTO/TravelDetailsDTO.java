package com.cc.DTO;

import java.time.LocalDate;

public class TravelDetailsDTO {
    private LocalDate travelDoj;
    private String travelAirlines;
    private String travelSource;
    private String travelDestination;
    private String pnrNumber;
    private String flightNumber;

    public LocalDate getTravelDoj() { return travelDoj; }
    public void setTravelDoj(LocalDate travelDoj) { this.travelDoj = travelDoj; }
    public String getTravelAirlines() { return travelAirlines; }
    public void setTravelAirlines(String travelAirlines) { this.travelAirlines = travelAirlines; }
    public String getTravelSource() { return travelSource; }
    public void setTravelSource(String travelSource) { this.travelSource = travelSource; }
    public String getTravelDestination() { return travelDestination; }
    public void setTravelDestination(String travelDestination) { this.travelDestination = travelDestination; }
    public String getPnrNumber() { return pnrNumber; }
    public void setPnrNumber(String pnrNumber) { this.pnrNumber = pnrNumber; }
    public String getFlightNumber() { return flightNumber; }
    public void setFlightNumber(String flightNumber) { this.flightNumber = flightNumber; }
}
