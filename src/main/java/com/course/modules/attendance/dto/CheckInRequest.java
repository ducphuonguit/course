package com.course.modules.attendance.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CheckInRequest {
    @NotNull(message = "Session ID is required")
    private Long sessionId;

    @NotBlank(message = "QR token is required")
    private String qrToken;
}
