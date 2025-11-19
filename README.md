# Course Attendance System

> **🎉 Implementation Complete!** This project now includes full authentication and QR-based attendance functionality.

## ✨ What's Implemented

### 1. **Modular Architecture**
- ✅ Core module for authentication (`com.course.core.auth`)
- ✅ Attendance module (`com.course.modules.attendance`)
- ✅ Clean separation of concerns with layered architecture

### 2. **Authentication System**
- ✅ User signup and login with JWT tokens
- ✅ Two user roles: **ADMIN** and **STUDENT**
- ✅ Secure password hashing with BCrypt
- ✅ JWT-based stateless authentication
- ✅ Role-based access control

### 3. **QR Code Check-In System**
- ✅ Admin creates sessions for courses
- ✅ Admin generates secure QR tokens with expiration
- ✅ Students scan QR codes to check in
- ✅ Automatic validation of tokens and attendance recording
- ✅ Prevention of duplicate check-ins

### 4. **Database Schema**
- ✅ Liquibase migrations for all tables
- ✅ User, Student, Course, Session, Attendance entities
- ✅ Proper foreign key relationships

### 5. **API Endpoints**
See [API_DOCUMENTATION.md](./API_DOCUMENTATION.md) for complete API reference.

**Interactive API Documentation:**
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **OpenAPI Spec**: http://localhost:8080/v3/api-docs

**Authentication:**
- `POST /api/auth/signup` - Register new user
- `POST /api/auth/login` - Login and get JWT token
- `GET /api/auth/me` - Get current user info

**Session Management (Admin):**
- `POST /api/admin/sessions` - Create new session
- `POST /api/admin/sessions/{id}/generate-qr` - Generate QR token
- `GET /api/admin/sessions/{id}` - Get session details
- `GET /api/admin/sessions/course/{courseId}` - List course sessions

**Attendance:**
- `POST /api/attendance/check-in` - Student check-in via QR
- `GET /api/attendance/session/{sessionId}` - Get session attendance
- `GET /api/attendance/student/{studentId}` - Get student attendance

## 🚀 Quick Start

1. **Start MySQL Database**
   ```bash
   # Ensure MySQL is running on localhost:3307
   # Database: course
   ```

2. **Run the Application**
   ```bash
   ./mvnw spring-boot:run
   ```

3. **Access Swagger UI**
   ```
   http://localhost:8080/swagger-ui.html
   ```
   See [SWAGGER_GUIDE.md](./SWAGGER_GUIDE.md) for detailed Swagger usage.

4. **Create Admin User**
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

## 📊 How QR Check-In Works

1. **Admin creates a session** for a course
2. **Admin generates QR token** (valid for X minutes)
3. **Frontend displays QR code** (to be implemented)
4. **Student scans QR code** (must be logged in)
5. **System validates and records attendance**

## 🛡️ Security Features

- JWT authentication with 24-hour token expiration
- BCrypt password hashing
- Role-based authorization (ADMIN/STUDENT)
- Secure random QR tokens
- Token expiration validation
- CORS support

## 📁 Project Structure

```
src/main/java/com/course/
├── core/
│   └── auth/              # Authentication module
│       ├── controller/    # AuthController
│       ├── dto/          # Login/Signup DTOs
│       ├── model/        # User entity
│       ├── repository/   # UserRepository
│       ├── security/     # SecurityConfig, JWT Filter
│       └── service/      # AuthService, JwtTokenProvider
└── modules/
    └── attendance/       # Attendance module
        ├── controller/   # Session & Attendance controllers
        ├── dto/         # Session, Attendance DTOs
        ├── model/       # Course, Session, Student, Attendance
        ├── repository/  # JPA repositories
        └── service/     # Business logic
```

## 🔧 Configuration

Edit `src/main/resources/application.properties`:

```properties
# JWT
jwt.secret=your-secret-key-at-least-256-bits
jwt.expiration=86400000

# Database
spring.datasource.url=jdbc:mysql://localhost:3307/course
spring.datasource.username=root
spring.datasource.password=my-secret-pw
```

## 📝 Next Steps

For production deployment, consider:

1. **Frontend Development**
   - Build QR code display for admins
   - Build QR scanner for students
   - Create dashboards

2. **Additional Features**
   - Course CRUD operations
   - Student management
   - Attendance reports
   - Email notifications
   - Late arrivals handling

3. **Production Hardening**
   - Use environment variables for secrets
   - Add refresh tokens
   - Implement rate limiting
   - Add comprehensive logging
   - Set up monitoring

---

# Course Project — Architecture + Liquibase + Module Guide

Mục tiêu: Cung cấp schema Liquibase cho các bảng chính (courses, sessions, attendance, students) và hướng dẫn cách triển khai module theo các layer: controller, service, repository, models.

## Tóm tắt tính năng (the requested features)

1. Thiết kế courses, sessions, attendance.
2. Sinh mã QR cho mỗi buổi học.
3. Quét QR để điểm danh.
4. Xác thực token QR.
5. Ghi nhận điểm danh học viên.

## Liquibase

Các changelog nằm ở `src/main/resources/db/changelog`.
- Master changelog: `db/changelog/db.changelog-master.yaml`
- Tạo bảng cơ bản: `000-create-tables.yaml`

Spring Boot (khi có `liquibase-core` trên classpath) sẽ tự chạy Liquibase nếu có `spring.datasource.*` hoặc `DataSource` cấu hình. Thêm vào `application.properties` (hoặc sử dụng env vars):

