package com.course.modules.attendance.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QrTokenResponse {
    private Long sessionId;
    private String qrToken;
    private String checkInUrl;
    private String expiresAt;
}
