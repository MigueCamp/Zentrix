package com.zentrix.command.dto;

import com.zentrix.command.DeviceCommand;

import java.time.LocalDateTime;

public record DeviceCommandResponse(
        Long id, String type, String payloadJson, String status,
        String resultDetail, LocalDateTime createdAt, LocalDateTime updatedAt
) {
    public static DeviceCommandResponse from(DeviceCommand command) {
        return new DeviceCommandResponse(
                command.getId(),
                command.getType().name(),
                command.getPayloadJson(),
                command.getStatus().name(),
                command.getResultDetail(),
                command.getCreatedAt(),
                command.getUpdatedAt()
        );
    }
}
