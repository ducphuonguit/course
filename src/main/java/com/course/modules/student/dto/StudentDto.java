package com.course.modules.student.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentDto {
    private Long id;
    private String studentNumber;
    private String fullName;
    private String email;
    // Có thể thêm các trường khác nếu cần, nhưng chỉ những gì Frontend cần hiển thị
}