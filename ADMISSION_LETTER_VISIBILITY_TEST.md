# Testing Admission Letter Visibility Control

## What Was Changed

Modified `Student.java` `getAdmissionLetterPath()` method to return `null` unless status is `ADMISSION_LETTER_APPROVED`.

```java
public String getAdmissionLetterPath() {
    // Only return admission letter path if it's been approved
    if (this.applicationStatus == StudentStatus.ADMISSION_LETTER_APPROVED) {
        return admissionLetterPath;
    }
    return null;  // Hide from students/agents until approved
}
```

---

## Test Scenarios

### Scenario 1: Before Upload (Status: FORWARDED_TO_UNI)

**Request:**
```bash
GET http://localhost:8080/api/students/1
```

**Expected Response:**
```json
{
  "id": 1,
  "fullName": "Rohit Verma",
  "applicationStatus": "FORWARDED_TO_UNI",
  "admissionLetterPath": null,  // ✅ NULL - letter not uploaded yet
  ...
}
```

---

### Scenario 2: After Upload, Before Approval (Status: ADMISSION_LETTER_GENERATED)

**Step 1: Upload Letter**
```bash
POST http://localhost:8080/api/universityPerson/upload-admission-letter/1
Form-data: file + universityPersonUsername=UE12345
```

**Step 2: Check Student**
```bash
GET http://localhost:8080/api/students/1
```

**Expected Response:**
```json
{
  "id": 1,
  "fullName": "Rohit Verma",
  "applicationStatus": "ADMISSION_LETTER_GENERATED",
  "admissionLetterPath": null,  // ✅ NULL - not approved yet, HIDDEN from students/agents
  ...
}
```

**🔒 Security:** Even though the file exists on the server, students and agents cannot see the path!

---

### Scenario 3: After Approval (Status: ADMISSION_LETTER_APPROVED)

**Step 1: Approve Letter**
```bash
POST http://localhost:8080/api/managers/approve-admission-letter/1
Body: {"admissionManagerUsername": "M2522229"}
```

**Step 2: Check Student**
```bash
GET http://localhost:8080/api/students/1
```

**Expected Response:**
```json
{
  "id": 1,
  "fullName": "Rohit Verma",
  "applicationStatus": "ADMISSION_LETTER_APPROVED",
  "admissionLetterPath": "uploads/admission-letters/admission_letter_1_abc123.pdf",  // ✅ NOW VISIBLE!
  ...
}
```

**✅ Success:** Now students and agents can see and download the admission letter!

---

## How to Test

### Test 1: Student Cannot See Unapproved Letter

1. Upload admission letter (UniversityPerson)
2. **GET** `/api/students/1`
3. ✅ Verify `admissionLetterPath` is `null`
4. ✅ Verify `applicationStatus` is `ADMISSION_LETTER_GENERATED`

### Test 2: Agent Cannot See Unapproved Letter

1. **GET** `/api/agents/{agentId}` (returns agent's students)
2. ✅ Verify student's `admissionLetterPath` is `null`

### Test 3: Student CAN See Approved Letter

1. Approve letter (AdmissionManager)
2. **GET** `/api/students/1`
3. ✅ Verify `admissionLetterPath` is populated
4. ✅ Verify `applicationStatus` is `ADMISSION_LETTER_APPROVED`

---

## Complete Test Flow

```bash
# 1. Check initial state
GET /api/students/1
# Expected: admissionLetterPath = null

# 2. Upload letter
POST /api/universityPerson/upload-admission-letter/1
# Expected: Status 200, admissionLetterPath in response but...

# 3. Verify students can't see it yet
GET /api/students/1
# Expected: admissionLetterPath = null (HIDDEN!)

# 4. Approve letter
POST /api/managers/approve-admission-letter/1
# Expected: Status 200

# 5. Verify students CAN see it now
GET /api/students/1
# Expected: admissionLetterPath = "uploads/admission-letters/..." (VISIBLE!)
```

---

## Security Summary

| Status | File Exists | Visible to Student/Agent |
|--------|-------------|--------------------------|
| `FORWARDED_TO_UNI` | ❌ No | ❌ No |
| `ADMISSION_LETTER_GENERATED` | ✅ Yes | ❌ **No** (HIDDEN) |
| `ADMISSION_LETTER_APPROVED` | ✅ Yes | ✅ **Yes** (VISIBLE) |

---

## Notes

- UniversityPerson and AdmissionManager can always see the path in their responses
- The conditional logic only applies when the Student object is serialized to JSON
- Restart the application for changes to take effect
