package com.cc.modals;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })

public enum StudentStatus {
    Submitted,
    REGISTERED,
    UNDER_REVIEW,
    PENDING_APPROVAL,
    APPROVED,
    FORWARDED_TO_UNI,
    ADMISSION_LETTER_WAITING,
    ADMISSION_LETTER_GENERATED,
    ADMISSION_LETTER_APPROVED,
    TELEX_INITIATED,
    TELEX_APPROVED,
    VISA_INITIATED,
    VISA_APPROVED,
    COMPLETED,
    REJECTED
}