package com.course.modules.attendance.service;

import com.course.core.auth.model.User;
import com.course.core.auth.repository.UserRepository;
import com.course.modules.attendance.dto.AttendanceDto;
import com.course.modules.attendance.dto.CheckInRequest;
import com.course.modules.attendance.model.Attendance;
import com.course.modules.attendance.model.AttendanceStatus;
import com.course.modules.session.model.Session;
import com.course.modules.student.model.Student;
import com.course.modules.attendance.repository.AttendanceRepository;
import com.course.modules.session.repository.SessionRepository;
import com.course.modules.student.repository.StudentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final SessionRepository sessionRepository;
    private final StudentRepository studentRepository;
    private final UserRepository userRepository;

    public AttendanceService(AttendanceRepository attendanceRepository,
                            SessionRepository sessionRepository,
                            StudentRepository studentRepository,
                            UserRepository userRepository) {
        this.attendanceRepository = attendanceRepository;
        this.sessionRepository = sessionRepository;
        this.studentRepository = studentRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public AttendanceDto checkIn(CheckInRequest request, String username) {
        // Get user by username
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Verify user is a student
        if (user.getStudentId() == null) {
            throw new RuntimeException("Only students can check in to sessions");
        }

        // Get student
        Student student = studentRepository.findById(user.getStudentId())
                .orElseThrow(() -> new RuntimeException("Student not found"));

        // Get session
        Session session = sessionRepository.findById(request.getSessionId())
                .orElseThrow(() -> new RuntimeException("Session not found"));

        // Validate QR token
        if (session.getQrToken() == null || !session.getQrToken().equals(request.getQrToken())) {
            throw new RuntimeException("Invalid QR token");
        }

        // Check if token has expired
        if (session.getQrTokenExpiresAt() == null || 
            session.getQrTokenExpiresAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("QR token has expired");
        }

        // Check if student already checked in
        var existingAttendance = attendanceRepository.findBySessionIdAndStudentId(
                session.getId(), student.getId());
        if (existingAttendance.isPresent()) {
            throw new RuntimeException("Student has already checked in to this session");
        }

        // Create attendance record
        Attendance attendance = Attendance.builder()
                .session(session)
                .student(student)
                .status(AttendanceStatus.PRESENT)
                .checkedAt(LocalDateTime.now())
                .providedQrToken(request.getQrToken())
                .build();

        attendance = attendanceRepository.save(attendance);
        return toDto(attendance);
    }

    public List<AttendanceDto> getAttendanceBySession(Long sessionId) {
        return attendanceRepository.findBySessionId(sessionId).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public List<AttendanceDto> getAttendanceByStudent(Long studentId) {
        return attendanceRepository.findByStudentId(studentId).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    private AttendanceDto toDto(Attendance attendance) {
        return AttendanceDto.builder()
                .id(attendance.getId())
                .sessionId(attendance.getSession().getId())
                .studentId(attendance.getStudent().getId())
                .studentNumber(attendance.getStudent().getStudentNumber())
                .studentName(attendance.getStudent().getFullName())
                .status(attendance.getStatus())
                .checkedAt(attendance.getCheckedAt())
                .build();
    }
}
