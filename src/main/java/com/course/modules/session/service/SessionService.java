package com.course.modules.session.service;

import com.course.modules.session.dto.CreateSessionRequest;
import com.course.modules.attendance.dto.QrTokenResponse;
import com.course.modules.session.dto.SessionDto;
import com.course.modules.course.model.Course;
import com.course.modules.session.model.Session;
import com.course.modules.course.repository.CourseRepository;
import com.course.modules.session.repository.SessionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SessionService {

    private final SessionRepository sessionRepository;
    private final CourseRepository courseRepository;
    private static final SecureRandom secureRandom = new SecureRandom();

    public SessionService(SessionRepository sessionRepository, CourseRepository courseRepository) {
        this.sessionRepository = sessionRepository;
        this.courseRepository = courseRepository;
    }

    @Transactional
    public SessionDto createSession(CreateSessionRequest request) {
        Course course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> new RuntimeException("Course not found with id: " + request.getCourseId()));

        Session session = Session.builder()
                .course(course)
                .sessionDate(request.getSessionDate())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .build();

        session = sessionRepository.save(session);
        return toDto(session);
    }

    @Transactional
    public QrTokenResponse generateQrToken(Long sessionId, int validityMinutes) {
        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found with id: " + sessionId));

        // Generate secure random token
        byte[] randomBytes = new byte[32];
        secureRandom.nextBytes(randomBytes);
        String qrToken = Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);

        // Set expiry time
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(validityMinutes);

        session.setQrToken(qrToken);
        session.setQrTokenExpiresAt(expiresAt);
        sessionRepository.save(session);

        return QrTokenResponse.builder()
                .sessionId(session.getId())
                .qrToken(qrToken)
                .checkInUrl("/api/attendance/scan?sessionId=" + sessionId + "&token=" + qrToken)
                .expiresAt(expiresAt.toString())
                .build();
    }

    public SessionDto getSession(Long id) {
        Session session = sessionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Session not found with id: " + id));
        return toDto(session);
    }

    public List<SessionDto> getSessionsByCourse(Long courseId) {
        return sessionRepository.findByCourseId(courseId).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    private SessionDto toDto(Session session) {
        boolean isActive = session.getQrToken() != null &&
                session.getQrTokenExpiresAt() != null &&
                session.getQrTokenExpiresAt().isAfter(LocalDateTime.now());

        return SessionDto.builder()
                .id(session.getId())
                .courseId(session.getCourse().getId())
                .courseCode(session.getCourse().getCode())
                .courseTitle(session.getCourse().getTitle())
                .sessionDate(session.getSessionDate())
                .startTime(session.getStartTime())
                .endTime(session.getEndTime())
                .qrToken(session.getQrToken())
                .qrTokenExpiresAt(session.getQrTokenExpiresAt())
                .qrTokenActive(isActive)
                .build();
    }
}
