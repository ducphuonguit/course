-- Sample data for testing the Course Attendance System
-- Run this after Liquibase migrations have been applied

-- Insert sample students
INSERT INTO student (student_number, full_name, email) VALUES
('STU001', 'John Doe', 'john.doe@example.com'),
('STU002', 'Jane Smith', 'jane.smith@example.com'),
('STU003', 'Bob Johnson', 'bob.johnson@example.com'),
('STU004', 'Alice Williams', 'alice.williams@example.com'),
('STU005', 'Charlie Brown', 'charlie.brown@example.com');

-- Insert sample courses
INSERT INTO course (code, title, description, start_date, end_date) VALUES
('CS101', 'Introduction to Programming', 'Learn the basics of programming using Java', '2025-01-15 00:00:00', '2025-05-15 00:00:00'),
('CS201', 'Data Structures', 'Advanced data structures and algorithms', '2025-01-15 00:00:00', '2025-05-15 00:00:00'),
('CS301', 'Database Systems', 'Introduction to relational databases', '2025-01-15 00:00:00', '2025-05-15 00:00:00');

-- Note: Users should be created via the API endpoints for proper password hashing
-- Example API calls:

-- Create Admin User:
-- POST /api/auth/signup
-- {
--   "username": "admin",
--   "password": "admin123",
--   "email": "admin@example.com",
--   "fullName": "System Admin",
--   "role": "ADMIN"
-- }

-- Create Student Users:
-- POST /api/auth/signup
-- {
--   "username": "student1",
--   "password": "student123",
--   "email": "john.doe@example.com",
--   "fullName": "John Doe",
--   "role": "STUDENT",
--   "studentId": 1
-- }

-- Similarly create for other students...

-- Sessions will be created via Admin API after authentication
-- Example session creation:
-- POST /api/admin/sessions
-- {
--   "courseId": 1,
--   "sessionDate": "2025-11-20T10:00:00",
--   "startTime": "2025-11-20T10:00:00",
--   "endTime": "2025-11-20T12:00:00"
-- }
