package com.course.core.auth.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponse {
    private String token;
    
    @Builder.Default
    private String type = "Bearer";
    
    private UserDto user;

    public AuthResponse(String token, UserDto user) {
        this.token = token;
        this.type = "Bearer";
        this.user = user;
    }
}
