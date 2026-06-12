package com.cc.modals;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class LoanTimelineEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "credit_id")
    private AccountantCredit credit;

    // LOAN_CREATED, DISBURSED, EMI_PAID, EMI_MISSED, PENALTY_APPLIED, FORECLOSED, CLOSED
    private String eventType;

    @Column(length = 1000)
    private String description;

    private LocalDateTime eventDate;

    @PrePersist
    protected void onCreate() {
        if (eventDate == null) {
            eventDate = LocalDateTime.now();
        }
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public AccountantCredit getCredit() {
        return credit;
    }

    public void setCredit(AccountantCredit credit) {
        this.credit = credit;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getEventDate() {
        return eventDate;
    }

    public void setEventDate(LocalDateTime eventDate) {
        this.eventDate = eventDate;
    }
}
