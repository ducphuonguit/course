package com.course.modules.attendance.dto;

import lombok.*;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttendanceStatisticsDto {
    // Thống kê tổng quan
    private long totalSessions;
    private long totalStudents;
    private long totalAttendanceRecords;
    private long presentCount;
    private long absentCount;
    private long lateCount;
    private double attendanceRate;
    private Map<String, Long> statusCount;

    // Class con: Thống kê theo Buổi học
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SessionStatistics {
        private Long sessionId;
        private String sessionDate;
        private long totalStudents;
        private long presentCount;
        private long absentCount;
        private long lateCount;
        private double attendanceRate;
    }

    // Class con: Thống kê theo Sinh viên
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class StudentStatistics {
        private Long studentId;
        private String studentNumber;
        private String studentName;
        private long totalSessions;
        private long presentCount;
        private long absentCount;
        private long lateCount;
        private double attendanceRate;
    }
}