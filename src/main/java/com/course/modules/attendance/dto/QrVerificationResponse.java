package com.course.modules.attendance.dto;

import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QrVerificationResponse {
    private boolean valid;
    private String message;
    private Long sessionId;
    private String courseCode;
    private String courseTitle;
    private LocalDateTime sessionDate;
    private LocalDateTime expiresAt;
    private boolean alreadyCheckedIn;
}