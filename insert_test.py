import mysql.connector
import random
import time

conn = mysql.connector.connect(
    host="localhost",
    user="root",
    password="Monis@333",
    database="campusConnect"
)
cursor = conn.cursor(dictionary=True)

cursor.execute("SELECT * FROM student WHERE id = 1")
student_data = cursor.fetchone()

# Exclude id
if 'id' in student_data:
    del student_data['id']

for i in range(10):
    current_time = int(time.time() * 1000)
    student_data['full_name'] = f"Test Visa Student {i}"
    student_data['application_id'] = f"TEST_APP_VM_{current_time}_{i}"
    student_data['email'] = f"testvm_{current_time}_{i}@example.com"
    student_data['visa_man_id'] = None
    student_data['action'] = None
    student_data['action_on_user'] = None
    student_data['application_status'] = 'VISA_INITIATED'
    student_data['payment_status'] = 'Pending'
    
    columns = ', '.join(student_data.keys())
    placeholders = ', '.join(['%s'] * len(student_data))
    
    query = f"INSERT INTO student ({columns}) VALUES ({placeholders})"
    cursor.execute(query, list(student_data.values()))
    student_id = cursor.lastrowid
    
    print(f"Inserted student {student_id}")

conn.commit()
cursor.close()
conn.close()
