package com.zentrix.policy.dto;

import com.zentrix.policy.Policy;

import java.time.LocalDateTime;

public record PolicyResponse(
        Integer id, String name, String type, String configurationJson,
        boolean encrypted, LocalDateTime updatedAt
) {
    public static PolicyResponse from(Policy policy, String decryptedConfigurationJson) {
        return new PolicyResponse(
                policy.getId(),
                policy.getName(),
                policy.getType().name(),
                decryptedConfigurationJson,
                policy.isEncrypted(),
                policy.getUpdatedAt()
        );
    }
}
