# System Architecture Overview

## High-Level Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                        Frontend (Not Implemented)                │
│                    - QR Code Display (Admin)                     │
│                    - QR Code Scanner (Student)                   │
│                    - Dashboards & Reports                        │
└──────────────────────────────┬──────────────────────────────────┘
                               │ HTTPS/REST
                               │ JWT Bearer Token
                               ▼
┌─────────────────────────────────────────────────────────────────┐
│                      Spring Boot Application                     │
│                         (Port 8080)                              │
│                                                                  │
│  ┌────────────────────────────────────────────────────────┐    │
│  │              Security Layer                             │    │
│  │  - JWT Authentication Filter                            │    │
│  │  - Role-Based Access Control                            │    │
│  │  - CORS Configuration                                   │    │
│  └────────────────────────────────────────────────────────┘    │
│                               │                                  │
│  ┌────────────────────────────┼────────────────────────────┐   │
│  │         Core Module        │    Modules                  │   │
│  │                            │                             │   │
│  │  ┌──────────────────┐      │   ┌──────────────────┐    │   │
│  │  │   Auth Module    │      │   │ Attendance Module│    │   │
│  │  │                  │      │   │                  │    │   │
│  │  │ • User Entity    │      │   │ • Course         │    │   │
│  │  │ • Signup/Login   │      │   │ • Session        │    │   │
│  │  │ • JWT Service    │      │   │ • Student        │    │   │
│  │  │ • Security       │      │   │ • Attendance     │    │   │
│  │  └──────────────────┘      │   │ • QR Generation  │    │   │
│  │                            │   └──────────────────┘    │   │
│  └────────────────────────────┴────────────────────────────┘   │
│                               │                                  │
│  ┌────────────────────────────────────────────────────────┐    │
│  │              Data Access Layer (JPA)                    │    │
│  │  - UserRepository                                       │    │
│  │  - CourseRepository, SessionRepository                  │    │
│  │  - StudentRepository, AttendanceRepository              │    │
│  └────────────────────────────────────────────────────────┘    │
│                               │                                  │
└───────────────────────────────┼──────────────────────────────────┘
                                │
                                ▼
                    ┌────────────────────────┐
                    │   MySQL Database       │
                    │   (Port 3307)          │
                    │                        │
                    │  Tables:               │
                    │  - user                │
                    │  - student             │
                    │  - course              │
                    │  - session             │
                    │  - attendance          │
                    └────────────────────────┘
```

## Module Structure

```
course/
│
├── core/                          # Core functionality
│   └── auth/                      # Authentication & Authorization
│       ├── controller/
│       │   └── AuthController.java         → POST /api/auth/signup
│       │                                    → POST /api/auth/login
│       │                                    → GET  /api/auth/me
│       ├── dto/
│       │   ├── SignupRequest.java
│       │   ├── LoginRequest.java
│       │   ├── AuthResponse.java
│       │   └── UserDto.java
│       ├── model/
│       │   ├── User.java                   → Database: user table
│       │   └── UserRole.java               → Enum: ADMIN, STUDENT
│       ├── repository/
│       │   └── UserRepository.java         → JPA Repository
│       ├── security/
│       │   ├── SecurityConfig.java         → Spring Security Config
│       │   └── JwtAuthenticationFilter.java → JWT Filter
│       └── service/
│           ├── AuthService.java            → Signup/Login Logic
│           ├── JwtTokenProvider.java       → JWT Generation/Validation
│           ├── UserDetailsServiceImpl.java → Spring Security Integration
│           └── UserMapper.java             → Entity/DTO Mapping
│
└── modules/                       # Feature modules
    └── attendance/                # Attendance Management
        ├── controller/
        │   ├── SessionController.java      → POST /api/admin/sessions
        │   │                                → POST /api/admin/sessions/{id}/generate-qr
        │   │                                → GET  /api/admin/sessions/{id}
        │   └── AttendanceController.java   → POST /api/attendance/check-in
        │                                    → GET  /api/attendance/session/{id}
        ├── dto/
        │   ├── SessionDto.java
        │   ├── CreateSessionRequest.java
        │   ├── QrTokenResponse.java
        │   ├── CheckInRequest.java
        │   └── AttendanceDto.java
        ├── model/
        │   ├── Course.java                 → Database: course table
        │   ├── Session.java                → Database: session table
        │   ├── Student.java                → Database: student table
        │   ├── Attendance.java             → Database: attendance table
        │   └── AttendanceStatus.java       → Enum: PRESENT, ABSENT, LATE
        ├── repository/
        │   ├── CourseRepository.java       → JPA Repository
        │   ├── SessionRepository.java      → JPA Repository
        │   ├── StudentRepository.java      → JPA Repository
        │   └── AttendanceRepository.java   → JPA Repository
        └── service/
            ├── SessionService.java         → Session CRUD, QR Generation
            └── AttendanceService.java      → Check-in Logic, Queries
