package com.course.modules.attendance.dto;

import com.course.modules.attendance.model.AttendanceStatus;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttendanceDto {
    private Long id;
    private Long sessionId;
    private Long studentId;
    private String studentNumber;
    private String studentName;
    private AttendanceStatus status;
    private LocalDateTime checkedAt;
}
