# Course Attendance System - API Documentation

## Overview
This is a modular Spring Boot application for course attendance management with QR code-based check-in functionality.

## Architecture

The project follows a modular architecture:

```
src/main/java/com/course/
├── core/
│   └── auth/              # Authentication & Authorization
│       ├── controller/    # Auth REST controllers
│       ├── dto/          # Auth DTOs
│       ├── model/        # User entity
│       ├── repository/   # User repository
│       ├── security/     # Security config & JWT filter
│       └── service/      # Auth services
└── modules/
    └── attendance/       # Attendance module
        ├── controller/   # Session & Attendance controllers
        ├── dto/         # Module DTOs
        ├── model/       # Course, Session, Student, Attendance entities
        ├── repository/  # Module repositories
        └── service/     # Module services
```

## Database Schema

- **user** - User accounts (admin/student) with authentication
- **student** - Student information
- **course** - Course details
- **session** - Class sessions with QR token support
- **attendance** - Attendance records

## API Endpoints

### Authentication Endpoints

#### 1. Sign Up
```
POST /api/auth/signup
Content-Type: application/json

{
  "username": "student1",
  "password": "password123",
  "email": "student1@example.com",
  "fullName": "John Doe",
  "role": "STUDENT",
  "studentId": 1
}

Response:
{
  "token": "eyJhbGciOiJIUzUxMiJ9...",
  "type": "Bearer",
  "user": {
    "id": 1,
    "username": "student1",
    "email": "student1@example.com",
    "fullName": "John Doe",
    "role": "STUDENT",
    "studentId": 1
  }
}
```

#### 2. Login
```
POST /api/auth/login
Content-Type: application/json

{
  "username": "student1",
  "password": "password123"
}

Response:
{
  "token": "eyJhbGciOiJIUzUxMiJ9...",
  "type": "Bearer",
  "user": { ... }
}
```

#### 3. Get Current User
```
GET /api/auth/me
Authorization: Bearer {token}

Response:
{
  "id": 1,
  "username": "student1",
  "email": "student1@example.com",
  "fullName": "John Doe",
  "role": "STUDENT",
  "studentId": 1
}
```

### Session Management (Admin Only)

#### 1. Create Session
```
POST /api/admin/sessions
Authorization: Bearer {admin-token}
Content-Type: application/json

{
  "courseId": 1,
  "sessionDate": "2025-11-20T10:00:00",
  "startTime": "2025-11-20T10:00:00",
  "endTime": "2025-11-20T12:00:00"
}

Response:
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

#### 2. Generate QR Token for Session
```
POST /api/admin/sessions/{sessionId}/generate-qr?validityMinutes=10
Authorization: Bearer {admin-token}

Response:
{
  "sessionId": 1,
  "qrToken": "abc123xyz789...",
  "checkInUrl": "/api/attendance/scan?sessionId=1&token=abc123xyz789...",
  "expiresAt": "2025-11-20T10:10:00"
}
```

#### 3. Get Session Details
```
GET /api/admin/sessions/{sessionId}
Authorization: Bearer {admin-token}

Response: SessionDto
```

#### 4. Get Sessions by Course
```
GET /api/admin/sessions/course/{courseId}
Authorization: Bearer {admin-token}

Response: List<SessionDto>
```

### Attendance Endpoints

#### 1. Student Check-In (QR Scan)
```
POST /api/attendance/check-in
Authorization: Bearer {student-token}
Content-Type: application/json

{
  "sessionId": 1,
  "qrToken": "abc123xyz789..."
}

Response:
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

#### 2. Get Attendance by Session (Admin)
```
GET /api/attendance/session/{sessionId}
Authorization: Bearer {admin-token}

Response: List<AttendanceDto>
```

#### 3. Get Attendance by Student
```
GET /api/attendance/student/{studentId}
Authorization: Bearer {token}

Response: List<AttendanceDto>
```

## User Roles

### ADMIN
- Create sessions
- Generate QR codes for sessions
- View all attendance records
- Manage courses

