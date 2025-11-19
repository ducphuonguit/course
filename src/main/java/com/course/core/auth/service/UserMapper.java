package com.course.core.auth.service;

import com.course.core.auth.dto.UserDto;
import com.course.core.auth.model.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {
    public UserDto toDto(User user) {
        if (user == null) {
            return null;
        }
        return UserDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole())
                .studentId(user.getStudentId())
                .build();
    }
}
