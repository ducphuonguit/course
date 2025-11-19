# Swagger API Documentation Guide

## Overview

Swagger UI has been integrated into the Course Attendance System to provide interactive API documentation.

## Accessing Swagger UI

Once the application is running, access Swagger UI at:

```
http://localhost:8080/swagger-ui.html
```

or

```
http://localhost:8080/swagger-ui/index.html
```

## OpenAPI JSON

The OpenAPI specification is available at:

```
http://localhost:8080/v3/api-docs
```

## Using Swagger UI

### 1. **View API Endpoints**

Swagger UI displays all available endpoints organized by tags:
- **Authentication** - Signup, login, and user info endpoints
- **Session Management** - Session CRUD and QR generation (Admin only)
- **Attendance** - Check-in and attendance queries

### 2. **Test Endpoints Without Authentication**

Some endpoints don't require authentication:
- `POST /api/auth/signup` - Register new user
- `POST /api/auth/login` - Login

To test:
1. Click on the endpoint to expand it
2. Click "Try it out"
3. Fill in the request body
4. Click "Execute"
5. View the response

### 3. **Authenticate for Protected Endpoints**

Most endpoints require JWT authentication:

#### Step-by-step:

1. **Login or Signup**
   - Use `POST /api/auth/signup` or `POST /api/auth/login`
   - Copy the `token` value from the response

2. **Authorize in Swagger**
   - Click the **"Authorize"** button at the top right
   - In the dialog, paste your token (without "Bearer " prefix)
   - Click "Authorize"
   - Click "Close"

3. **Use Protected Endpoints**
   - Now all requests will include the JWT token automatically
   - Test any protected endpoint

### 4. **Complete Workflow Example**

#### A. Create Admin and Student Users

1. **Signup as Admin**
   ```
   POST /api/auth/signup
   {
     "username": "admin",
     "password": "admin123",
     "email": "admin@example.com",
     "fullName": "System Admin",
     "role": "ADMIN"
   }
   ```
   - Copy the token from response

2. **Authorize with Admin Token**
   - Click "Authorize" button
   - Paste admin token
   - Click "Authorize"

3. **Signup as Student** (optional - for testing)
   ```
   POST /api/auth/signup
   {
     "username": "student1",
     "password": "student123",
     "email": "student1@example.com",
     "fullName": "John Doe",
     "role": "STUDENT",
     "studentId": 1
   }
   ```

#### B. Admin Creates Session and QR Token

1. **Create Session**
   ```
   POST /api/admin/sessions
   {
     "courseId": 1,
     "sessionDate": "2025-11-20T10:00:00",
     "startTime": "2025-11-20T10:00:00",
     "endTime": "2025-11-20T12:00:00"
   }
   ```
   - Note the session ID from response

2. **Generate QR Token**
   ```
   POST /api/admin/sessions/{id}/generate-qr?validityMinutes=10
   ```
   - Replace `{id}` with session ID
   - Copy the `qrToken` from response

#### C. Student Checks In

1. **Re-authorize with Student Token**
   - Logout from admin (click "Authorize" → "Logout")
   - Login as student (if not already)
   - Copy student token
   - Authorize with student token

2. **Check-in**
   ```
   POST /api/attendance/check-in
   {
     "sessionId": 1,
     "qrToken": "paste_qr_token_here"
   }
   ```

#### D. View Attendance

1. **Re-authorize as Admin**
   - Logout from student
   - Authorize with admin token

2. **View Session Attendance**
   ```
   GET /api/attendance/session/{sessionId}
   ```

## API Endpoint Groups

### 🔐 Authentication (No auth required)
- `POST /api/auth/signup` - Register new user
- `POST /api/auth/login` - Login and get JWT
- `GET /api/auth/me` - Get current user (requires auth)

### 📅 Session Management (Admin only)
- `POST /api/admin/sessions` - Create session
- `GET /api/admin/sessions/{id}` - Get session
- `GET /api/admin/sessions/course/{courseId}` - List sessions
- `POST /api/admin/sessions/{id}/generate-qr` - Generate QR token

### ✅ Attendance
- `POST /api/attendance/check-in` - Student check-in (Student only)
- `GET /api/attendance/session/{sessionId}` - View session attendance (Admin)
- `GET /api/attendance/student/{studentId}` - View student attendance (Student/Admin)

## Features

### 1. **Schema Documentation**
Each endpoint shows:
- Request parameters and body schema
- Response codes and schemas
- Required/optional fields
- Field types and validations

### 2. **Try It Out**
- Interactive testing of all endpoints
- Real-time request/response examples
- Automatic request formatting

### 3. **Authorization**
- JWT Bearer token support
- Lock icons show protected endpoints
- Easy token management

### 4. **Response Examples**
- Success responses
- Error responses
- Different status codes

## Tips

1. **Keep Token Handy**: Copy your JWT token immediately after login
2. **Token Expiration**: Tokens expire after 24 hours by default
3. **Re-authorize**: Click "Authorize" again if you get 401 errors
4. **Check Roles**: Some endpoints require specific roles (ADMIN/STUDENT)
5. **Validation Errors**: Swagger shows validation requirements for each field

## Swagger Configuration

The Swagger configuration is in:
- **Config Class**: `src/main/java/com/course/core/config/OpenApiConfig.java`
- **Properties**: `src/main/resources/application.properties`

### Configuration Details

```properties
# Swagger paths
springdoc.api-docs.path=/v3/api-docs
springdoc.swagger-ui.path=/swagger-ui.html

# UI customization
springdoc.swagger-ui.operationsSorter=method
springdoc.swagger-ui.tagsSorter=alpha
```

### Security Configuration

JWT authentication is configured in:
- `SecurityConfig.java` - Permits Swagger endpoints
- `OpenApiConfig.java` - Defines security scheme

## Troubleshooting

### Issue 1: Cannot access Swagger UI
**Solution**: Ensure application is running and visit `http://localhost:8080/swagger-ui.html`

### Issue 2: 401 Unauthorized on protected endpoints
**Solution**: 
1. Login first to get token
2. Click "Authorize" button
3. Paste token (without "Bearer " prefix)
4. Try the endpoint again

### Issue 3: Token doesn't work
**Solution**:
- Make sure you copied the full token
- Don't include "Bearer " prefix in Swagger authorization
- Check if token expired (login again)

### Issue 4: Can't create student user
**Solution**: Ensure `studentId` refers to existing student in database (run sample-data.sql first)

## Benefits of Swagger

✅ **Interactive Documentation** - Test APIs directly in browser
✅ **No Postman Needed** - Everything in one place
✅ **Always Up-to-Date** - Generated from code annotations
✅ **Easy Testing** - Quick endpoint testing during development
✅ **Clear Schemas** - See exact request/response structures
✅ **Security Testing** - Easy JWT token management

## Next Steps

After testing with Swagger:
1. Use the same endpoints in your frontend application
2. Generate client SDKs using OpenAPI spec
3. Share API documentation with team members
4. Export OpenAPI spec for external integrations
