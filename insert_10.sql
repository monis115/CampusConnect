CREATE TEMPORARY TABLE temp_student AS SELECT * FROM student WHERE id = 1;

DELIMITER //
CREATE PROCEDURE insert_10_students()
BEGIN
  DECLARE i INT DEFAULT 1;
  WHILE i <= 10 DO
    UPDATE temp_student SET 
      id = NULL,
      full_name = CONCAT('Test Visa Student ', i),
      application_id = CONCAT('TEST_APP_VM_', i, '_', FLOOR(RAND() * 100000)),
      email = CONCAT('testvm_', i, '_', FLOOR(RAND() * 100000), '@example.com'),
      visa_man_id = NULL,
      action = NULL,
      action_on_user = NULL,
      application_status = 'VISA_INITIATED',
      payment_status = 'Pending';
      
    INSERT INTO student SELECT * FROM temp_student;
    SET i = i + 1;
  END WHILE;
END //
DELIMITER ;

CALL insert_10_students();
DROP PROCEDURE insert_10_students;
DROP TEMPORARY TABLE temp_student;
