package com.zentrix.device.dto;

import com.zentrix.device.Device;

import java.time.LocalDateTime;

public record DeviceResponse(
        Integer id, String imei, String serialNumber, String model, String androidVersion,
        String status, LocalDateTime lastSeenAt, LocalDateTime registeredAt,
        Integer groupId, String groupName, boolean enrollmentPending
) {
    public static DeviceResponse from(Device device) {
        return new DeviceResponse(
                device.getId(),
                device.getImei(),
                device.getSerialNumber(),
                device.getModel(),
                device.getAndroidVersion(),
                device.getStatus().name(),
                device.getLastSeenAt(),
                device.getRegisteredAt(),
                device.getGroup() != null ? device.getGroup().getId() : null,
                device.getGroup() != null ? device.getGroup().getName() : null,
                device.getEnrollmentToken() != null
        );
    }
}
