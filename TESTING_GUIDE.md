# Testing Guide - Course Attendance System

This guide will walk you through testing all the features of the system.

## Prerequisites

1. MySQL database running on `localhost:3307`
2. Database named `course` created
3. Application running on `http://localhost:8080`

## Step-by-Step Testing

### 1. Initial Setup

#### 1.1 Insert Sample Data
```sql
-- Connect to MySQL
mysql -u root -p -h localhost -P 3307 course

-- Run the sample data script
source sample-data.sql;

-- Verify data
SELECT * FROM student;
SELECT * FROM course;
```

### 2. Test Authentication

#### 2.1 Create Admin Account
```bash
curl -X POST http://localhost:8080/api/auth/signup \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "admin123",
    "email": "admin@example.com",
    "fullName": "System Admin",
    "role": "ADMIN"
  }'
```

**Expected Response:**
```json
{
  "token": "eyJhbGciOiJIUzUxMiJ9...",
  "type": "Bearer",
  "user": {
    "id": 1,
    "username": "admin",
    "email": "admin@example.com",
    "fullName": "System Admin",
    "role": "ADMIN",
    "studentId": null
  }
}
```

**Save the token** as `ADMIN_TOKEN` for later use.

#### 2.2 Create Student Account
```bash
curl -X POST http://localhost:8080/api/auth/signup \
  -H "Content-Type: application/json" \
  -d '{
    "username": "student1",
    "password": "student123",
    "email": "john.doe@example.com",
    "fullName": "John Doe",
    "role": "STUDENT",
    "studentId": 1
  }'
```

**Save the token** as `STUDENT_TOKEN`.

#### 2.3 Test Login
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "admin123"
  }'
```

#### 2.4 Get Current User Info
```bash
curl -X GET http://localhost:8080/api/auth/me \
  -H "Authorization: Bearer YOUR_ADMIN_TOKEN"
```

### 3. Test Session Management (Admin)

#### 3.1 Create a Session
```bash
curl -X POST http://localhost:8080/api/admin/sessions \
  -H "Authorization: Bearer YOUR_ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "courseId": 1,
    "sessionDate": "2025-11-20T10:00:00",
    "startTime": "2025-11-20T10:00:00",
    "endTime": "2025-11-20T12:00:00"
  }'
```

**Expected Response:**
```json
{
  "id": 1,
  "courseId": 1,
  "courseCode": "CS101",
  "courseTitle": "Introduction to Programming",
  "sessionDate": "2025-11-20T10:00:00",
  "startTime": "2025-11-20T10:00:00",
  "endTime": "2025-11-20T12:00:00",
  "qrToken": null,
  "qrTokenExpiresAt": null,
  "qrTokenActive": false
}
```

**Save the session ID** as `SESSION_ID`.

#### 3.2 Generate QR Token for Session
```bash
curl -X POST "http://localhost:8080/api/admin/sessions/1/generate-qr?validityMinutes=10" \
  -H "Authorization: Bearer YOUR_ADMIN_TOKEN"
```

**Expected Response:**
```json
{
  "sessionId": 1,
  "qrToken": "abc123xyz...",
  "checkInUrl": "/api/attendance/scan?sessionId=1&token=abc123xyz...",
  "expiresAt": "2025-11-20T10:10:00"
}
```

**Save the QR token** as `QR_TOKEN` for the next step.

#### 3.3 Get Session Details
```bash
curl -X GET http://localhost:8080/api/admin/sessions/1 \
  -H "Authorization: Bearer YOUR_ADMIN_TOKEN"
```

#### 3.4 List Sessions by Course
```bash
curl -X GET http://localhost:8080/api/admin/sessions/course/1 \
  -H "Authorization: Bearer YOUR_ADMIN_TOKEN"
```

### 4. Test Student Check-In

#### 4.1 Student Checks In
```bash
curl -X POST http://localhost:8080/api/attendance/check-in \
  -H "Authorization: Bearer YOUR_STUDENT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "sessionId": 1,
    "qrToken": "YOUR_QR_TOKEN_FROM_STEP_3.2"
  }'
```

**Expected Response:**
```json
{
  "id": 1,
  "sessionId": 1,
  "studentId": 1,
  "studentNumber": "STU001",
  "studentName": "John Doe",
  "status": "PRESENT",
  "checkedAt": "2025-11-20T10:05:00"
}
```

#### 4.2 Try Duplicate Check-In (Should Fail)
```bash
# Run the same command again
curl -X POST http://localhost:8080/api/attendance/check-in \
  -H "Authorization: Bearer YOUR_STUDENT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "sessionId": 1,
    "qrToken": "YOUR_QR_TOKEN"
  }'
