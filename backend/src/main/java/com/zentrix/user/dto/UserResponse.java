package com.zentrix.user.dto;

import com.zentrix.user.User;

import java.time.LocalDateTime;
import java.util.List;

public record UserResponse(
        Integer id, String name, String email, String status, LocalDateTime createdAt, List<String> roles
) {
    public static UserResponse from(User user, List<String> roles) {
        return new UserResponse(
                user.getId(), user.getName(), user.getEmail(), user.getStatus().name(), user.getCreatedAt(), roles
        );
    }
}
