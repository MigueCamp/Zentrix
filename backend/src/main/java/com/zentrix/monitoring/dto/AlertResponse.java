package com.zentrix.monitoring.dto;

import com.zentrix.monitoring.Alert;

import java.time.LocalDateTime;

public record AlertResponse(
        Integer id, Integer deviceId, String deviceImei, String type, String severity,
        String message, boolean acknowledged, LocalDateTime createdAt
) {
    public static AlertResponse from(Alert alert) {
        return new AlertResponse(
                alert.getId(),
                alert.getDevice().getId(),
                alert.getDevice().getImei(),
                alert.getType(),
                alert.getSeverity().name(),
                alert.getMessage(),
                alert.isAcknowledged(),
                alert.getCreatedAt()
        );
    }
}
