package com.cc.modals;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@Table(name = "document_return_request")
public class DocumentReturnRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    @JsonIgnoreProperties({ "documentReturnRequest", "hibernateLazyInitializer", "handler" })
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "agent_id", nullable = false)
    @JsonIgnoreProperties({ "student", "hibernateLazyInitializer", "handler" })
    private Agent agent;

    // Contact Person Details
    @Column(name = "contact_person_name")
    private String contactPersonName;

    @Column(name = "contact_phone")
    private String contactPhone;

    @Column(name = "alternative_phone")
    private String alternativePhone;

    @Column(name = "address_with_zip", columnDefinition = "TEXT")
    private String addressWithZip;

    @Column(name = "need_before_date")
    private LocalDate needBeforeDate;

    // Travel Confirmation
    @Column(name = "travel_date")
    private LocalDate travelDate;

    @Column(name = "from_country")
    private String fromCountry;

    @Column(name = "from_airport")
    private String fromAirport;

    @Column(name = "to_country")
    private String toCountry;

    @Column(name = "to_airport")
    private String toAirport;

    @Column(name = "pnr_number")
    private String pnrNumber;

    @Column(name = "ticket_pdf_path", columnDefinition = "TEXT")
    private String ticketPdfPath;

    @Column(name = "is_returned")
    private Boolean isReturned = false;

    public DocumentReturnRequest() {
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Student getStudent() {
        return student;
    }

    public void setStudent(Student student) {
        this.student = student;
    }

    public Agent getAgent() {
        return agent;
    }

    public void setAgent(Agent agent) {
        this.agent = agent;
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

    public String getTicketPdfPath() {
        return ticketPdfPath;
    }

    public void setTicketPdfPath(String ticketPdfPath) {
        this.ticketPdfPath = ticketPdfPath;
    }

    public Boolean getIsReturned() {
        return isReturned;
    }

    public void setIsReturned(Boolean isReturned) {
        this.isReturned = isReturned;
    }
}