```

## Data Flow - QR Check-In

```
┌──────────┐         ┌────────────────┐         ┌──────────────┐
│  Admin   │         │     System     │         │   Student    │
└────┬─────┘         └───────┬────────┘         └──────┬───────┘
     │                       │                         │
     │ 1. Create Session     │                         │
     │ POST /admin/sessions  │                         │
     ├──────────────────────>│                         │
     │                       │                         │
     │                       │ SessionService          │
     │                       │ .createSession()        │
     │                       │    │                    │
     │                       │    ▼                    │
     │                       │ Save to DB              │
     │                       │                         │
     │ 2. SessionDto         │                         │
     │<──────────────────────┤                         │
     │                       │                         │
     │ 3. Generate QR        │                         │
     │ POST /sessions/{id}/  │                         │
     │      generate-qr      │                         │
     ├──────────────────────>│                         │
     │                       │                         │
     │                       │ SessionService          │
     │                       │ .generateQrToken()      │
     │                       │    │                    │
     │                       │    ▼                    │
     │                       │ Generate Random Token   │
     │                       │ Set Expiry Time         │
     │                       │ Update Session in DB    │
     │                       │                         │
     │ 4. QR Token Response  │                         │
     │ {token, url, expiry}  │                         │
     │<──────────────────────┤                         │
     │                       │                         │
     │ 5. Display QR Code    │                         │
     │ (Frontend)            │                         │
     │                       │                         │
     │                       │ 6. Scan QR             │
     │                       │ (Frontend Scanner)      │
     │                       │<────────────────────────┤
     │                       │                         │
     │                       │ 7. Check-In Request     │
     │                       │ POST /attendance/       │
     │                       │      check-in           │
     │                       │<────────────────────────┤
     │                       │ {sessionId, qrToken}    │
     │                       │                         │
     │                       │ AttendanceService       │
     │                       │ .checkIn()              │
     │                       │    │                    │
     │                       │    ▼                    │
     │                       │ 1. Validate User        │
     │                       │ 2. Get Student          │
     │                       │ 3. Validate Token       │
     │                       │ 4. Check Expiry         │
     │                       │ 5. Check Duplicate      │
     │                       │ 6. Create Attendance    │
     │                       │                         │
     │                       │ 8. AttendanceDto        │
     │                       │ {status: PRESENT}       │
     │                       │────────────────────────>│
     │                       │                         │
     │ 9. View Attendance    │                         │
     │ GET /attendance/      │                         │
     │     session/{id}      │                         │
     ├──────────────────────>│                         │
     │                       │                         │
     │                       │ AttendanceService       │
     │                       │ .getAttendanceBySession()│
     │                       │                         │
     │ 10. List<AttendanceDto>                         │
     │<──────────────────────┤                         │
     │                       │                         │
```

## Security Flow - JWT Authentication

```
┌─────────┐              ┌─────────────────┐              ┌──────────┐
│  Client │              │   API Server    │              │ Database │
└────┬────┘              └────────┬────────┘              └────┬─────┘
     │                            │                            │
     │ 1. POST /auth/signup       │                            │
     │ {username, password, ...}  │                            │
     ├───────────────────────────>│                            │
     │                            │                            │
     │                            │ AuthService                │
     │                            │ .signup()                  │
     │                            │    │                       │
     │                            │    ▼                       │
     │                            │ Validate Input             │
     │                            │ Hash Password (BCrypt)     │
     │                            │                            │
     │                            │ Save User                  │
     │                            │───────────────────────────>│
     │                            │                            │
     │                            │ JwtTokenProvider           │
     │                            │ .generateToken()           │
     │                            │                            │
     │ 2. JWT Token + UserDto     │                            │
     │<───────────────────────────┤                            │
     │                            │                            │
     │ 3. POST /admin/sessions    │                            │
     │ Authorization: Bearer {JWT}│                            │
     ├───────────────────────────>│                            │
     │                            │                            │
     │                            │ JwtAuthenticationFilter    │
     │                            │    │                       │
     │                            │    ▼                       │
     │                            │ Extract JWT from Header    │
     │                            │ Validate Token             │
     │                            │ Load User Details          │
     │                            │ Set SecurityContext        │
     │                            │                            │
     │                            │ SecurityConfig             │
     │                            │ Check Role (ADMIN)         │
     │                            │    │                       │
     │                            │    ▼                       │
     │                            │ SessionController          │
     │                            │ .createSession()           │
     │                            │                            │
     │ 4. SessionDto              │                            │
     │<───────────────────────────┤                            │
     │                            │                            │