### STUDENT
- Check in to sessions using QR code
- View own attendance records
- Must be linked to a student record

## QR Code Check-In Flow

1. **Admin Creates Session**
   - POST `/api/admin/sessions`
   - Admin creates a new class session

2. **Admin Generates QR Token**
   - POST `/api/admin/sessions/{id}/generate-qr?validityMinutes=10`
   - System generates a secure random token
   - Token is valid for specified minutes (default: 10)

3. **Frontend Displays QR Code** (to be implemented)
   - Frontend receives the `checkInUrl` from step 2
   - Displays it as a QR code
   - QR code contains: `/api/attendance/scan?sessionId=1&token=abc123...`

4. **Student Scans QR Code**
   - Student must be logged in first
   - Student scans QR code
   - Frontend extracts `sessionId` and `token`
   - Sends POST to `/api/attendance/check-in` with the data

5. **System Validates and Records**
   - Validates token matches and hasn't expired
   - Checks student hasn't already checked in
   - Creates attendance record with status PRESENT

## Security Features

- **JWT Authentication**: Stateless authentication with Bearer tokens
- **Role-Based Access Control**: ADMIN and STUDENT roles
- **Secure QR Tokens**: Cryptographically secure random tokens
- **Token Expiration**: QR tokens expire after configurable time
- **Password Encryption**: BCrypt password hashing
- **CORS Support**: Cross-origin requests enabled

## Running the Application

1. **Start MySQL Database**
   ```bash
   # Database should be running on localhost:3307
   # Database name: course
   ```

2. **Build the Application**
   ```bash
   ./mvnw clean install
   ```

3. **Run the Application**
   ```bash
   ./mvnw spring-boot:run
   ```

4. **Access the API**
   - Base URL: `http://localhost:8080`
   - Health check: `GET http://localhost:8080/actuator/health` (if actuator is enabled)

## Configuration

Key properties in `application.properties`:

```properties
# JWT Configuration
jwt.secret=mySecretKeyForJWTTokenGenerationThatIsAtLeast256BitsLongAndSecure
jwt.expiration=86400000  # 24 hours in milliseconds

# Database
spring.datasource.url=jdbc:mysql://localhost:3307/course
spring.datasource.username=root
spring.datasource.password=my-secret-pw

# Liquibase
spring.liquibase.change-log=classpath:db/changelog/db.changelog-master.yaml
```

## Sample Data Setup

You can create sample data using the API:

1. **Create an Admin User**
```bash
curl -X POST http://localhost:8080/api/auth/signup \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "admin123",
    "email": "admin@example.com",
    "fullName": "Admin User",
    "role": "ADMIN"
  }'
```

2. **Create a Student User**
```bash
# First, you need to insert a student record in the database
# Then create user:
curl -X POST http://localhost:8080/api/auth/signup \
  -H "Content-Type: application/json" \
  -d '{
    "username": "student1",
    "password": "student123",
    "email": "student1@example.com",
    "fullName": "Student One",
    "role": "STUDENT",
    "studentId": 1
  }'
```

## Error Handling

All endpoints return appropriate HTTP status codes:

- `200 OK` - Successful request
- `201 Created` - Resource created successfully
- `400 Bad Request` - Invalid input or business rule violation
- `401 Unauthorized` - Missing or invalid authentication
- `403 Forbidden` - Insufficient permissions
- `404 Not Found` - Resource not found
- `500 Internal Server Error` - Server error

Error response format:
```json
{
  "message": "Error description"
}
```

## Next Steps

To complete the system:

1. **Frontend Implementation**
   - QR code generation/display for sessions
   - QR code scanner for students
   - Student and admin dashboards

2. **Additional Features**
   - Course CRUD operations
   - Student CRUD operations
   - Attendance reports and analytics
   - Export attendance to CSV/Excel
   - Email notifications
   - Late check-in handling

3. **Security Enhancements**
   - Refresh tokens
   - Rate limiting
   - IP-based restrictions
   - Audit logging
