-- Run this SQL command in your MySQL database to fix the column size issue

ALTER TABLE student_status_history 
MODIFY COLUMN student_application_status VARCHAR(50);

-- This will increase the column size from its current limit to 50 characters
-- which will accommodate all enum values including FORWARDED_TO_UNI (16 chars)
