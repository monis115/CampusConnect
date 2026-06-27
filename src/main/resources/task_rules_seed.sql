-- Enterprise Task Engine - Default Rules Seed Data
-- These rules replace the hardcoded if/else logic in the old TaskSyncService.
-- To add a new automated task: INSERT a new row here. Zero code changes needed.

INSERT IGNORE INTO task_rules (rule_code, task_title, description, condition_type, condition_field, condition_value, condition_days_threshold, owner_role, department, category, priority, creation_delay_minutes, due_delay_minutes, watcher_roles, escalation_chain, auto_close_condition_field, auto_close_condition_value, prerequisite_rule_code, enabled, requires_approval, created_at) VALUES

-- Rule 1: Admission Letter Missing
('ADMISSION_LETTER_MISSING', 'Upload Admission Letter', 'Admission Letter not found for {studentName}', 'FIELD_NULL', 'admissionLetterPath', NULL, NULL, 'UNIVERSITY_COORDINATOR', 'UNIVERSITY', 'DOCUMENT', 'HIGH', 5, 4320, 'ADMISSION_MANAGER', '["UNIVERSITY_COORDINATOR","ADMISSION_MANAGER","ADMIN"]', 'admissionLetterPath', NULL, NULL, true, true, NOW()),

-- Rule 2: Telex Pending > 7 days
('TELEX_PENDING_7_DAYS', 'Follow Up University on Telex', 'Telex pending > 7 days for {studentName}', 'DATE_OVERDUE', 'telexRequestDate', NULL, 7, 'TELEX_MANAGER', 'TELEX', 'FOLLOW_UP', 'HIGH', 5, 1440, 'ADMISSION_MANAGER', '["TELEX_MANAGER","ADMISSION_MANAGER","ADMIN"]', 'telexIssueDate', NULL, NULL, true, false, NOW()),

-- Rule 3: Visa Payment Pending
('VISA_PAYMENT_PENDING', 'Collect Visa Payment', 'Visa payment pending for {studentName}', 'FIELD_EQUALS', 'paymentStatus', 'Pending', NULL, 'ACCOUNTANT', 'ACCOUNTANT', 'PAYMENT', 'HIGH', 5, 2880, 'ADMISSION_MANAGER', '["ACCOUNTANT","ADMISSION_MANAGER","ADMIN"]', 'paymentStatus', 'Approved', NULL, true, true, NOW()),

-- Rule 4: Passport Transfer
('PASSPORT_TRANSFER_PENDING', 'Collect and dispatch passport to Delhi', 'Student {studentName} passport is ready for transfer', 'STATUS_EQUALS', 'status', 'PASSPORT_RECEIVED', NULL, 'DOCUMENT_MANAGER', 'DOCUMENT', 'DOCUMENT', 'HIGH', 5, 2880, 'ADMISSION_MANAGER', '["DOCUMENT_MANAGER","ADMISSION_MANAGER","ADMIN"]', 'status', 'PASSPORT_DISPATCHED', NULL, true, true, NOW()),

-- Rule 5: Flight Missing
('FLIGHT_MISSING', 'Book Flight for Student', 'Visa done, flight ticket missing for {studentName}', 'FIELD_NULL', 'flightTicketPath', NULL, NULL, 'AGENT', 'AGENT', 'TRAVEL', 'HIGH', 5, 4320, 'ADMISSION_MANAGER', '["VISA_MANAGER","ADMISSION_MANAGER","ADMIN"]', 'flightTicketPath', NULL, NULL, true, true, NOW()),

-- Rule 6: Arrival Confirmation
('ARRIVAL_CONFIRMATION', 'Confirm Student Arrival', 'Student {studentName} flight landed, confirm arrival at university', 'DATE_OVERDUE', 'flightDate', NULL, 0, 'VISA_MANAGER', 'VISA', 'TRAVEL', 'HIGH', 5, 1440, 'ADMISSION_MANAGER,AGENT', '["VISA_MANAGER","ADMISSION_MANAGER","ADMIN"]', 'status', 'JOINED_COLLEGE', NULL, true, true, NOW()),

-- Admission Manager Operational Tasks

-- Rule 7: Assign University Coordinator
('ASSIGN_UNIVERSITY_COORDINATOR', 'Assign University Coordinator', 'Student {studentName} is registered but no University Coordinator is assigned.', 'FIELD_NULL', 'universityPerson', NULL, NULL, 'ADMISSION_MANAGER', 'ADMISSION', 'COORDINATION', 'HIGH', 1, 1440, 'ADMIN', '["ADMISSION_MANAGER","ADMIN"]', 'universityPerson', NULL, NULL, true, false, NOW()),

