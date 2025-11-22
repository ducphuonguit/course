package com.course.modules.attendance.service;

import com.course.core.auth.model.User;
import com.course.core.auth.repository.UserRepository;
import com.course.modules.attendance.dto.AttendanceDto;
import com.course.modules.attendance.dto.AttendanceStatisticsDto;
import com.course.modules.attendance.dto.CheckInRequest;
import com.course.modules.attendance.dto.QrVerificationResponse;
import com.course.modules.attendance.model.Attendance;
import com.course.modules.attendance.model.AttendanceStatus;
import com.course.modules.course.repository.CourseRepository;
import com.course.modules.session.model.Session;
import com.course.modules.student.model.Student;
import com.course.modules.attendance.repository.AttendanceRepository;
import com.course.modules.session.repository.SessionRepository;
import com.course.modules.student.repository.StudentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final SessionRepository sessionRepository;
    private final StudentRepository studentRepository;
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;

    public AttendanceService(AttendanceRepository attendanceRepository,
                             SessionRepository sessionRepository,
                             StudentRepository studentRepository,
                             UserRepository userRepository,
                             CourseRepository courseRepository) {
        this.attendanceRepository = attendanceRepository;
        this.sessionRepository = sessionRepository;
        this.studentRepository = studentRepository;
        this.userRepository = userRepository;
        this.courseRepository = courseRepository;
    }

    // --- 1. API GHI NHẬN ĐIỂM DANH (CHECK-IN) ---
    @Transactional
    public AttendanceDto checkIn(CheckInRequest request, String username) {
        // Validate User & Student
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        if (user.getStudentId() == null) throw new RuntimeException("Only students can check in");
        Student student = studentRepository.findById(user.getStudentId())
                .orElseThrow(() -> new RuntimeException("Student not found"));

        // Validate Session & Token
        Session session = sessionRepository.findById(request.getSessionId())
                .orElseThrow(() -> new RuntimeException("Session not found"));

        if (session.getQrToken() == null || !session.getQrToken().equals(request.getQrToken())) {
            throw new RuntimeException("Invalid QR token");
        }
        if (session.getQrTokenExpiresAt() == null || session.getQrTokenExpiresAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("QR token has expired");
        }

        // Kiểm tra trùng lặp
        if (attendanceRepository.findBySessionIdAndStudentId(session.getId(), student.getId()).isPresent()) {
            throw new RuntimeException("Student has already checked in to this session");
        }

        // --- LOGIC NGHIỆP VỤ NÂNG CAO ---
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startTime = session.getStartTime();

        // Chặn điểm danh sớm (ví dụ: trước 30 phút)
        if (now.isBefore(startTime.minusMinutes(30))) {
            throw new RuntimeException("Attendance is not open yet");
        }

        // Tính LATE (Trễ 15 phút)
        AttendanceStatus status = AttendanceStatus.PRESENT;
        long minutesLate = ChronoUnit.MINUTES.between(startTime, now);
        if (minutesLate > 15) {
            status = AttendanceStatus.LATE;
        }

        // Lưu DB
        Attendance attendance = Attendance.builder()
                .session(session)
                .student(student)
                .status(status)
                .checkedAt(now)
                .providedQrToken(request.getQrToken())
                .build();

        return toDto(attendanceRepository.save(attendance));
    }

    // --- 2. API QUÉT & XÁC THỰC QR (SCAN - KHÔNG LƯU DB) ---
    public QrVerificationResponse verifyQrToken(Long sessionId, String qrToken, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found"));

        boolean isValid = true;
        String message = "Valid QR Token";

        if (session.getQrToken() == null || !session.getQrToken().equals(qrToken)) {
            isValid = false;
            message = "Invalid QR Token";
        } else if (session.getQrTokenExpiresAt().isBefore(LocalDateTime.now())) {
            isValid = false;
            message = "QR Token Expired";
        }

        // Kiểm tra xem đã điểm danh chưa để báo cho FE biết
        boolean alreadyCheckedIn = false;
        if (user.getStudentId() != null) {
            alreadyCheckedIn = attendanceRepository.findBySessionIdAndStudentId(sessionId, user.getStudentId()).isPresent();
        }

        return QrVerificationResponse.builder()
                .valid(isValid)
                .message(message)
                .sessionId(session.getId())
                .courseCode(session.getCourse().getCode())
                .courseTitle(session.getCourse().getTitle())
                .sessionDate(session.getSessionDate())
                .expiresAt(session.getQrTokenExpiresAt())
                .alreadyCheckedIn(alreadyCheckedIn)
                .build();
    }

    // --- 3. CÁC API THỐNG KÊ (STATISTICS) ---

    // Thống kê tổng quan toàn hệ thống
    public AttendanceStatisticsDto getStatistics() {
        List<Attendance> all = attendanceRepository.findAll();
        return calculateStats(all, sessionRepository.count(), studentRepository.count());
    }

    // Thống kê theo Buổi học (Ai vắng, ai đi trễ...)
    public AttendanceStatisticsDto.SessionStatistics getStatisticsBySession(Long sessionId) {
        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found"));
        List<Attendance> attendances = attendanceRepository.findBySessionId(sessionId);

        // Giả sử tổng sinh viên là tất cả sinh viên trong DB (hoặc lấy theo đăng ký khóa học nếu có bảng enrollment)
        long totalStudents = studentRepository.count();

        AttendanceStatisticsDto stats = calculateStats(attendances, 1, totalStudents);

        return AttendanceStatisticsDto.SessionStatistics.builder()
                .sessionId(session.getId())
                .sessionDate(session.getSessionDate().toString())
                .totalStudents(totalStudents)
                .presentCount(stats.getPresentCount())
                .lateCount(stats.getLateCount())
                .absentCount(stats.getAbsentCount())
                .attendanceRate(stats.getAttendanceRate())
                .build();
    }

    // Thống kê theo Sinh viên (Tỉ lệ đi học của 1 người)
    public AttendanceStatisticsDto.StudentStatistics getStatisticsByStudent(Long studentId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));
        List<Attendance> attendances = attendanceRepository.findByStudentId(studentId);
        long totalSessions = sessionRepository.count(); // Tổng số buổi đã diễn ra

        AttendanceStatisticsDto stats = calculateStats(attendances, totalSessions, 1);

        return AttendanceStatisticsDto.StudentStatistics.builder()
                .studentId(student.getId())
                .studentNumber(student.getStudentNumber())
                .studentName(student.getFullName())
                .totalSessions(totalSessions)
                .presentCount(stats.getPresentCount())
                .lateCount(stats.getLateCount())
                .absentCount(stats.getAbsentCount()) // Absent = Total - (Present + Late)
                .attendanceRate(stats.getAttendanceRate())
                .build();
    }

    // Thống kê theo Khóa học
    public AttendanceStatisticsDto getStatisticsByCourse(Long courseId) {
        List<Session> sessions = sessionRepository.findByCourseId(courseId);
        List<Long> sessionIds = sessions.stream().map(Session::getId).collect(Collectors.toList());
        // Cần thêm hàm findBySessionIdIn trong Repository nếu chưa có, tạm thời dùng findAll filter
        List<Attendance> attendances = attendanceRepository.findAll().stream()
                .filter(a -> sessionIds.contains(a.getSession().getId()))
                .collect(Collectors.toList());

        return calculateStats(attendances, sessions.size(), studentRepository.count());
    }

    // Hàm phụ trợ tính toán số liệu
    private AttendanceStatisticsDto calculateStats(List<Attendance> attendances, long totalSessions, long totalStudents) {
        long present = attendances.stream().filter(a -> a.getStatus() == AttendanceStatus.PRESENT).count();
        long late = attendances.stream().filter(a -> a.getStatus() == AttendanceStatus.LATE).count();
        long totalRecords = totalSessions * totalStudents; // Tổng số lượt điểm danh kỳ vọng
        long absent = (totalRecords > 0) ? totalRecords - (present + late) : 0; // Vắng = Tổng - Có mặt

        if (absent < 0) absent = 0; // Fix bug nếu dữ liệu không khớp

        double rate = (totalRecords > 0) ? (double)(present + late) / totalRecords * 100 : 0;

        Map<String, Long> statusCount = new HashMap<>();
        statusCount.put("PRESENT", present);
        statusCount.put("LATE", late);
        statusCount.put("ABSENT", absent);

        return AttendanceStatisticsDto.builder()
                .presentCount(present)
                .lateCount(late)
                .absentCount(absent)
                .attendanceRate(rate)
                .statusCount(statusCount)
                .build();
    }

    // --- CÁC HÀM GET LIST CŨ ---
    public List<AttendanceDto> getAttendanceBySession(Long sessionId) {
        return attendanceRepository.findBySessionId(sessionId).stream().map(this::toDto).collect(Collectors.toList());
    }

    public List<AttendanceDto> getAttendanceByStudent(Long studentId) {
        return attendanceRepository.findByStudentId(studentId).stream().map(this::toDto).collect(Collectors.toList());
    }

    private AttendanceDto toDto(Attendance a) {
        return AttendanceDto.builder()
                .id(a.getId())
                .sessionId(a.getSession().getId())
                .studentId(a.getStudent().getId())
                .studentNumber(a.getStudent().getStudentNumber())
                .studentName(a.getStudent().getFullName())
                .status(a.getStatus())
                .checkedAt(a.getCheckedAt())
                .build();
    }
}