```

**Expected Response:**
```json
{
  "message": "Student has already checked in to this session"
}
```

### 5. Test Attendance Queries

#### 5.1 Get Attendance for a Session (Admin)
```bash
curl -X GET http://localhost:8080/api/attendance/session/1 \
  -H "Authorization: Bearer YOUR_ADMIN_TOKEN"
```

**Expected Response:**
```json
[
  {
    "id": 1,
    "sessionId": 1,
    "studentId": 1,
    "studentNumber": "STU001",
    "studentName": "John Doe",
    "status": "PRESENT",
    "checkedAt": "2025-11-20T10:05:00"
  }
]
```

#### 5.2 Get Attendance for a Student
```bash
curl -X GET http://localhost:8080/api/attendance/student/1 \
  -H "Authorization: Bearer YOUR_STUDENT_TOKEN"
```

### 6. Test Error Cases

#### 6.1 Invalid QR Token
```bash
curl -X POST http://localhost:8080/api/attendance/check-in \
  -H "Authorization: Bearer YOUR_STUDENT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "sessionId": 1,
    "qrToken": "invalid-token"
  }'
```

**Expected:** Error message about invalid token

#### 6.2 Expired QR Token
Wait for the token to expire (after validityMinutes), then try to check in.

**Expected:** Error message about expired token

#### 6.3 Unauthorized Access
```bash
# Try to create session as student (should fail)
curl -X POST http://localhost:8080/api/admin/sessions \
  -H "Authorization: Bearer YOUR_STUDENT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "courseId": 1,
    "sessionDate": "2025-11-20T10:00:00",
    "startTime": "2025-11-20T10:00:00",
    "endTime": "2025-11-20T12:00:00"
  }'
```

**Expected:** 403 Forbidden

#### 6.4 No Authentication
```bash
# Try to access protected endpoint without token
curl -X GET http://localhost:8080/api/auth/me
```

**Expected:** 401 Unauthorized

## Using Postman

Instead of curl, you can use Postman:

1. Import the `postman_collection.json` file
2. The collection includes all API endpoints
3. Login requests automatically save tokens to collection variables
4. Use the saved tokens for subsequent requests

### Postman Workflow:

1. **Setup**
   - Open Postman
   - Import `postman_collection.json`
   - Set `baseUrl` to `http://localhost:8080`

2. **Authentication**
   - Run "Signup Admin" 
   - Run "Login Admin" (token saved automatically)
   - Run "Signup Student"
   - Run "Login Student" (token saved automatically)

3. **Session Management**
   - Run "Create Session"
   - Run "Generate QR Token"
   - Copy the `qrToken` from response

4. **Check-In**
   - Paste the QR token in "Student Check-In" request body
   - Run "Student Check-In"

5. **View Attendance**
   - Run "Get Attendance by Session"
   - Run "Get Attendance by Student"

## Database Verification

After testing, verify the data in MySQL:

```sql
-- Check users
SELECT id, username, email, role, student_id FROM user;

-- Check sessions
SELECT s.id, c.title as course, s.session_date, s.qr_token IS NOT NULL as has_qr
FROM session s
JOIN course c ON s.course_id = c.id;

-- Check attendance
SELECT 
  a.id,
  s.student_number,
  s.full_name,
  c.title as course,
  a.status,
  a.checked_at
FROM attendance a
JOIN student s ON a.student_id = s.id
JOIN session sess ON a.session_id = sess.id
JOIN course c ON sess.course_id = c.id;
```

## Common Issues

### Issue 1: Token Expired
**Solution:** Login again to get a new token

### Issue 2: Cannot Create Student User
**Solution:** Ensure the `studentId` refers to an existing student in the database

### Issue 3: QR Token Validation Fails
**Solution:** 
- Verify the token hasn't expired
- Ensure you're using the exact token from the generate-qr response
- Check that the session ID matches

### Issue 4: Database Connection Error
**Solution:**
- Verify MySQL is running on port 3307
- Check database credentials in `application.properties`
- Ensure database `course` exists

## Success Criteria

You should be able to:
- ✅ Create admin and student users
- ✅ Login and receive JWT tokens
- ✅ Create sessions as admin
- ✅ Generate QR tokens for sessions
- ✅ Check in as student using QR token
- ✅ View attendance records
- ✅ Prevent duplicate check-ins
- ✅ Handle expired tokens correctly
- ✅ Enforce role-based access control