-- Rule 8: Approve Admission Letter (Wait for Approval)
('REVIEW_ADMISSION_LETTER', 'Approve Admission Letter', 'Admission letter for {studentName} has not been approved.', 'ACTION_EQUALS', 'action', 'APPROVE_ADMISSION_LETTER', NULL, 'ADMISSION_MANAGER', 'ADMISSION', 'APPROVAL', 'HIGH', 1, 1440, 'ADMIN', '["ADMISSION_MANAGER","ADMIN"]', 'applicationStatus', 'ADMISSION_LETTER_GENERATED', NULL, true, false, NOW()),

-- Rule 9: Assign Telex Manager
('ASSIGN_TELEX_MANAGER', 'Assign Telex Manager', 'Admission letter approved but no Telex Manager assigned for {studentName}.', 'ACTION_EQUALS', 'action', 'ASSIGN_TELEX_MAN', NULL, 'ADMISSION_MANAGER', 'ADMISSION', 'COORDINATION', 'HIGH', 1, 1440, 'ADMIN', '["ADMISSION_MANAGER","ADMIN"]', 'telexMan', NULL, NULL, true, false, NOW()),

-- Rule 10: Assign Visa Manager
('ASSIGN_VISA_MANAGER', 'Assign Visa Manager', 'Telex is complete but no Visa Manager assigned for {studentName}.', 'ACTION_EQUALS', 'action', 'ASSIGN_TO_VISA_MAN', NULL, 'ADMISSION_MANAGER', 'ADMISSION', 'COORDINATION', 'HIGH', 1, 1440, 'ADMIN', '["ADMISSION_MANAGER","ADMIN"]', 'visaMan', NULL, NULL, true, false, NOW()),

-- Rule 11: Review Agent Request
('REVIEW_AGENT_REQUEST', 'Review Agent Request', 'Agent requested additional documents for {studentName}.', 'FIELD_EQUALS', 'hasRaiseReturnRequest', 'true', NULL, 'ADMISSION_MANAGER', 'ADMISSION', 'COORDINATION', 'HIGH', 1, 1440, 'ADMIN', '["ADMISSION_MANAGER","ADMIN"]', 'hasRaiseReturnRequest', 'false', NULL, true, false, NOW()),

-- Rule 12: Agent Submit Visa Payment
('AGENT_SUBMIT_VISA_PAYMENT', 'Submit Visa Payment Receipt', 'Upload the transaction receipt for {studentName} visa payment.', 'FIELD_EQUALS', 'paymentStatus', 'Pending', NULL, 'AGENT', 'AGENT', 'PAYMENT', 'HIGH', 5, 2880, 'ADMISSION_MANAGER', '["AGENT","ADMISSION_MANAGER","ADMIN"]', 'paymentStatus', 'Approved', NULL, true, true, NOW()),

-- Rule 13: Agent Request Document Return
('AGENT_REQUEST_DOCUMENT_RETURN', 'Request Document Return', 'Visa stamped for {studentName}. Please request passport return by providing shipping details.', 'STATUS_EQUALS', 'status', 'VISA_STAMPED', NULL, 'AGENT', 'AGENT', 'DOCUMENT', 'HIGH', 5, 1440, 'ADMISSION_MANAGER', '["AGENT","ADMISSION_MANAGER","ADMIN"]', 'hasRaiseReturnRequest', 'true', NULL, true, true, NOW()),

-- Rule 14: Agent Confirm Document Receipt
('AGENT_CONFIRM_DOCUMENT_RECEIPT', 'Confirm Physical Document Receipt', 'Passport dispatched for {studentName}. Please confirm receipt once arrived.', 'STATUS_EQUALS', 'status', 'PASSPORT_DISPATCHED', NULL, 'AGENT', 'AGENT', 'DOCUMENT', 'HIGH', 5, 1440, 'ADMISSION_MANAGER', '["AGENT","ADMISSION_MANAGER","ADMIN"]', 'status', 'PASSPORT_RECEIVED', NULL, true, true, NOW()),

-- Rule 15: Agent Initiate Telex Reminder
('AGENT_INITIATE_TELEX', 'Initiate Telex Processing', 'Admission Letter approved for {studentName}. Please initiate Telex.', 'STATUS_EQUALS', 'status', 'ADMISSION_LETTER_APPROVED', NULL, 'AGENT', 'AGENT', 'TELEX', 'HIGH', 2880, 1440, 'ADMISSION_MANAGER', '["AGENT","ADMISSION_MANAGER","ADMIN"]', 'status', 'TELEX_INITIATED', NULL, true, false, NOW()),

-- Rule 16: Agent Request Visa Processing Reminder
('AGENT_REQUEST_VISA', 'Request Visa Processing', 'Telex Number issued for {studentName}. Please request Visa processing.', 'STATUS_EQUALS', 'status', 'TELEX_NUMBER_ISSUED', NULL, 'AGENT', 'AGENT', 'VISA', 'HIGH', 2880, 1440, 'ADMISSION_MANAGER', '["AGENT","ADMISSION_MANAGER","ADMIN"]', 'status', 'VISA_FEE_PENDING', NULL, true, false, NOW());
