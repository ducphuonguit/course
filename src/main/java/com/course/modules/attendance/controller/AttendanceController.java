package com.course.modules.attendance.controller;

import com.course.modules.attendance.dto.AttendanceDto;
import com.course.modules.attendance.dto.AttendanceStatisticsDto;
import com.course.modules.attendance.dto.CheckInRequest;
import com.course.modules.attendance.dto.QrVerificationResponse;
import com.course.modules.attendance.service.AttendanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/attendance")
@CrossOrigin(origins = "*", maxAge = 3600)
@Tag(name = "Attendance", description = "Student attendance and check-in endpoints")
public class AttendanceController {

    private final AttendanceService attendanceService;

    public AttendanceController(AttendanceService attendanceService) {
        this.attendanceService = attendanceService;
    }

    // --- 1. API QUÉT MÃ QR (SCAN) ---
    @GetMapping("/scan")
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "Scan & Verify QR Token",
            description = "Verify if the QR code is valid and retrieve session info",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "QR token valid",
                    content = @Content(schema = @Schema(implementation = QrVerificationResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid or expired QR token")
    })
    public ResponseEntity<?> scanQr(
            @Parameter(description = "Session ID") @RequestParam Long sessionId,
            @Parameter(description = "QR Token string") @RequestParam String qrToken) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();

            QrVerificationResponse response = attendanceService.verifyQrToken(sessionId, qrToken, username);

            if (response.isValid()) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.badRequest().body(response);
            }
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    // --- 2. API ĐIỂM DANH (CHECK-IN) ---
    @PostMapping("/check-in")
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "Student check-in",
            description = "Check in to a session using QR token (Records attendance)",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Check-in successful",
                    content = @Content(schema = @Schema(implementation = AttendanceDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid token, expired token, or duplicate check-in")
    })
    public ResponseEntity<?> checkIn(@Valid @RequestBody CheckInRequest request) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();

            AttendanceDto attendance = attendanceService.checkIn(request, username);
            return ResponseEntity.ok(attendance);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    // --- 3. CÁC API XEM DANH SÁCH ĐIỂM DANH ---

    @GetMapping("/session/{sessionId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get attendance list by session (Admin only)",
            description = "Retrieve all attendance records for a session",
            security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<List<AttendanceDto>> getAttendanceBySession(
            @Parameter(description = "Session ID") @PathVariable Long sessionId) {
        List<AttendanceDto> attendances = attendanceService.getAttendanceBySession(sessionId);
        return ResponseEntity.ok(attendances);
    }

    @GetMapping("/student/{studentId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STUDENT')")
    @Operation(summary = "Get attendance history by student",
            description = "Retrieve all attendance records for a student",
            security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<List<AttendanceDto>> getAttendanceByStudent(
            @Parameter(description = "Student ID") @PathVariable Long studentId) {
        List<AttendanceDto> attendances = attendanceService.getAttendanceByStudent(studentId);
        return ResponseEntity.ok(attendances);
    }

    // --- 4. CÁC API THỐNG KÊ (STATISTICS) ---

    @GetMapping("/statistics")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get overall system statistics (Admin only)",
            description = "View overall attendance statistics for the whole system",
            security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<AttendanceStatisticsDto> getStatistics() {
        return ResponseEntity.ok(attendanceService.getStatistics());
    }

    @GetMapping("/statistics/session/{sessionId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get statistics by session (Admin only)",
            description = "View attendance statistics for a specific session",
            security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<?> getStatisticsBySession(
            @Parameter(description = "Session ID") @PathVariable Long sessionId) {
        try {
            return ResponseEntity.ok(attendanceService.getStatisticsBySession(sessionId));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    @GetMapping("/statistics/student/{studentId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STUDENT')")
    @Operation(summary = "Get statistics by student",
            description = "View attendance statistics for a specific student",
            security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<?> getStatisticsByStudent(
            @Parameter(description = "Student ID") @PathVariable Long studentId) {
        try {
            return ResponseEntity.ok(attendanceService.getStatisticsByStudent(studentId));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    @GetMapping("/statistics/course/{courseId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get statistics by course (Admin only)",
            description = "View attendance statistics for a specific course",
            security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<?> getStatisticsByCourse(
            @Parameter(description = "Course ID") @PathVariable Long courseId) {
        try {
            return ResponseEntity.ok(attendanceService.getStatisticsByCourse(courseId));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    // Inner class for error responses
    public static class ErrorResponse {
        private String message;

        public ErrorResponse(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }
}