package com.course.modules.session.controller;

import com.course.modules.session.dto.CreateSessionRequest;
import com.course.modules.attendance.dto.QrTokenResponse;
import com.course.modules.session.dto.SessionDto;
import com.course.modules.session.service.SessionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/sessions")
@PreAuthorize("hasRole('ADMIN')")
@CrossOrigin(origins = "*", maxAge = 3600)
@Tag(name = "Session Management", description = "Session management endpoints (Admin only)")
@SecurityRequirement(name = "bearerAuth")
public class SessionController {

    private final SessionService sessionService;

    public SessionController(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    @PostMapping
    @Operation(summary = "Create a new session", description = "Create a new class session for a course")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Session created successfully",
                    content = @Content(schema = @Schema(implementation = SessionDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    public ResponseEntity<?> createSession(@Valid @RequestBody CreateSessionRequest request) {
        try {
            SessionDto session = sessionService.createSession(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(session);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get session details", description = "Retrieve session information by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Session found",
                    content = @Content(schema = @Schema(implementation = SessionDto.class))),
            @ApiResponse(responseCode = "404", description = "Session not found")
    })
    public ResponseEntity<?> getSession(
            @Parameter(description = "Session ID") @PathVariable Long id) {
        try {
            SessionDto session = sessionService.getSession(id);
            return ResponseEntity.ok(session);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse(e.getMessage()));
        }
    }

    @GetMapping("/course/{courseId}")
    @Operation(summary = "Get sessions by course", description = "Retrieve all sessions for a specific course")
    @ApiResponse(responseCode = "200", description = "Sessions retrieved successfully")
    public ResponseEntity<List<SessionDto>> getSessionsByCourse(
            @Parameter(description = "Course ID") @PathVariable Long courseId) {
        List<SessionDto> sessions = sessionService.getSessionsByCourse(courseId);
        return ResponseEntity.ok(sessions);
    }

    @PostMapping("/{id}/generate-qr")
    @Operation(summary = "Generate QR token", 
               description = "Generate a time-limited QR token for student check-in")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "QR token generated",
                    content = @Content(schema = @Schema(implementation = QrTokenResponse.class))),
            @ApiResponse(responseCode = "404", description = "Session not found")
    })
    public ResponseEntity<?> generateQrToken(
            @Parameter(description = "Session ID") @PathVariable Long id,
            @Parameter(description = "Token validity in minutes") @RequestParam(defaultValue = "10") int validityMinutes) {
        try {
            QrTokenResponse response = sessionService.generateQrToken(id, validityMinutes);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse(e.getMessage()));
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