```

## Database Schema Relationships

```
┌─────────────────┐
│      user       │
│─────────────────│
│ id (PK)         │
│ username        │──┐
│ password        │  │
│ email           │  │
│ role            │  │
│ student_id (FK) │──┼──────────┐
└─────────────────┘  │          │
                     │          │
                     │          ▼
                     │   ┌─────────────────┐
                     │   │    student      │
                     │   │─────────────────│
                     │   │ id (PK)         │◄────┐
                     │   │ student_number  │     │
                     │   │ full_name       │     │
                     │   │ email           │     │
                     │   └─────────────────┘     │
                     │                           │
                     │                           │
                     │   ┌─────────────────┐     │
                     │   │     course      │     │
                     │   │─────────────────│     │
                     │   │ id (PK)         │◄──┐ │
                     │   │ code            │   │ │
                     │   │ title           │   │ │
                     │   │ description     │   │ │
                     │   └─────────────────┘   │ │
                     │                         │ │
                     │                         │ │
                     │   ┌─────────────────┐   │ │
                     │   │    session      │   │ │
                     │   │─────────────────│   │ │
                     │   │ id (PK)         │◄──┼─┼─┐
                     │   │ course_id (FK)  │───┘ │ │
                     │   │ session_date    │     │ │
                     │   │ qr_token        │     │ │
                     │   │ qr_expires_at   │     │ │
                     │   └─────────────────┘     │ │
                     │                           │ │
                     │                           │ │
                     │   ┌─────────────────┐     │ │
                     │   │   attendance    │     │ │
                     │   │─────────────────│     │ │
                     │   │ id (PK)         │     │ │
                     │   │ session_id (FK) │─────┘ │
                     │   │ student_id (FK) │───────┘
                     │   │ status          │
                     │   │ checked_at      │
                     │   │ provided_token  │
                     │   └─────────────────┘
```

## Technology Stack Layers

```
┌──────────────────────────────────────────────────────────┐
│                     Presentation Layer                    │
│  - REST Controllers (@RestController)                     │
│  - Request/Response DTOs                                  │
│  - Validation (@Valid)                                    │
│  - Exception Handling                                     │
└─────────────────────────┬────────────────────────────────┘
                          │
┌─────────────────────────▼────────────────────────────────┐
│                     Service Layer                         │
│  - Business Logic (@Service)                              │
│  - Transaction Management (@Transactional)                │
│  - Entity ↔ DTO Mapping                                  │
│  - Authorization Checks                                   │
└─────────────────────────┬────────────────────────────────┘
                          │
┌─────────────────────────▼────────────────────────────────┐
│                  Data Access Layer                        │
│  - JPA Repositories (@Repository)                         │
│  - Spring Data JPA                                        │
│  - Entity Definitions (@Entity)                           │
│  - Custom Queries                                         │
└─────────────────────────┬────────────────────────────────┘
                          │
┌─────────────────────────▼────────────────────────────────┐
│                    Database Layer                         │
│  - MySQL 8.0                                              │
│  - Liquibase Migrations                                   │
│  - Schema Management                                      │
└──────────────────────────────────────────────────────────┘

                    Cross-Cutting Concerns
┌──────────────────────────────────────────────────────────┐
│  - Security (JWT, Spring Security)                        │
│  - Logging (SLF4J)                                        │
│  - Configuration (application.properties)                 │
│  - Exception Handling                                     │
└──────────────────────────────────────────────────────────┘
```
