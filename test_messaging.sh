#!/bin/bash

# Base URL
BASE_URL="http://localhost:8080/api/messages"

# IDs - REPLACE THESE WITH ACTUAL IDs FROM YOUR DATABASE IF NEEDED
AGENT_ID=1
MANAGER_ID=1
STUDENT_ID=1

echo "--------------------------------------------------"
echo "Testing Messaging Feature"
echo "--------------------------------------------------"

# 1. Send a message from Agent to Manager
echo "1. Sending a message from Agent (ID: $AGENT_ID) to Manager (Derived from Student) for Student (ID: $STUDENT_ID)..."
RESPONSE=$(curl -s -X POST "$BASE_URL/send" \
  -H "Content-Type: application/json" \
  -d "{
    \"agentId\": $AGENT_ID,
    \"studentId\": $STUDENT_ID,
    \"content\": \"Hello, checking on the status of this student.\",
    \"senderRole\": \"AGENT\"
  }")

echo "Response:"
echo "$RESPONSE"
echo -e "\n"

# 2. Reply from Manager
echo "2. Sending a reply from Manager..."
RESPONSE=$(curl -s -X POST "$BASE_URL/send" \
  -H "Content-Type: application/json" \
  -d "{
    \"agentId\": $AGENT_ID,
    \"studentId\": $STUDENT_ID,
    \"content\": \"We are processing the documents. Should be done shortly.\",
    \"senderRole\": \"MANAGER\"
  }")

echo "Response:"
echo "$RESPONSE"
echo -e "\n"

# 3. Get chat history for Student
echo "3. Retrieving chat history for Student (ID: $STUDENT_ID)..."
curl -s "$BASE_URL/student/$STUDENT_ID" | python3 -m json.tool
echo -e "\n"

# 4. Get Manager Inbox
echo "4. Retrieving Inbox for Manager (ID: $MANAGER_ID)..."
curl -s "$BASE_URL/manager/$MANAGER_ID" | python3 -m json.tool

echo -e "\n--------------------------------------------------"
echo "Done."
