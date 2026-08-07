package com.zentrix.application.dto;

import com.zentrix.application.DeviceApplication;

import java.time.LocalDateTime;

public record DeviceApplicationResponse(
        Integer deviceId, String deviceImei, String installedVersion, String status, LocalDateTime updatedAt
) {
    public static DeviceApplicationResponse from(DeviceApplication deviceApplication) {
        return new DeviceApplicationResponse(
                deviceApplication.getDevice().getId(),
                deviceApplication.getDevice().getImei(),
                deviceApplication.getInstalledVersion(),
                deviceApplication.getStatus().name(),
                deviceApplication.getUpdatedAt()
        );
    }
}
