package com.course.core.auth.dto;

import com.course.core.auth.model.UserRole;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDto {
    private Long id;
    private String username;
    private String email;
    private String fullName;
    private UserRole role;
    private Long studentId;
}
