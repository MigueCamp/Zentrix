package com.zentrix.config.dto;

import com.zentrix.config.ApiKey;

import java.time.LocalDateTime;

public record ApiKeyResponse(
        Integer id, String name, String prefix, boolean active,
        LocalDateTime createdAt, LocalDateTime revokedAt
) {
    public static ApiKeyResponse from(ApiKey apiKey) {
        return new ApiKeyResponse(
                apiKey.getId(),
                apiKey.getName(),
                apiKey.getPrefix(),
                apiKey.isActive(),
                apiKey.getCreatedAt(),
                apiKey.getRevokedAt()
        );
    }
}
