package com.zentrix.monitoring.dto;

import java.time.LocalDateTime;

public record DeviceStatusResponse(
        Integer deviceId, boolean online, Integer batteryLevel, Long storageFreeBytes,
        Long memoryUsedBytes, Long memoryTotalBytes, LocalDateTime lastSeenAt
) {
}