```properties
# Example (development):
spring.datasource.url=jdbc:mysql://localhost:3306/course_db?useSSL=false&serverTimezone=UTC
spring.datasource.username=root
spring.datasource.password=changeit
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# Liquibase master changelog
spring.liquibase.change-log=classpath:db/changelog/db.changelog-master.yaml
```

Ghi chú: không chỉnh sửa các changeset đã được apply trong production; thay vào đó tạo file changeset mới với id duy nhất.

## Database schema (sơ lược)

- `student` — danh sách học viên (id, student_number, full_name, email)
- `course` — khoá học (id, code, title, description, start_date, end_date)
- `session` — buổi học thuộc course, chứa `qr_token` và `qr_token_expires_at`
- `attendance` — bản ghi điểm danh (session_id, student_id, status, checked_at, provided_qr_token)

## Thiết kế module — thư mục và quy ước

Project sẽ dùng cấu trúc package: `com.course.<feature>` hoặc `com.course` với các folders cho layer:

- `controller` — REST controllers (HTTP endpoints)
- `service` — business logic, trả về DTOs
- `repository` — Spring Data JPA repositories hoặc custom DAO
- `models` / `entity` — JPA entities / domain models
- `dto` — các Data Transfer Objects (request/response)

Ví dụ cấu trúc (mô tả):

```
src/main/java/com/course/
  ├─ controller/
  │    └─ CourseController.java
  ├─ service/
  │    └─ CourseService.java
  ├─ repository/
  │    └─ CourseRepository.java
  ├─ model/
  │    └─ Course.java
  └─ dto/
       └─ CourseDto.java
```

### Contract / small checklist for each module

- Inputs: HTTP JSON request or path params
- Outputs: JSON responses, HTTP status codes
- Errors: validation -> 400, not found -> 404, server errors -> 500
- Success: 200/201 with payload

Edge cases: duplicate records, concurrent QR scans, expired QR token, student already checked-in.

## Example code skeletons (Java + Spring Boot)

1) Entity (models/Course.java)

```java
@Entity
@Table(name = "course")
public class Course {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  private String code;
  private String title;
  private String description;
  // getters/setters
}
```

2) Repository (repository/CourseRepository.java)

```java
public interface CourseRepository extends JpaRepository<Course, Long> {
  Optional<Course> findByCode(String code);
}
```

3) Service (service/CourseService.java)

```java
@Service
public class CourseService {
  private final CourseRepository repo;
  public CourseService(CourseRepository repo) { this.repo = repo; }

  public Course createCourse(Course c) { return repo.save(c); }
}
```

4) Controller (controller/CourseController.java)

```java
@RestController
@RequestMapping("/api/courses")
public class CourseController {
  private final CourseService svc;
  public CourseController(CourseService svc){ this.svc = svc; }

  @PostMapping
  public ResponseEntity<Course> create(@RequestBody Course c){
    Course saved = svc.createCourse(c);
    return ResponseEntity.status(HttpStatus.CREATED).body(saved);
  }
}
```

## QR generation & validation guidance

- Each `session` row should have a `qr_token` and `qr_token_expires_at`.
- To generate a token:
  - Option A: random UUID + store in `session.qr_token` with expiry.
  - Option B: HMAC-signed token: payload=(sessionId|expiry), sign with server secret; no DB write required but include signature validation and optional replay protection.

Recommended flow for simplicity:
- When instructor opens attendance for a session, server generates a secure random token (UUIDv4 or Base64 url-safe random 32 bytes), store `qr_token` and expiry (e.g., now + 5 minutes).
- QR image encodes an URL: https://your-app/attendance/scan?session={id}&token={token}
- Mobile/web client opens URL and POSTs to API with student's identity and the token.
- Server validates:
  - session exists
  - token equals `session.qr_token`
  - now < `qr_token_expires_at`
  - optionally check student not already present
- Then create `attendance` record with status `PRESENT` and timestamp, and optionally invalidate token if you want single-use.

Security notes:
- Use HTTPS for QR endpoints.
- Use short token expiry (e.g., 2–10 minutes).
- Consider rate-limiting and replay prevention.

## How to add a schema change

1. Create a new file `src/main/resources/db/changelog/{timestamp}-your-change.yaml`.
2. Add a unique `changeSet` id and author.
3. Include the file in `db.changelog-master.yaml` (or rely on the master to include all files by pattern if you change the master).
4. Commit and deploy; Liquibase will run at application startup.

Example new changeset header:

```yaml
- changeSet:
    id: 0002-add-column-xyz
    author: yourname
    changes:
      - addColumn:
          tableName: course
          columns:
            - column:
                name: new_col
                type: VARCHAR(255)
```

## How to run locally

- Ensure MySQL (or your chosen DB) is running and the DB exists.
- Add `spring.datasource.*` into `application.properties` or use env vars.
- Run via Maven:

```bash
./mvnw spring-boot:run
```

Liquibase will apply migrations at startup.

## Next steps / suggestions

- Add `spring-boot-starter-security` if you need authentication for endpoints.
- Add DTOs and mapping (MapStruct or manual) to decouple entities from API models.
- Add tests: unit tests for services and integration tests for controllers and Liquibase migrations.

---
If you want, I can also:
- Add example JPA entity classes and repository interfaces for the tables created by Liquibase.
- Create a sample endpoint for generating QR tokens and one for consuming them (scan endpoint).

Tell me which extra piece you'd like implemented next and I'll add it.
