package com.cc.modals;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })

public enum StudentStatus {
    Submitted,
    REGISTERED,
    UNDER_REVIEW,
    PENDING_APPROVAL,
    APPROVED,

    // Admission Process
    CONDITIONAL_OFFER_LETTER_GENERATED,
    PENDING_ADMISSION_LETTER,
    ADMISSION_LETTER_GENERATED, // Approved by Manager

    ADMISSION_LETTER_APPROVED,

    // Telex Process
    TELEX_PAYMENT_DOCS_SUBMITTED, // Agent submitted
    APPLIED_FOR_TELEX,
    TELEX_FEE_RECIEVED,
    TELEX_INITIATED, // Approved by Accountant & Doc
    TELEX_APPLICATION_SUBMITTED_TO_UNIVERSITY,
    TELEX_APPLICATION_SUBMITTED_TO_MEA,
    TELEX_NUMBER_ISSUED,
    DOCS_VARIFICATION_COMPLETE,
    DOCS_VARIFICATION_APPROVED, // Legacy - kept for backwards compatibility with old DB records

    // Visa Process
    VISA_FEE_PENDING,
    VISA_FEE_RECEIVED,
    VISA_APPLIED,
    VISA_STAMPED,
    PASSPORT_SUBMITTED_TO_EMBASSY,

    // Legacy/Other
    COMPLETED,
    REJECTED,

    // Keeping some legacy for backward compatibility if needed, or remove if safe
    FORWARDED_TO_UNI, // Deprecated in new flow but keeping to avoid immediate break
    PAYMENT_APPROVED // Kept for general payment
}