package com.cc.modals;

public enum StudentStatus {
    REGISTERED, 
    UNDER_REVIEW,// Student registered by agent
    PENDING_APPROVAL,               // Waiting for admin/admission manager approval
    APPROVED,                       // Approved by admin
    ADMISSION_LETTER_WAITING,       // Auto state after approval
    ADMISSION_LETTER_GENERATED,     // After admin generates the letter
    TELEX_INITIATED,                // Agent initiates telex
    TELEX_APPROVED,                 // Admin approves telex
    VISA_INITIATED,                 // Agent initiates visa
    VISA_APPROVED,                  // Admin approves visa
    COMPLETED,                      // Final stage
    REJECTED                        // Any rejection
}
