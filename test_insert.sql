CREATE TEMPORARY TABLE temp_student AS SELECT * FROM student WHERE id = 1;
UPDATE temp_student SET 
  id = NULL,
  full_name = CONCAT('Test Student ', FLOOR(RAND() * 100000)),
  application_id = CONCAT('APP_TEST_', FLOOR(RAND() * 100000)),
  email = CONCAT('test_student_', FLOOR(RAND() * 100000), '@example.com'),
  visa_man_id = 1,
  action = 11,
  action_on_user = 6,
  application_status = 'VISA_FEE_PENDING';
INSERT INTO student SELECT * FROM temp_student;
DROP TEMPORARY TABLE temp_student;
