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

    // New Admission Steps
    CONDITIONAL_OFFER_LETTER_ISSUED,
    PENDING_ADMISSION_LETTER,
    ADMISSION_LETTER_RECEIVED,

    // Telex Process
    ASSIGNED_TO_TELEX_MANAGER,
    APPLIED_FOR_TELEX,
    TELEX_APPLICATION_SUBMITTED_TO_UNIVERSITY,
    TELEX_APPLICATION_SUBMITTED_TO_MEA,
    TELEX_NUMBER_ISSUED,

    // Visa Process
    VISA_FEE_PENDING,
    VISA_FEE_RECEIVED,
    VISA_APPLIED,
    PASSPORT_SUBMITTED_TO_EMBASSY,
    VISA_STAMPED,

    // Legacy/Existing
    TELEX_MAN_ASSIGNED,
    ADMISSION_LETTER_WAITING, // Might be redundant with PENDING_ADMISSION_LETTER, keeping for safety
    ADMISSION_LETTER_GENERATED, // Might be redundant with ADMISSION_LETTER_RECEIVED
    ADMISSION_LETTER_APPROVED,
    TELEX_INITIATED, // Legacy
    TELEX_APPROVED, // Legacy
    VISA_INITIATED, // Legacy
    VISA_APPROVED, // Legacy
    COMPLETED,
    REJECTED
}