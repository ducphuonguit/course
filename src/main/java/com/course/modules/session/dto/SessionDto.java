package com.course.modules.session.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SessionDto {
    private Long id;
    private Long courseId;
    private String courseCode;
    private String courseTitle;
    private LocalDateTime sessionDate;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String qrToken;
    private LocalDateTime qrTokenExpiresAt;
    private boolean qrTokenActive;
}
