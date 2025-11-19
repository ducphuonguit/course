package com.course.modules.attendance.controller;

import com.course.modules.attendance.dto.AttendanceDto;
import com.course.modules.attendance.dto.CheckInRequest;
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

    @PostMapping("/check-in")
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "Student check-in", 
               description = "Check in to a session using QR token",
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

    @GetMapping("/session/{sessionId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get attendance by session", 
               description = "Retrieve all attendance records for a session (Admin only)",
               security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "200", description = "Attendance records retrieved successfully")
    public ResponseEntity<List<AttendanceDto>> getAttendanceBySession(
            @Parameter(description = "Session ID") @PathVariable Long sessionId) {
        List<AttendanceDto> attendances = attendanceService.getAttendanceBySession(sessionId);
        return ResponseEntity.ok(attendances);
    }

    @GetMapping("/student/{studentId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STUDENT')")
    @Operation(summary = "Get attendance by student", 
               description = "Retrieve all attendance records for a student",
               security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "200", description = "Attendance records retrieved successfully")
    public ResponseEntity<List<AttendanceDto>> getAttendanceByStudent(
            @Parameter(description = "Student ID") @PathVariable Long studentId) {
        List<AttendanceDto> attendances = attendanceService.getAttendanceByStudent(studentId);
        return ResponseEntity.ok(attendances);
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
